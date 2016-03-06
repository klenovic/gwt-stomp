package com.codeveo.gwt.stomp.testing;

import org.junit.rules.ExternalResource;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TomcatSeleniumRule extends ExternalResource {

    private final static Logger LOG = LoggerFactory.getLogger(TomcatSeleniumRule.class);

    private final static TomcatSeleniumRule INSTANCE = new TomcatSeleniumRule();

    private final TomcatManager tomcatManager = TomcatManager.getInstance();

    private WebDriver webDriver;

    private TomcatSeleniumRule() {
        // EMPTY
    }

    public static TomcatSeleniumRule getInstance() {
        return INSTANCE;
    }

    private void initWebDriver() {
        if(webDriver != null) {
            return;
        }

        LOG.info("Lancement du WebDriver.");
        webDriver = new FirefoxDriver();
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                LOG.info("Fermeture du WebDriver.");
                webDriver.close();
            }
        }));
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        tomcatManager.start();
        initWebDriver();
    }

    @Override
    protected void after() {
        super.after();
    }

    public int getPort() {
        return tomcatManager.getPort();
    }

    public WebDriver getDriver() {
        return webDriver;
    }
}
