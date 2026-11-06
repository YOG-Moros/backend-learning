package top.cch.web.listener;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@WebListener
public class OnlineCountListener implements HttpSessionListener {

    private static final AtomicInteger ONLINE = new AtomicInteger();

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        int now = ONLINE.incrementAndGet();
        log.info("session 创建，id={}，当前在线 {} 人", event.getSession().getId(), now);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        int now = ONLINE.decrementAndGet();
        log.info("session 销毁，id={}，当前在线 {} 人", event.getSession().getId(), now);
    }

    public static int online() {
        return ONLINE.get();
    }
}
