package com.grid.background;

import com.grid.ApplicationContext;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Based on: 
 * http://stackoverflow.com/questions/4691132/how-to-run-a-background-task-in-a-servlet-based-web-application
 * http://stackoverflow.com/a/4691650
 * 
 */
@WebListener
public class BackgroundJobManager implements ServletContextListener {
    
    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new ExpirationHandlerJob(), 0, ApplicationContext.getExpirationScanPeriod(), TimeUnit.MINUTES);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        scheduler.shutdownNow();
    }
}
