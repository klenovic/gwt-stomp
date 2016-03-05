package com.codeveo.gwt.stomp.testing;

import org.apache.catalina.*;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.servlet.ServletException;
import java.io.File;

public class TomcatManager {

    private final static TomcatManager INSTANCE = new TomcatManager();

    private final static Logger LOG = LoggerFactory.getLogger(TomcatManager.class);

    private final Tomcat tomcat;

    private boolean hasFailedOnce = false;

    private Thread waitStartThread;

    private boolean started = false;

    private TomcatManager() {
        SLF4JBridgeHandler.install();
        tomcat = new Tomcat();
        tomcat.setPort(0);
        tomcat.setBaseDir(new File("target/tomcat").getAbsolutePath());
        tomcat.setSilent(false);
        final Context context;
        try {
            context = tomcat.addWebapp("/testApp", new File("target/webapp").getAbsolutePath());
            context.addLifecycleListener(new LifecycleListener() {
                @Override
                public void lifecycleEvent(LifecycleEvent event) {
                    onLifecycleChanged(event.getLifecycle().getState());
                }
            });
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                if(!started) {
                    return;
                }

                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                LOG.info("Clean stop of Tomcat from shutdown hook.");
                try {
                    tomcat.stop();
                } catch (LifecycleException e) {
                    // SILENT STOPING
                }
            }
        }));
    }

    private void onLifecycleChanged(LifecycleState lifecycleState) {
        switch (lifecycleState) {
            case FAILED:
                hasFailedOnce = true;
                try {
                    LOG.warn("Le déploiement du contexte a échoué, arrêt de Tomcat....");
                    tomcat.stop();
                } catch (LifecycleException e) {
                    // SILENT STOPING
                } finally {
                    interruptStartWaitingThread();
                }
                break;
        }
    }

    public static TomcatManager getInstance() {
        return INSTANCE;
    }

    public void start() {

        try {
            LOG.info("BEFORE FOR NAME org.apache.jasper.servlet.JspServlet");
            Class.forName("org.apache.jasper.servlet.JspServlet");
            LOG.info("AFTER FOR NAME org.apache.jasper.servlet.JspServlet");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        if(hasFailedOnce) {
            throw new RuntimeException("Tomcat never started. You have to check error on first try and fix it.");
        }

        if(started) {
            return;
        }

        waitStartThread = Thread.currentThread();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LOG.info("Démarrage de Tomcat.");
                    tomcat.start();

                    started = true;
                    interruptStartWaitingThread();
                    LOG.info("Tomcat est démarré.");
                    tomcat.getServer().await();
                    LOG.info("Tomcat est arrêté.");
                    started = false;
                } catch (LifecycleException e) {
                    started = false;
                    hasFailedOnce = true;
                    interruptStartWaitingThread();
                    throw new RuntimeException("Tomcat failed starting.", e);
                }
            }
        }).start();

        while(true) {
            try {
                Thread.sleep(1000);
                if(Thread.currentThread().isInterrupted()) {
                    break;
                }
            } catch (InterruptedException e) {
                break;
            }
        }

        if(hasFailedOnce) {
            throw new RuntimeException("Echec du démarrage de Tomcat ou du contexte. Vérifiez les stacktrace des autres Thread.");
        }

        LOG.info("Tomcat démarré et l'application de test est prête.");
    }

    private void interruptStartWaitingThread() {
        if(waitStartThread != null) {
            if(waitStartThread.isAlive()) {
                waitStartThread.interrupt();
            }
            waitStartThread = null;
        }
    }

    public int getPort() {
        return tomcat.getConnector().getLocalPort();
    }
}
