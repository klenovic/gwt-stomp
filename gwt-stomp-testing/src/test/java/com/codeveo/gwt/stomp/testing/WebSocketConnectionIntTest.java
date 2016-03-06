package com.codeveo.gwt.stomp.testing;

import org.fest.util.Strings;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Objects;

import static org.fest.assertions.api.Assertions.assertThat;

public class WebSocketConnectionIntTest {

    public static final int MAX_WAIT_SEC = 3;

    public static final String STOMP_JS_ERR_MSG_PREFIX = "Whoops! Lost connection to";

    @Rule
    public TomcatSeleniumRule rule = TomcatSeleniumRule.getInstance();

    private String getIndexHttpUrl() {
        return "http://localhost:" + rule.getPort() + "/testApp/index.html";
    }

    private String getWebsocketEndpoint() {
        return "ws://localhost:" + rule.getPort() + "/testApp/stomp/";
    }

    private void changeTestPage(String testId) {
        final String currentUrl = rule.getDriver().getCurrentUrl();

        if(Strings.isNullOrEmpty(currentUrl) || !currentUrl.startsWith(getIndexHttpUrl())) {
            rule.getDriver().get(getIndexHttpUrl());
        }

        final WebDriverWait wait = new WebDriverWait(rule.getDriver(), MAX_WAIT_SEC);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("gwt-loading-status")));

        String previousTestId = null;
        try {
            WebElement testIdElem = rule.getDriver().findElement(By.id("test-id"));
            previousTestId = testIdElem.getText();
        } catch (NoSuchElementException e) {
            // EMPTY
        }

        if(!Objects.equals(previousTestId, testId)) {
            rule.getDriver().navigate().to(getIndexHttpUrl() + "#" + testId);
        }

        WebElement testIdElem = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("test-id")));
        wait.until(ExpectedConditions.textToBePresentInElement(testIdElem, testId));
        WebElement resetButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("test-reset-button")));
        resetButton.click();
        wait.until(ExpectedConditions.textToBePresentInElement(testIdElem, testId));
    }

    /**
     * Nominal connection test case : try to connect and then disconnect.
     */
    @Test
    public void testConnectionIsOk() {
        changeTestPage("websocketConnectionTest");

        final WebDriverWait wait = new WebDriverWait(rule.getDriver(), MAX_WAIT_SEC);
        final WebElement wsUrlInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("websocket-url-input")));
        wsUrlInput.sendKeys(getWebsocketEndpoint());
        final WebElement wsUrlOutput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("websocket-url")));
        final WebElement statusLabel = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("connection-status-label")));
        final WebElement connectBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("connect-button")));
        final WebElement disconnectBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("disconnect-button")));
        wait.until(ExpectedConditions.textToBePresentInElement(statusLabel, "INIT"));
        connectBtn.click();
        wait.until(ExpectedConditions.textToBePresentInElement(wsUrlOutput, getWebsocketEndpoint()));
        wait.until(ExpectedConditions.textToBePresentInElement(statusLabel, "CONNECTED"));
        disconnectBtn.click();
        wait.until(ExpectedConditions.textToBePresentInElement(statusLabel, "DISCONNECTED"));
    }

    /**
     * Test error callback when trying to open websocket on closed TCP/IP port.<br/>
     * In this case, error callback is executed by stomp.js with specific message "Whoops! Lost connection to ${uri}"
     */
    @Test
    public void testConnectionOnNotOpenedPortIsKo() throws IOException {
        changeTestPage("websocketConnectionTest");

        ServerSocket serverSocket = new ServerSocket(0);
        int freshPort = serverSocket.getLocalPort();
        assertThat(freshPort).isGreaterThan(0);
        serverSocket.close();

        final WebDriverWait wait = new WebDriverWait(rule.getDriver(), 10); // Extra time because web browser make a long time before considering connection failed
        final WebElement wsUrlInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("websocket-url-input")));
        wsUrlInput.sendKeys("ws://localhost:" + freshPort + "/stomp/");
        final WebElement errorCause = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("error-cause")));
        final WebElement wsUrlOutput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("websocket-url")));
        final WebElement statusLabel = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("connection-status-label")));
        final WebElement connectBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("connect-button")));
        wait.until(ExpectedConditions.textToBePresentInElement(statusLabel, "INIT"));
        connectBtn.click();
        wait.until(ExpectedConditions.textToBePresentInElement(wsUrlOutput, "ws://localhost:" + freshPort + "/stomp/"));
        wait.until(ExpectedConditions.textToBePresentInElement(statusLabel, "FAILED"));
        wait.until(ExpectedConditions.textToBePresentInElement(errorCause, STOMP_JS_ERR_MSG_PREFIX));
    }

    /**
     * Test error callback when trying to open websocket with wrong URI path.<br/>
     * In this case, error callback is executed by stomp.js with specific message "Whoops! Lost connection to ${uri}"
     */
    @Test
    public void testConnection404ErrorIsKo() {
        changeTestPage("websocketConnectionTest");

        final WebDriverWait wait = new WebDriverWait(rule.getDriver(), MAX_WAIT_SEC);
        final WebElement wsUrlInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("websocket-url-input")));
        wsUrlInput.sendKeys(getWebsocketEndpoint()+"notfound/");
        final WebElement errorCause = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("error-cause")));
        final WebElement wsUrlOutput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("websocket-url")));
        final WebElement statusLabel = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("connection-status-label")));
        final WebElement connectBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("connect-button")));
        wait.until(ExpectedConditions.textToBePresentInElement(statusLabel, "INIT"));
        connectBtn.click();
        wait.until(ExpectedConditions.textToBePresentInElement(wsUrlOutput, getWebsocketEndpoint() + "notfound/"));
        wait.until(ExpectedConditions.textToBePresentInElement(statusLabel, "FAILED"));
        wait.until(ExpectedConditions.textToBePresentInElement(errorCause, STOMP_JS_ERR_MSG_PREFIX));
    }

    /**
     * Test error callback when trying to open websocket with empty URI<br/>
     * In this case, error callback is executed the GWT wrapper. Error message will depend on WebSocket web browser implementation,
     * so it cannot be tested.
     */
    @Test
    public void testConnectionEmptyUrl() {
        changeTestPage("websocketConnectionTest");

        final WebDriverWait wait = new WebDriverWait(rule.getDriver(), MAX_WAIT_SEC);
        final WebElement errorCause = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("error-cause")));
        final WebElement wsUrlOutput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("websocket-url")));
        final WebElement statusLabel = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("connection-status-label")));
        final WebElement connectBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("connect-button")));
        wait.until(ExpectedConditions.textToBePresentInElement(statusLabel, "INIT"));
        connectBtn.click();
        wait.until(ExpectedConditions.textToBePresentInElement(wsUrlOutput, ""));
        wait.until(ExpectedConditions.textToBePresentInElement(statusLabel, "FAILED"));
        assertThat(errorCause.getText()).isNotEmpty();
    }

    /**
     * Test error callback when trying to open websocket with wrong URI scheme like http<br/>
     * In this case, error callback is executed the GWT wrapper. Error message will depend on WebSocket web browser implementation,
     * so it cannot be tested.
     */
    @Test
    public void testConnectionOnWrongScheme() {
        changeTestPage("websocketConnectionTest");

        final WebDriverWait wait = new WebDriverWait(rule.getDriver(), MAX_WAIT_SEC);
        final WebElement wsUrlInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("websocket-url-input")));
        wsUrlInput.sendKeys(getIndexHttpUrl());
        final WebElement errorCause = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("error-cause")));
        final WebElement wsUrlOutput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("websocket-url")));
        final WebElement statusLabel = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("connection-status-label")));
        final WebElement connectBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("connect-button")));
        wait.until(ExpectedConditions.textToBePresentInElement(statusLabel, "INIT"));
        connectBtn.click();
        wait.until(ExpectedConditions.textToBePresentInElement(wsUrlOutput, getIndexHttpUrl()));
        wait.until(ExpectedConditions.textToBePresentInElement(statusLabel, "FAILED"));
        assertThat(errorCause.getText()).isNotEmpty();
    }

    /**
     * Test that connection is established. Then send message to server to request it to close websocket.<br/>
     * We are testing that when server side is closing websocket, stomp will trigger error callback.
     */
    @Test
    public void testConnectionLostBecauseOfServer() {
        changeTestPage("websocketConnectionTest");

        final WebDriverWait wait = new WebDriverWait(rule.getDriver(), MAX_WAIT_SEC);
        final WebElement wsUrlInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("websocket-url-input")));
        wsUrlInput.sendKeys(getWebsocketEndpoint());
        final WebElement errorCause = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("error-cause")));
        final WebElement wsUrlOutput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("websocket-url")));
        final WebElement statusLabel = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("connection-status-label")));
        final WebElement connectBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("connect-button")));
        final WebElement sendBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("send-button")));
        final WebElement destinationInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("destination-input")));
        wait.until(ExpectedConditions.textToBePresentInElement(statusLabel, "INIT"));
        connectBtn.click();
        wait.until(ExpectedConditions.textToBePresentInElement(wsUrlOutput, getWebsocketEndpoint()));
        wait.until(ExpectedConditions.textToBePresentInElement(statusLabel, "CONNECTED"));
        destinationInput.sendKeys("/app/close-connection");
        sendBtn.click();
        wait.until(ExpectedConditions.textToBePresentInElement(statusLabel, "FAILED"));
        wait.until(ExpectedConditions.textToBePresentInElement(errorCause, STOMP_JS_ERR_MSG_PREFIX));
    }
}
