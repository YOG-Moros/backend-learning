package top.cch.web.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WebListener
public class AppLifecycleListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        String contextPath = event.getServletContext().getContextPath();
        log.info("ServletContext 初始化，contextPath=[{}]", contextPath.isEmpty() ? "/" : contextPath);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        log.info("ServletContext 销毁，应用即将退出");
    }
}
