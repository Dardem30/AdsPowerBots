//package refresh;
//
//import bo.PayeerAccount;
//import bo.VkAccount;
//import da.DbConnection;
//import org.apache.commons.lang3.StringUtils;
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.openqa.selenium.By;
//import org.openqa.selenium.WebElement;
//import org.openqa.selenium.chrome.ChromeDriver;
//import org.openqa.selenium.chrome.ChromeOptions;
//import org.openqa.selenium.logging.LogEntries;
//import org.openqa.selenium.logging.LogEntry;
//import org.openqa.selenium.logging.LogType;
//import org.openqa.selenium.logging.LoggingPreferences;
//import org.openqa.selenium.remote.CapabilityType;
//import org.openqa.selenium.support.ui.ExpectedConditions;
//import org.openqa.selenium.support.ui.WebDriverWait;
//
//import java.awt.*;
//import java.awt.event.InputEvent;
//import java.awt.event.KeyEvent;
//import java.time.Duration;
//import java.util.logging.Level;
//
//public class ReacuirePayeerSession {
//    public static void main(String[] args) throws Exception {
//        final DbConnection dbConnection = DbConnection.getInstance();
//        final Robot robot = new Robot();
//        clickOn(216, 1426, robot);
//        for (final PayeerAccount payeerAccount : dbConnection.getLostPayeers()) {
//            try {
//                final VkAccount vkAccount = payeerAccount.getVkAccount();
//                final String proxyUrl = vkAccount.getProxy();
//                final String proxyPort = vkAccount.getPort();
//                final String proxyPassword = vkAccount.getProxyPassword();
//                final String proxyUsername = vkAccount.getProxyUsername();
//                Thread.sleep(300);
//                clickOn(379, 700, robot);
//                clickOn(379, 700, robot);
//                clickOn(379, 700, robot);
//                typeString(proxyUrl, robot);
//                cleanProxy(robot);
//                Thread.sleep(300);
//                typeString(proxyPort, robot);
//                Thread.sleep(500);
//                clickOn(374, 948, robot);
//                Thread.sleep(500);
//                clickOn(374, 948, robot);
//                clickOn(374, 948, robot);
//                Thread.sleep(1500);
//                final ChromeOptions options = new ChromeOptions();
//                options.setExperimentalOption("useAutomationExtension", false);
//                final LoggingPreferences logPrefs = new LoggingPreferences();
//                logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
//                options.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
//                final ChromeDriver driver = new ChromeDriver(options);
//                driver.get("https://payeer.com/ru/auth/");
//                typeString(proxyUsername, robot);
//                robot.keyPress(KeyEvent.VK_TAB);
//                typeString(proxyPassword, robot);
//                pressEnter(robot);
//                waitUntilElementWillBePresent(driver, "input[@name='email']").sendKeys(payeerAccount.getAccName());
//                waitUntilElementWillBePresent(driver, "input[@name='password']").sendKeys(payeerAccount.getPassword());
//                Thread.sleep(1500);
//                waitUntilElementWillBePresent(driver, "button[@class='login-form__login-btn step1']").click();
//                waitUntilElementWillBePresentWithoutTimeout(driver, "li[@class='add']");
//                Thread.sleep(1000);
//                final LogEntries payeerLogs = driver.manage().logs().get("performance");
//                for (final LogEntry entry : payeerLogs) {
//                    final JSONObject json = new JSONObject(entry.getMessage());
//                    final JSONObject message = json.getJSONObject("message");
//                    final String method = message.getString("method");
//
//                    if ("Network.responseReceived".equals(method)) {
//                        final JSONObject params = message.getJSONObject("params");
//
//                        final JSONObject response = params.getJSONObject("response");
//                        final String messageUrl = response.getString("url");
//
//                        if (messageUrl.contains("https://payeer.com/bitrix/components/payeer/account.info2/templates/top2/ajax.php")) {
//                            payeerAccount.setSessionId(messageUrl.split("sessid=")[1].split("&")[0]);
//                            final String requestId = params.getString("requestId");
//                            for (final LogEntry reqEntry : payeerLogs) {
//                                final JSONObject reqJson = new JSONObject(reqEntry.getMessage());
//                                final JSONObject reqMessage = reqJson.getJSONObject("message");
//                                final JSONObject reqParams = reqMessage.getJSONObject("params");
//                                try {
//                                    final String reqId = reqParams.getString("requestId");
//                                    if (requestId.equals(reqId)) {
//                                        final JSONObject headers = reqParams.getJSONObject("headers");
//                                        try {
//                                            payeerAccount.setCookies((String) headers.get("Cookie"));
//                                            break;
//                                        } catch (Exception e) {
//                                            payeerAccount.setCookies((String) headers.get("cookie"));
//                                            break;
//                                        }
//                                    }
//                                } catch (JSONException ignored) {
//                                }
//                            }
//                            break;
//                        }
//                    }
//                }
//                dbConnection.saveSessionForPayeer(payeerAccount.getCookies(), payeerAccount.getSessionId(), payeerAccount.getId());
//                driver.quit();
//                Thread.sleep(1000);
//            } catch (Exception e) {
//                System.out.println("FAiled to retrive session for " + payeerAccount.getId());
//                e.printStackTrace();
//            }
//        }
//    }
//    private static void pressEnter(final Robot robot) {
//        robot.keyPress(KeyEvent.VK_ENTER);
//        robot.keyRelease(KeyEvent.VK_ENTER);
//    }
//    private static WebElement waitUntilElementWithTextWillBePresent(final ChromeDriver driver, final String tag, final String text) {
//        final By searchCriteria = By.xpath("//" + tag + "[text()='" + text + "']");
//        new WebDriverWait(driver, Duration.ofSeconds(60)).until(ExpectedConditions.presenceOfElementLocated(searchCriteria));
//        return driver.findElement(searchCriteria);
//
//    }
//    private static WebElement waitUntilElementWithTextWillBePresentWithoutTimeout(final ChromeDriver driver, final String tag, final String text) {
//        final By searchCriteria = By.xpath("//" + tag + "[text()='" + text + "']");
//        new WebDriverWait(driver, Duration.ofSeconds(1200)).until(ExpectedConditions.presenceOfElementLocated(searchCriteria));
//        return driver.findElement(searchCriteria);
//
//    }
//
//    private static WebElement waitUntilElementWillBePresent(final ChromeDriver driver, final String xpath) throws Exception {
//        final By searchCriteria = By.xpath("//" + xpath);
//        new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.presenceOfElementLocated(searchCriteria));
//        Thread.sleep(2000);
//        return driver.findElement(searchCriteria);
//    }
//
//    private static WebElement waitUntilElementWillBePresentWithoutTimeout(final ChromeDriver driver, final String xpath) throws Exception {
//        final By searchCriteria = By.xpath("//" + xpath);
//        new WebDriverWait(driver, Duration.ofSeconds(1200)).until(ExpectedConditions.presenceOfElementLocated(searchCriteria));
//        Thread.sleep(2000);
//        return driver.findElement(searchCriteria);
//    }
//    private static void clickOn(final int x, final int y, final Robot robot) throws Exception {
//        robot.mouseMove(x, y);
//        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
//        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
//        Thread.sleep(150);
//    }
//    private static void cleanProxy(Robot robot) throws Exception {
//        clickOn(609, 699, robot);
//        Thread.sleep(400);
//        clickOn(609, 699, robot);
//    }
//    private static void typeString(String string, final Robot robot) throws Exception {
//
//        //Looping through every char
//        for (int i = 0; i < string.length(); i++) {
//            //Getting current char
//            char c = string.charAt(i);
//            if (StringUtils.isEmpty(String.valueOf(c))) {
//                return;
//            }
//            try {
//                //Pressing shift if it's uppercase
//                if (Character.isUpperCase(c)) {
//                    robot.keyPress(KeyEvent.VK_SHIFT);
//                }
//
//                if (c == '@') {
//                    robot.keyPress(KeyEvent.VK_SHIFT);
//                    robot.keyPress(KeyEvent.VK_2);
//                    robot.keyRelease(KeyEvent.VK_2);
//                    robot.keyRelease(KeyEvent.VK_SHIFT);
//                } else if (c == ':') {
//                    robot.keyPress(KeyEvent.VK_SHIFT);
//                    robot.keyPress(KeyEvent.VK_SEMICOLON);
//                    robot.keyRelease(KeyEvent.VK_SEMICOLON);
//                    robot.keyRelease(KeyEvent.VK_SHIFT);
//                } else if (c == '?') {
//                    robot.keyPress(KeyEvent.VK_SHIFT);
//                    robot.keyPress(KeyEvent.VK_SLASH);
//                    robot.keyRelease(KeyEvent.VK_SLASH);
//                    robot.keyRelease(KeyEvent.VK_SHIFT);
//                } else {
//                    //Actually pressing the key
//                    robot.keyPress(Character.toUpperCase(c));
//                    robot.keyRelease(Character.toUpperCase(c));
//                }
//
//
//                //Releasing shift if it's uppercase
//                if (Character.isUpperCase(c)) {
//                    robot.keyRelease(KeyEvent.VK_SHIFT);
//                }
//            } catch (Exception e) {
//
//            }
//            //Optional delay to make it look like it's a human typing
//            Thread.sleep(50);
//        }
//    }
//}
