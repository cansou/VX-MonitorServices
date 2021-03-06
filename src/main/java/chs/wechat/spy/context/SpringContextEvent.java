package chs.wechat.spy.context;

import chs.wechat.spy.db.redis.RedisConnManager;
import chs.wechat.spy.utils.ConfigProperties;
import chs.wechat.spy.utils.DownloadPool;
import chs.wechat.spy.websocket.WebSocketClient;
import chs.wechat.spy.websocket.WebSocketServerSingleton;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Objects;

public class SpringContextEvent implements ApplicationListener {
    private Process p = null;

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        Runtime runtime = Runtime.getRuntime();
        if (applicationEvent instanceof ApplicationStartedEvent) {
            RedisConnManager rcm = RedisConnManager.getInstance();
            rcm.setServerIp(ConfigProperties.GetProperties("status_redis_server.host"));
            rcm.setPort(Integer.parseInt(ConfigProperties.GetProperties("status_redis_server.port")));
            rcm.BuildJedisPool();
            String client_host = ConfigProperties.GetProperties("api_websocket_server.host");
            String client_port = ConfigProperties.GetProperties("api_websocket_server.port");
            WebSocketClient ws = WebSocketClient.getInstance();
            ws.setHost(client_host);
            ws.setPort(Integer.parseInt(client_port));
            String server_host = ConfigProperties.GetProperties("monitor_websocket_server.host");
            String server_port = ConfigProperties.GetProperties("monitor_websocket_server.port");
            WebSocketServerSingleton wss = WebSocketServerSingleton.getInstance();
            wss.setHost(server_host);
            wss.setPort(Integer.parseInt(server_port));
            try {
                wss.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
            DownloadPool downloadPool = DownloadPool.getInstance();
            downloadPool.StartDownloadPool();
            try {
                p = runtime.exec(Objects.requireNonNull(SpringContextEvent.class.getClassLoader().getResource("")).getPath() + "wechat_server\\WechatServer.exe");
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Start Complete!");
        } else if (applicationEvent instanceof ContextClosedEvent) {
            WebSocketServerSingleton.getInstance().shutdown();
            WebSocketClient.getInstance().close();
            DownloadPool.getInstance().StopDownloadPool();
            RedisConnManager.getInstance().shutdown();
            try {
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
                bw.write("exit");
                bw.flush();
                bw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
