import bo.*;
import da.DbConnection;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import service.RuCaptcha;
import service.VkService;
import service.smsActivation.SmsActivate;
import util.ExecuteService;
import util.ProxyCustomizer;
import util.ThrowableFunction;

import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static util.SeleniumUtils.*;

public class AdsPowerService {
    private static final String BASE_URL = "http://local.adspower.com:50325/";
    private static final Map<Integer, Map<String, Integer>> failedTasks = new HashMap<>();
    private static final DbConnection dbConnection = DbConnection.getInstance();
    private static final RestTemplate localRestTemplate = new RestTemplate();

    //15000000 ~ 1h 10min
    public static void main(String[] args) throws Exception {
        System.setProperty("webdriver.http.factory", "jdk-http-client");
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        final VkService vkService = VkService.getInstance();
        //    final TimeZone timeZone = TimeZone.getDefault();
        //   int rawOffset = timeZone.getRawOffset();
        final ExecuteService executeService = ExecuteService.getInstance();
        while (true) {
            final Date date = new Date();
            final boolean isNight = date.getHours() < 10 || date.getHours() >= 23;
//            final boolean isNight = true;
            final List<VkSerfingAccount> allVkSerfingAccounts;
            if (isNight) {
                allVkSerfingAccounts = dbConnection.getAllVkSerfingAccounts().stream()
                        .filter(record -> record.getVkAccount().getPayUpVideoAcc() != null
                                        && !record.getVkAccount().getPayUpVideoAcc().isBlocked()
                                 //      && record.getVkAccount().getId() == 542
                        )
                        .collect(Collectors.toList());
            } else {
                allVkSerfingAccounts = dbConnection.getAllVkSerfingAccounts();
            }
            final Date startExecution = new Date();
            executeService.execute(allVkSerfingAccounts, (accounts, index) -> {
                try {
                    // Thread.sleep(1000L * (index - 1));
//                    Thread.sleep((long) getRandomNumber(10000, 20000) * (index - 1));
                    Thread.sleep((long) getRandomNumber(60000, 120000) * (index - 1));
//                    Thread.sleep((long) getRandomNumber(30000, 60000) * (index - 1));
//                    Thread.sleep((long) 2000 * (index - 1));
                } catch (Exception e) {

                }
                for (int accIndex = 0; accIndex < accounts.size(); accIndex++) {
                    final VkSerfingAccount account = accounts.get(accIndex);
                    final VkAccount vkAccount = account.getVkAccount();
//                    if (vkAccount.getId() != 535
//                    ) {
//                        continue;
//                    }
                    vkAccount.setAvailableSubscribes(getRandomNumber(1, 5));
                    // vkAccount.setAvailableSubscribes(0);
                    //   vkAccount.setAvailableShares(0);
                    vkAccount.setAvailableFriendRequests(getRandomNumber(-10, 1));
                    vkAccount.setAvailableShares(getRandomNumber(-5, 1));
                    //   vkAccount.setAvailableFriendRequests(getRandomNumber(0, 1));
                    try {

                        final ChromeDriver driver;
                        final Date now = new Date();
                        final RestTemplate restTemplate = ProxyCustomizer.buildRestTemplate(vkAccount);
                        vkAccount.setRestTemplate(restTemplate);
                        try {
                            final String accParams;
                            if (isNight) {
                                accParams = localRestTemplate.exchange(BASE_URL + "/api/v1/browser/start?user_id=" + vkAccount.getBrowserProfile()
                                        + "&ip_tab=0&headless=0&open_tabs=1", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class).getBody();
                            } else {
                                accParams = localRestTemplate.exchange(BASE_URL + "/api/v1/browser/start?user_id=" + vkAccount.getBrowserProfile()
                                        + "&ip_tab=0&headless=1&open_tabs=1", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class).getBody();
                            }
                            System.out.println(accParams);
                            final JSONObject accParamsJson = new JSONObject(accParams);
                            final JSONObject seleniumConnectionParams = accParamsJson.getJSONObject("data");
                            System.setProperty("webdriver.chrome.driver", seleniumConnectionParams.getString("webdriver")); // Set the webdriver returned by the API
                            final ChromeOptions options = new ChromeOptions();
                            options.setPageLoadStrategy(PageLoadStrategy.NONE);
                            options.setExperimentalOption("debuggerAddress", seleniumConnectionParams.getJSONObject("ws").getString("selenium"));
                            final LoggingPreferences logPrefs = new LoggingPreferences();
                            logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
                            options.setCapability("goog:loggingPrefs", logPrefs);
                            driver = new ChromeDriver(options);
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Failed to start browser for " + vkAccount.getId());
                            try {
                                localRestTemplate.exchange(BASE_URL + "/api/v1/browser/stop?user_id=" + vkAccount.getBrowserProfile(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            accIndex--;
                            try {
                                Thread.sleep(getRandomNumber(500, 1000));
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                            continue;
                        }
//                        vkAccount.setAvailableShares(3);
//                        vkAccount.setAvailableFriendRequests(5);
//                        vkAccount.setAvailableSubscribes(10);
//                        if (account.getVkAccount().getId() == 13) {
//                            continue;
//                        }
//                        if (account.getVkAccount() == null || (account.getCoolDown() != null && now.getTime() < account.getCoolDown().getTime() - rawOffset)
//                        ) {
//                            continue;
//                        }
                        System.out.println("Start processing acc " + vkAccount.getId() + " " + now);
//                    if (1 <= now.getHours() && now.getHours() < 11) {
//                        continue;
//                    }
//                    if (now.getHours() == 11 && now.getMinutes() <= getRandomNumber(1, 30)) {
//                        continue;
//                    }

                        // System.out.println(driver.manage().window().getSize());

                        //TEST section
//                        driver.get("https://vk.com/public211261465");
//                        final TaskInfo taskInfo = new TaskInfo();
//                        scanCookiesFromCurrentPage(driver, vkAccount, taskInfo);
//                        vkService.subscribe(vkAccount, "https://vk.com/public211261465", restTemplate, driver, taskInfo.getProfileHtml());
//                        System.out.printf("");

                        // test blocked
                        if (true) {
                            try {
                                driver.get("https://vk.com/");
                                final String blockMessage = waitUntilElementWillBePresent(driver, "div[@id='login_blocked_wrap']", 5).getAttribute("innerText");
                                DbConnection.getInstance().markVkAccAsBlocked(vkAccount.getId(), "1");
                                System.out.println("Vk acc " + vkAccount.getId() + " is blocked for " + blockMessage);
                                continue;
                            } catch (Exception e) {
                                localRestTemplate.exchange(BASE_URL + "/api/v1/browser/stop?user_id=" + vkAccount.getBrowserProfile(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
                                driver.quit();
                                continue;
                            }
                        }

                        if (!isNight) {
                            boolean skip = false;
                            while (true) {
                                try {
                                    if (!vkService.refreshCookie(driver, vkAccount, restTemplate, false)) {
                                        localRestTemplate.exchange(BASE_URL + "/api/v1/browser/stop?user_id=" + vkAccount.getBrowserProfile(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
                                        skip = true;
                                    }
                                    break;
                                } catch (HttpTimeoutException e) {
                                    System.out.println(e.getClass().getSimpleName() + " - going to retry refresh cookie");
                                }
                            }
                            if (skip) {
                                continue;
                            }
                        }

                        final AtomicBoolean jobIsFinished = new AtomicBoolean(false);
                        final Thread execution = new Thread(() -> {
                            try {
                                final List<Runnable> operations = new ArrayList<>(3);
                                if (isNight) {
                                    if (now.getHours() < 6) {
                                        try {
                                            processPayUpVideoAcc(driver, account);
                                        } catch (Exception e) {
                                            System.out.println("Failed to process payup acc [" + account.getVkAccount().getId() + "]");
                                            e.printStackTrace();
                                        }
                                    }
                                } else {
                                    if (account.getBalanceVkTarget() != -1 && getRandomNumber(0, 100) > 70) {
                                        operations.add(() -> {
                                            try {
                                                processVkTargetTaskList(driver, dbConnection, account, vkService, restTemplate);
                                                Thread.sleep(getRandomNumber(2000, 4000));
                                            } catch (Exception ignored) {
                                                ignored.printStackTrace();
                                            }
                                        });
                                        operations.add(() -> {
                                            try {
                                                processVLikeTaskList(driver, dbConnection, account, vkService, restTemplate, false);
                                                Thread.sleep(getRandomNumber(2000, 4000));
                                            } catch (Exception ignored) {
                                                ignored.printStackTrace();
                                            }
                                        });
                                    } else {
                                        operations.add(() -> {
                                            try {
                                                processVLikeTaskList(driver, dbConnection, account, vkService, restTemplate, true);
                                                Thread.sleep(getRandomNumber(2000, 4000));
                                            } catch (Exception ignored) {
                                                ignored.printStackTrace();
                                            }
                                        });
                                    }
                                    Collections.shuffle(operations);
                                    if (account.getBalance() != -1) {
                                        operations.add(() -> {
                                            try {
                                                processVkSerfingTaskListV2(driver, dbConnection, account, vkService, restTemplate, true);
                                                Thread.sleep(getRandomNumber(2000, 4000));
                                            } catch (Exception ignored) {
                                                ignored.printStackTrace();
                                            }
                                        });
                                    }
                                }
                                for (final Runnable operation : operations) {
                                    operation.run();
                                    closeAllTabsExceptFirstOne(driver);
                                }
                                setAccCooldown(dbConnection, account, null, 15000000 + getRandomNumber(1200000, 2400000));
                                localRestTemplate.exchange(BASE_URL + "/api/v1/browser/stop?user_id=" + vkAccount.getBrowserProfile(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
                                driver.quit();
                                jobIsFinished.set(true);
                            } catch (Exception e) {
                                e.printStackTrace();
                                jobIsFinished.set(true);
                            }
                        });
                        execution.start();
                        int tick = 0;
                        while (!jobIsFinished.get()) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            tick++;
                            if (isNight && tick > 1200) {
                                execution.interrupt();
                                driver.quit();
                                localRestTemplate.exchange(BASE_URL + "/api/v1/browser/stop?user_id=" + vkAccount.getBrowserProfile(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
                                System.out.println("Processing vk acc [" + vkAccount.getId() + "] took more than 20 min!!!!!");
                                jobIsFinished.set(true);
                            } else if (tick > 1800) {
                                execution.interrupt();
                                driver.quit();
                                localRestTemplate.exchange(BASE_URL + "/api/v1/browser/stop?user_id=" + vkAccount.getBrowserProfile(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
                                System.out.println("Processing vk acc [" + vkAccount.getId() + "] took more than 30 min!!!!!");
                                jobIsFinished.set(true);
                            }

                        }
                        //Thread.sleep(getRandomNumber(60000, 120000));
                    } catch (Exception e) {
                        System.out.println("Failed to process tasks for " + vkAccount.getId());
                        e.printStackTrace();
                        try {
                            localRestTemplate.exchange(BASE_URL + "/api/v1/browser/stop?user_id=" + vkAccount.getBrowserProfile(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
                        } catch (Exception ex) {

                        }
                    }
                }
            }, 5);
                   // }, 999);
            System.out.println("Finished processing all accs at " + new Date() + " started at " + startExecution);
//            System.exit(0);
//            if (true) {
//                return;
//            }


            for (VkSerfingAccount account : allVkSerfingAccounts) {
                localRestTemplate.exchange(BASE_URL + "/api/v1/browser/stop?user_id=" + account.getVkAccount().getBrowserProfile(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
                Thread.sleep(1000);
            }
            if (isNight) {
                Thread.sleep(getRandomNumber(3600000, (int) (3600000 * 1.5))); //1-1.5h
            }
            //Thread.sleep(getRandomNumber(3600000, 7200000)); //1-2h
            // Thread.sleep(getRandomNumber(1200000, 2400000)); //20-40min
            //  Thread.sleep(getRandomNumber(14400000, 15400000)); //4-4.33h
        }
    }

    private static Long sleepTime(int initialSleep, final TaskInfo taskInfo) {
        if (taskInfo == null || taskInfo.getExecuteTime() == null) {
            return (long) initialSleep;
        }
        if (taskInfo.getExecuteTime() > initialSleep) {
            return 0L;
        }
        return initialSleep - taskInfo.getExecuteTime();
    }

    private static void processVkTargetTaskList(final ChromeDriver driver,
                                                final DbConnection dbConnection,
                                                final VkSerfingAccount account,
                                                final VkService vkService,
                                                RestTemplate restTemplate) throws Exception {
        driver.get("https://vktarget.ru/list/");
        Thread.sleep(1500);
        try {
            WebElement loginButton = waitUntilElementWillBePresent(driver, "a[@href='/login/']");
            System.out.println("Account " + account.getVkAccount().getId() + " unauthorized going to relogin");
            try {
                clickElement(driver, loginButton);
                Thread.sleep(getRandomNumber(1000, 1500));
                clickAndFillField(driver, "input[@name='email']", account.getEmail());
                clickAndFillField(driver, "input[@name='password']", account.getPassword());
                clickElement(driver, "button[@data-login='login']");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Account " + account.getVkAccount().getId() + " failed to login!!!!!");
                return;
            }
//            WebElement emailField = waitUntilElementWillBePresent(driver, "input[@name='email']");
//            clickButton(driver, emailField);
//            Thread.sleep(getRandomNumber(300, 500));
//            emailField.sendKeys(account.getEmail());
//            Thread.sleep(getRandomNumber(1000, 1500));
//            WebElement passwordField = driver.findElement(By.xpath("//input[@name='password']"));
//            clickButton(driver, passwordField);
//            Thread.sleep(getRandomNumber(300, 500));
//            passwordField.sendKeys(account.getPassword());
//            Thread.sleep(getRandomNumber(500, 900));
//            clickButton(driver, "button[@data-login='login']");
//            Thread.sleep(getRandomNumber(1500, 2500));
        } catch (Exception ignored) {

        }
        updateVkTargetBalance(driver, dbConnection, account);
        WebElement availableTasksTable = driver.findElement(By.xpath("//div[@data-id='available_table']"));
        try {
            clickElement(driver, findElement(driver, "button[@onclick='hide_guide()']"));
        } catch (Exception e) {
        }
        int index = 0;
        int failesInRow = 0;
        float currentBalance = 0;
        final int amountOfChecks = getRandomNumber(180, 300);
        final List<String> processedHrefs = new ArrayList<>();
        int amountOfAvailableScenarious = getRandomNumber(0, 3);
        while (index < amountOfChecks) {
            if (failesInRow > 2) {
                System.out.println("VKtarget fails in row more than 2 for acc " + account.getVkAccount().getId());
                break;
            }
            String currentLink = null;
            try {
                List<WebElement> tasks = availableTasksTable.findElements(By.xpath("./div[@data-bind='task_item']/div[@class='col-12 col-md link__col flex-middle']/div[@class='wrap']"));
                final VkAccount vkAccount = account.getVkAccount();
                for (final WebElement taskTag : tasks) {
                    final String taskTagContent = taskTag.getAttribute("innerHTML");
                    if (taskTagContent.contains("vk.com")) {
                        final String link = taskTagContent.split("href=\"")[1].split("\"")[0];
                        if (processedHrefs.contains(link)) {
                            continue;
                        } else {
                            processedHrefs.add(link);
                        }
                        if (dbConnection.isLinkBlackListed(link)) {
                            continue;
                        }
                        final String taskId = taskTagContent.split("data-tid=\"")[1].split("\"")[0];
                        try {

                            final String typeName = taskTagContent.split("<span data-bind=\"link_text\">")[1].split("</span>")[0];
//                            if (getRandomNumber(0, 100) > 50) {
//                                System.out.println("Account vktarget " + account.getVkAccount().getId() + " will check help");
//                                //<div class="control__item help" data-bind="instrunction_btn" data-info-id="14833416"> <div class="sprite__background info dark" data-night-svg=""></div> </div>
//                                clickButton(driver, "div[@data-bind='instrunction_btn][@data-info-id='" + taskId + "']");
//                            }
                            final WebElement startTaskButton;
                            if (getRandomNumber(0, 100) > 70) {
                                //     System.out.println("Account vktarget " + account.getVkAccount().getId() + " is going to missclick");
                                startTaskButton = missClick(driver, "a[@href='" + link + "']");
                            } else {
                                startTaskButton = waitUntilElementWillBePresent(driver, "a[@href='" + link + "']");
                            }
                            clickElement(driver, startTaskButton);
                            waitUntilNumbersOfWindows(driver, 300, 2);
                            ArrayList<String> windows = new ArrayList<>(driver.getWindowHandles());
                            driver.switchTo().window(windows.get(1));
                            TaskInfo taskInfo = scanCookiesFromCurrentPage(driver, account.getVkAccount(), null);
                            String profileHtml = taskInfo.getProfileHtml();
                            restTemplate = ProxyCustomizer.buildRestTemplate(vkAccount);
                            boolean result = false;
                            if (typeName.contains("Поставьте лайк на")) {
                                if (link.contains("/photo")) {
                                    result = vkService.likePhoto(vkAccount, link, restTemplate, profileHtml);
                                } else if (link.contains("/clip")) {
                                    result = vkService.likeClip(vkAccount, link, restTemplate, profileHtml);
                                } else if (link.contains("/video")) {
                                    result = vkService.likeVideo(vkAccount, link, restTemplate, profileHtml);
                                } else if (link.contains("reply=")) {
                                    result = vkService.likeReply(vkAccount, link, restTemplate, profileHtml);
                                } else {
                                    result = vkService.like(vkAccount, link, restTemplate, profileHtml);
                                }
                                Thread.sleep(sleepTime(getRandomNumber(3000, 5000), taskInfo));
                            } else if (typeName.contains("Добавить в")) {
                                if (vkAccount.isLimitForFriendRequestsIsOver()
                                        || vkAccount.isCantAddFriends()
                                        || dbConnection.isLinkOnCooldown(link)
                                ) {
                                    driver.close();
                                    driver.switchTo().window(windows.get(0));
                                    continue;
                                }
                                result = vkService.addFriend(vkAccount, link, restTemplate, profileHtml);
                                if (result) {
                                    //    processSubscription(link, vkAccount.getId());
                                    vkAccount.setAvailableFriendRequests(vkAccount.getAvailableFriendRequests() - 1);
                                    dbConnection.updateVkLimitsForFriendRequests(vkAccount);
                                }
                                Thread.sleep(sleepTime(getRandomNumber(3000, 5000), taskInfo));
                            } else if (typeName.contains("Нажмите")) {
                                if (vkAccount.isLimitForShares() || dbConnection.isLinkOnCooldown(link)) {
                                    driver.close();
                                    driver.switchTo().window(windows.get(0));
                                    continue;
                                }
                                result = vkService.share(vkAccount, link, restTemplate, profileHtml);
                                if (result) {
                                    vkAccount.setAvailableShares(vkAccount.getAvailableShares() - 1);
                                    dbConnection.updateVkLimitsForShares(vkAccount);
                                }
                                Thread.sleep(sleepTime(getRandomNumber(6000, 7000), taskInfo));
                            } else if (typeName.contains("Посмотреть")) {
                                result = vkService.showPost(vkAccount, link, restTemplate, profileHtml);
                                Thread.sleep(sleepTime(getRandomNumber(10000, 15000), taskInfo));
                            } else if (!typeName.contains("Расскажите о")) {
                                if (!dbConnection.isLinkOnCooldown(link) && !vkAccount.isCantSubscribe() && !vkAccount.isLimitForSubscribtions()) {
                                    result = vkService.subscribe(vkAccount, link, restTemplate, driver, profileHtml);
                                    if (result) {
                                        vkAccount.setAvailableSubscribes(vkAccount.getAvailableSubscribes() - 1);
                                        dbConnection.updateVkLimitsForSubscribes(vkAccount);
                                    }
                                } else {
                                    driver.close();
                                    driver.switchTo().window(windows.get(0));
                                    continue;
                                }
                                Thread.sleep(sleepTime(getRandomNumber(3000, 5000), taskInfo));
                            }
                            driver.close();
                            driver.switchTo().window(windows.get(0));
                            currentLink = link;
                            final WebElement submitTaskButton;
                            if (getRandomNumber(0, 100) > 70) {
                                //      System.out.println("Account vktarget " + account.getVkAccount().getId() + " is going to missclick on submit button");
                                submitTaskButton = missClick(driver, "button[@data-check-task='" + taskId + "']");
                            } else {
                                submitTaskButton = waitUntilElementWillBePresent(driver, "button[@data-check-task='" + taskId + "']");
                            }
                            if (result) {
                                clickElement(driver, submitTaskButton);
                                Thread.sleep(getRandomNumber(6000, 9500));
                                Float balance = updateVkTargetBalance(driver, dbConnection, account);
                                if (currentBalance != balance) {
                                    currentBalance = balance;
                                    failesInRow = 0;
                                } else {
                                    failesInRow += 1;
                                }
                                System.out.println("Account vktarget " + account.getVkAccount().getId() + " submitted task " + link + " balance " + balance);
                            } else {
                                skipVktargetTask(driver, taskId);
                            }
                            currentLink = null;
                        } catch (Exception e) {
                            ArrayList<String> windows = new ArrayList<>(driver.getWindowHandles());
                            if (windows.size() > 1) {
                                driver.close();
                                driver.switchTo().window(windows.get(0));
                            }
                            if (!(e instanceof ResourceAccessException)) {
                                e.printStackTrace();
                                skipVktargetTask(driver, taskId);
                            }
                        }
                    }
                }
            } catch (StaleElementReferenceException e) {
                failesInRow += 1;
                System.out.println("Element is refreshed going to retry to accuire for acc " + account.getVkAccount().getId());
                if (currentLink != null) {
                    System.out.println("Going to remove link from processed " + currentLink);
                    processedHrefs.remove(currentLink);
                    currentLink = null;
                }
            }
            index++;
            Thread.sleep(getRandomNumber(800, 1200));
            if (getRandomNumber(0, 100) > 95 && amountOfAvailableScenarious != 0) {
                int scenario = getRandomNumber(0, 3);
                System.out.println("Account vktarget " + account.getVkAccount().getId() + " will execute scenario " + scenario);
                vkTargetScenarious.get(scenario).accept(driver);
                amountOfAvailableScenarious--;
            }
        }
        if (processedHrefs.isEmpty()) {
            driver.get("https://vktarget.ru/list/");
            clickElement(driver, "li[@data-tab-id='errors']");
            if (waitUntilElementWillBePresent(driver, "div[@class='errors__table__wrap']").getAttribute("innerHTML")
                    .contains("К сожалению, ваша учетная запись заблокирована")) {
                System.out.println("Account " + account.getVkAccount().getId() + " blocked by vktarget");
                dbConnection.updateVKTargetBalance(account.getId(), -1);
            }
        }

    }

    private static void skipVktargetTask(final ChromeDriver driver, final String taskId) throws Exception {
        clickElement(driver, "div[@data-task-complain='" + taskId + "']/div");
    }

    private static Float updateVkTargetBalance(final ChromeDriver driver, final DbConnection dbConnection, final VkSerfingAccount account) throws Exception {
        final Float balance = Float.parseFloat(driver.findElement(By.xpath("//span[@data-bind='balance']")).getAttribute("innerText"));
        if (balance >= 20 && getRandomNumber(0, 100) > 97
        ) {
            Date latestInvoice = dbConnection.getLatestInvoice(account.getVkAccount().getId(), "vktarget");
            final Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, -4);
            if (latestInvoice == null || calendar.getTime().after(latestInvoice)) {
                System.out.println("VKtarget account " + account.getVkAccount().getId() + " is going to invoice. Last invoice was on: " + latestInvoice);
                clickElement(driver, waitUntilElementWithTextWillBePresent(driver, "a", "Вывести"));
                Thread.sleep(getRandomNumber(500, 900));
                final PayeerAccount payeerAccount = account.getVkAccount().getPayeerAccount();
                if (latestInvoice == null) { //только на первую выплату нужен код
                    clickElement(driver, waitUntilElementWillBePresent(driver, "label[@for='method_pyr']"));
                    WebElement sumField = waitUntilElementWillBePresent(driver, "input[@id='withdraw_sum']");
                    clickElement(driver, sumField);
                    Thread.sleep(getRandomNumber(200, 450));
                    sumField.sendKeys(String.valueOf(balance).substring(0, 2));
                    Thread.sleep(getRandomNumber(300, 1000));
                    WebElement addressField = waitUntilElementWillBePresent(driver, "input[@id='withdrawals_pr']");
                    clickElement(driver, addressField);
                    Thread.sleep(getRandomNumber(200, 450));
                    addressField.sendKeys(payeerAccount.getAccName());
                    Thread.sleep(getRandomNumber(400, 800));
                    clickElement(driver, waitUntilElementWithTextWillBePresent(driver, "button", "Создать заявку"));
                    try {
                        clickElement(driver, waitUntilElementWillBePresent(driver, "button[@data-confirm-type='code']"));
                        String verificationCode;
                        try {
                            //Вставьте этот код: 124532
                            String mailWithVerificationCode = processMailByTitle(driver, "Код для выплаты на vktarget", mailTab -> findElement(mailTab, "div[@class='letter__body']").getAttribute("innerText"));
                            System.out.println("VkTarget acc " + account.getVkAccount().getId() + " received invoice mail:" + mailWithVerificationCode);
                            verificationCode = mailWithVerificationCode.replaceAll("Вставьте этот код: ", "");
                        } catch (Exception e) {
                            //"Здравствуйте!\n\nВот ваш код для подтверждения заявки на выплату: 40f97e42\n\nС уважением, Команда VkTarget"
                            String mailWithVerificationCode = processMailByTitle(driver, "Код для подтверждение", mailTab -> findElement(mailTab, "div[@class='letter__body']").getAttribute("innerText"));
                            System.out.println("VkTarget acc " + account.getVkAccount().getId() + " received invoice mail:" + mailWithVerificationCode);
                            verificationCode = mailWithVerificationCode.split("Вот ваш код для подтверждения заявки на выплату: ")[1].split("\n")[0];
                        }
                        WebElement codeField = waitUntilElementWillBePresent(driver, "input[@placeholder='Код']");
                        clickElement(driver, codeField);
                        Thread.sleep(300, 600);
                        System.out.println("VkTarget acc " + account.getVkAccount().getId() + " invoice code:" + verificationCode);
                        codeField.sendKeys(verificationCode);
                        clickElement(driver, waitUntilElementWithTextWillBePresent(driver, "button", "Создать заявку"));
                        dbConnection.updatePayeerBalance(payeerAccount.getId(), balance);
                        Thread.sleep(25000);
                        driver.get("https://vktarget.ru/list/");
                        dbConnection.logInvoice(account.getVkAccount().getId(), balance, "vktarget");
                        System.out.println("Vktarget account " + account.getVkAccount().getId() + " successfully invoiced");
                    } catch (Exception e) {
                        System.out.println("Vktarget account " + account.getVkAccount().getId() + " failed to invoice !!!!!");
                        e.printStackTrace();
                    }
                } else {
                    try {
                        WebElement sumField = waitUntilElementWillBePresent(driver, "input[@id='withdraw_sum']");
                        clickElement(driver, sumField);
                        Thread.sleep(getRandomNumber(200, 450));
                        sumField.sendKeys(String.valueOf(balance).substring(0, 2));
                        Thread.sleep(getRandomNumber(300, 1000));
                        clickElement(driver, waitUntilElementWithTextWillBePresent(driver, "button", "Создать заявку"));
                        Thread.sleep(25000);
                        driver.get("https://vktarget.ru/list/");
                        dbConnection.logInvoice(account.getVkAccount().getId(), balance, "vktarget");
                        System.out.println("Vktarget account " + account.getVkAccount().getId() + " successfully invoiced");
                    } catch (Exception e) {
                        System.out.println("Vktarget account " + account.getVkAccount().getId() + " failed to invoice !!!!!");
                        e.printStackTrace();
                    }
                }
            }
        }
        dbConnection.updateVKTargetBalance(account.getId(), balance);
        account.setBalanceVkTarget(balance);
        return balance;
    }

    private static void processVLikeTaskList(final ChromeDriver driver,
                                             final DbConnection dbConnection,
                                             final VkSerfingAccount account,
                                             final VkService vkService,
                                             RestTemplate restTemplate,
                                             boolean extended) throws Exception {
        try {
            driver.executeScript("window.open('https://v-like.ru/tasks/vk', '_blank').focus();");
        } catch (Exception e) {
        }
        Thread.sleep(getRandomNumber(10000, 12000));
        closeAllTabsExceptFirstOne(driver);

        driver.get("https://v-like.ru/tasks/vk");
        try {
            waitUntilElementWillBePresent(driver, "a[@href='https://v-like.ru/account/withdraws']");
        } catch (Exception e) {
            System.out.println("Account vlike " + account.getVkAccount().getId() + " is not authorized going to relogin");
            WebElement vlikeEmailField = waitUntilElementWillBePresent(driver, "input[@id='email']");
            clickElement(driver, vlikeEmailField);
            Thread.sleep(getRandomNumber(500, 1000));
            vlikeEmailField.sendKeys(account.getEmail());
            WebElement vlikePasswordField = waitUntilElementWillBePresent(driver, "input[@id='password']");
            clickElement(driver, vlikePasswordField);
            Thread.sleep(getRandomNumber(500, 1000));
            vlikePasswordField.sendKeys(account.getPassword());
            clickElement(driver, "button[@type='submit']");
            waitUntilElementWillBePresent(driver, "a[@href='https://v-like.ru/account/withdraws']");
        }
        try {
            driver.findElement(By.xpath("//h4[text()='Для выполнения заданий вам необходимо прикрепить страницу VK']"));
            System.out.println("Account vlike " + account.getVkAccount().getId() + " doesn't have associated vk account");
            return;
        } catch (Exception ignored) {
        }
//        try {
//            waitUntilElementWithTextWillBePresent(driver, "h4", "Преимущества");
//            return;
//        } catch (Exception ignored) {
//
//        }
        int index = 0;
        final int amountOfChecks = extended ? getRandomNumber(200, 290) : 1;
        int amountOfAvailableScenarious = getRandomNumber(0, 3);
        final List<String> processedHrefs = new ArrayList<>();
        final Date startTime = new Date();
        System.out.println(startTime + " Going to process vlike acc for vk [" + account.getVkAccount().getId() + "]");
        while (index < amountOfChecks) {
            if (index != 0 && index % 15 == 0 && getRandomNumber(0, 100) > 60) {
                if (getRandomNumber(0, 100) > 50) {
                    clickElement(driver, "a[@href='https://v-like.ru/tasks/vk']");
                } else {
                    driver.get("https://v-like.ru/tasks/vk");
                }
                waitUntilElementWillBePresent(driver, "a[@href='https://v-like.ru/account/withdraws']", 5);
                try {
                    driver.findElement(By.xpath("//h4[text()='Для выполнения заданий вам необходимо прикрепить страницу VK']"));
                    System.out.println("Account vlike " + account.getVkAccount().getId() + " doesn't have associated vk account");
                    return;
                } catch (Exception ignored) {
                }
            }
            String pageSource = driver.getPageSource();
            if (pageSource.contains("Ваш аккаунт заблокирован за нарушение правил сервиса.")) {
                System.out.println("Vlike acc is blocked " + account.getId());
                return;
            }
            updateVlikeBalance(driver, dbConnection, account);
            final Pattern taskPattern = Pattern.compile("<a(.+?)>(.+?)<\\/a>", Pattern.DOTALL);
            final Matcher taskUrlMatcher = taskPattern.matcher(pageSource);
            final List<String> vkLinks = new ArrayList<>();
            while (taskUrlMatcher.find()) {
                final String link = taskUrlMatcher.group();
                if (link.contains("vk.com") && !link.contains("class")) {
                    vkLinks.add(link);
                }
            }

            Collections.shuffle(vkLinks);
            final VkAccount vkAccount = account.getVkAccount();
            for (final String tag : vkLinks) {
                try {
                    final String link = tag.split("href=\"")[1].split("\"")[0];
                    if (processedHrefs.contains(link)) {
                        continue;
                    } else {
                        processedHrefs.add(link);
                    }
                    final String taskType = tag.split("data-task-type=\"")[1].split("\"")[0];
                    final String taskId = tag.split("data-task-id=\"")[1].split("\"")[0];
                    final WebElement startTaskButton;
                    if (getRandomNumber(0, 100) > 70) {
                        //        System.out.println("Account vlike " + account.getVkAccount().getId() + " is going to missclick");
                        startTaskButton = missClick(driver, "a[@href='" + link + "'][contains(text(), 'открыть страницу')]");
                    } else {
                        startTaskButton = waitUntilElementWillBePresent(driver, "a[@href='" + link + "'][contains(text(), 'открыть страницу')]");
                    }
                    closeAllTabsExceptFirstOne(driver);
                    clickElement(driver, startTaskButton);
                    waitUntilNumbersOfWindows(driver, 300, 2);
                    ArrayList<String> windows = new ArrayList<>(driver.getWindowHandles());
                    driver.switchTo().window(windows.get(1));
                    TaskInfo taskInfo = scanCookiesFromCurrentPage(driver, account.getVkAccount(), null);
                    String profileHtml = taskInfo.getProfileHtml();
                    if (dbConnection.isLinkBlackListed(link)) {
                        Thread.sleep(sleepTime(getRandomNumber(5000, 7000), taskInfo));
                        driver.close();
                        driver.switchTo().window(windows.get(0));
                        clickElement(driver, "a[@data-href='/tasks/vk/" + taskType + "/skip/" + taskId + "']");
                        // System.out.println(account.getVkAccount().getId() + ": Task skipped " + link);
                        continue;
                    }
                    // System.out.println(taskType + ": " + tag);
                    try {
                        restTemplate = ProxyCustomizer.buildRestTemplate(vkAccount);
                        if ("subscribers".equals(taskType)) {
                            //   if (extended) {
                            if (vkAccount.isCantSubscribe() || vkAccount.isLimitForSubscribtions() || dbConnection.isLinkOnCooldown(link)) {
                                Thread.sleep(sleepTime(getRandomNumber(5000, 7000), taskInfo));
                                driver.close();
                                driver.switchTo().window(windows.get(0));
                                continue;
                            }
                            boolean subscribe = vkService.subscribe(vkAccount, link, restTemplate, driver, profileHtml);
                            if (subscribe) {
                                //          processSubscription(link, vkAccount.getId());
                                vkAccount.setAvailableSubscribes(vkAccount.getAvailableSubscribes() - 1);
                                dbConnection.updateVkLimitsForSubscribes(vkAccount);
                            } else {
                                throw new Exception("vlike acc " + vkAccount.getId() + " couldn't subscribe for " + link + " going to skip");
                            }
                            Thread.sleep(sleepTime(getRandomNumber(2000, 4000), taskInfo));
//                            } else {
//                                Thread.sleep(getRandomNumber(5000, 7000));//vlike стремный
//                                driver.close();
//                                driver.switchTo().window(windows.get(0));
//                                continue;
//                            }
                        } else if ("likes".equals(taskType)) {
                            boolean result = false;
                            if (tag.contains("лайк и репост")) {
                                if (vkAccount.isLimitForShares() || dbConnection.isLinkOnCooldown(link)) {
                                    Thread.sleep(getRandomNumber(5000, 7000));
                                    driver.close();
                                    driver.switchTo().window(windows.get(0));
                                    continue;
                                }
                                result = vkService.share(vkAccount, link, restTemplate, profileHtml);
                                if (result) {
                                    vkAccount.setAvailableShares(vkAccount.getAvailableShares() - 1);
                                    dbConnection.updateVkLimitsForShares(vkAccount);
                                } else {
                                    throw new Exception("vlike acc " + vkAccount.getId() + " will skip repost");
                                }
//                                Thread.sleep(getRandomNumber(5000, 7000));
//                                Thread.sleep(sleepTime(getRandomNumber(5000, 7000), taskInfo));//vlike стремный
//                                driver.close();
//                                driver.switchTo().window(windows.get(0));
//                                continue;
                            } else if (link.contains("/photo")) {
                                result = vkService.likePhoto(vkAccount, link, restTemplate, profileHtml);
                            } else if (link.contains("/clip")) {
                                result = vkService.likeClip(vkAccount, link, restTemplate, profileHtml);
                            } else if (link.contains("/video")) {
                                result = vkService.likeVideo(vkAccount, link, restTemplate, profileHtml);
                            } else if (link.contains("reply=")) {
                                result = vkService.likeReply(vkAccount, link, restTemplate, profileHtml);
                            } else {
                                result = vkService.like(vkAccount, link, restTemplate, profileHtml);
                            }
                            if (!result) {
                                throw new Exception("vlike acc " + vkAccount.getId() + " couldn't like " + link + " going to skip");
                            }
                            Thread.sleep(sleepTime(getRandomNumber(2000, 5000), taskInfo));
                        } else if ("friends".equals(taskType)) {
                            // throw new Exception("vlike acc " + vkAccount.getId() + " will skip friend request");
                            if (vkAccount.isLimitForFriendRequestsIsOver()
                                    || vkAccount.isCantAddFriends()
                                    || dbConnection.isLinkOnCooldown(link)
                            ) {
                                Thread.sleep(sleepTime(getRandomNumber(5000, 7000), taskInfo));
                                driver.close();
                                driver.switchTo().window(windows.get(0));
                                continue;
                            }
                            boolean subscribed = vkService.addFriend(vkAccount, link, restTemplate, profileHtml);
                            if (subscribed) {
                                //  processSubscription(link, vkAccount.getId());
                                vkAccount.setAvailableFriendRequests(vkAccount.getAvailableFriendRequests() - 1);
                                dbConnection.updateVkLimitsForFriendRequests(vkAccount);
                            }
                            Thread.sleep(sleepTime(getRandomNumber(2000, 4000), taskInfo));
                        } else {
                            System.out.println("unknown task type for vk: " + taskType);
                            Thread.sleep(sleepTime(getRandomNumber(5000, 7000), taskInfo));
                            driver.close();
                            driver.switchTo().window(windows.get(0));
                            continue;
                        }
                    } catch (Exception e) {
//                        if (vkAccount.isProxyUnauthorized()) {
//                            restTemplate = ProxyCustomizer.buildRestTemplate(vkAccount);
//                        } else {
//                            e.printStackTrace();
//                        }
                        Thread.sleep(sleepTime(getRandomNumber(2000, 4000), taskInfo));
                        driver.close();
                        driver.switchTo().window(windows.get(0));
                        clickElement(driver, "a[@data-href='/tasks/vk/" + taskType + "/skip/" + taskId + "']");
                        // System.out.println(account.getId() + ": Task skipped " + taskId);
                        continue;
                    }
                    driver.close();
                    driver.switchTo().window(windows.get(0));
                    final WebElement submitButton;
                    if (getRandomNumber(0, 100) > 80) {
                        //   System.out.println("Account vlike " + account.getVkAccount().getId() + " is going to missclick on submit button");
                        submitButton = missClick(driver, "a[@data-href='/tasks/vk/" + taskType + "/check/" + taskId + "'][contains(text(), 'подтвердить выполнение')]");
                    } else {
                        submitButton = waitUntilElementWillBePresent(driver, "a[@data-href='/tasks/vk/" + taskType + "/check/" + taskId + "'][contains(text(), 'подтвердить выполнение')]");
                    }
                    clickElement(driver, submitButton);
                    Thread.sleep(getRandomNumber(4000, 6500));
                    final Float balance = updateVlikeBalance(driver, dbConnection, account);
                    if (balance != null && balance > 20 && getRandomNumber(0, 100) > 95) {
                        try {
                            clickElement(driver, "a[@href='https://v-like.ru/account/withdraws']");
                            WebElement payeerWalletField = waitUntilElementWillBePresent(driver, "input[@id='wallet_7']");
                            String payeerWalletFieldDisabledAttribute = payeerWalletField.getAttribute("disabled");
                            System.out.println("Going to invoice vlike acc " + vkAccount.getId() + " wallet field status: " + payeerWalletFieldDisabledAttribute);
                            final PayeerAccount payeerAccount = vkAccount.getPayeerAccount();
                            if (payeerWalletFieldDisabledAttribute == null || !payeerWalletFieldDisabledAttribute.equals("true")) {
//                                final WebElement withdrawCombo = waitUntilElementWillBePresent(driver, "select[@name='withdraw_method']");
//                                Thread.sleep(getRandomNumber(700, 1000));
//                                clickButton(driver, withdrawCombo);
////                            Thread.sleep(getRandomNumber(200, 450));
////                            withdrawCombo.sendKeys(Keys.ARROW_DOWN);
////                            Thread.sleep(getRandomNumber(200, 450));
////                            withdrawCombo.sendKeys(Keys.ARROW_DOWN);
////                            Thread.sleep(getRandomNumber(200, 450));
////                            withdrawCombo.sendKeys(Keys.ARROW_DOWN);
//                                Thread.sleep(getRandomNumber(200, 450));
//                                withdrawCombo.sendKeys(Keys.ENTER);
//                                Thread.sleep(getRandomNumber(800, 1000));
                                clickElement(driver, payeerWalletField);
                                Thread.sleep(getRandomNumber(1000, 1200));
                                payeerWalletField.sendKeys(payeerAccount.getAccName());
                            }
                            WebElement sumField = waitUntilElementWillBePresent(driver, "input[@id='amount']");
                            Thread.sleep(getRandomNumber(400, 800));
                            clickElement(driver, sumField);
                            Thread.sleep(getRandomNumber(200, 450));
                            final int sumToInvoice = balance.intValue();
                            sumField.sendKeys(String.valueOf(sumToInvoice));
                            dbConnection.updatePayeerBalance(payeerAccount.getId(), sumToInvoice);
                            //  Thread.sleep(100000);
                            clickElement(driver, "input[@type='submit'][@value='Заказать выплату']");
                            dbConnection.logInvoice(account.getVkAccount().getId(), (float) sumToInvoice, "vlike");
                            System.out.println("Successfully invoiced vlike acc " + vkAccount.getId());
                        } catch (Exception e) {
                            System.out.println("Failed to invoice vlike acc " + vkAccount.getId());
                            e.printStackTrace();
                        }
                        clickElement(driver, "a[@href='https://v-like.ru/tasks/vk']");
                    }
                    System.out.println("Account vlike " + account.getVkAccount().getId() + " submitted task " + link + " balance " + balance);
                } catch (Exception vkException) {
                    System.out.println("ERROR vlike acc " + account.getId() + " :" + tag);
                    vkException.printStackTrace();
                }
            }
            index++;
            Thread.sleep(1000);
            //   if (getRandomNumber(0, 100) > 90 && amountOfAvailableScenarious != 0) {
//                int scenario = getRandomNumber(0, 4);
//                System.out.println("Account vlike " + account.getVkAccount().getId() + " will execute scenario " + scenario);
//                vlikeScenarious.get(scenario).accept(driver);
//                amountOfAvailableScenarious--;
            //}
        }
        if (extended) {
            final Date now = new Date();
            System.out.println(now + " Finished extended processing vlike acc for vk [" + account.getVkAccount().getId() + "]. Took " + new Date(now.getTime() - startTime.getTime()).getMinutes() + "m");
        } else {
            System.out.println(new Date() + " Finished processing vlike acc for vk [" + account.getVkAccount().getId() + "]");
        }
    }

    private static Float updateVlikeBalance(final ChromeDriver driver, final DbConnection dbConnection, final VkSerfingAccount account) throws Exception {
        final Matcher balanceMatcher = Pattern.compile("<span id=\"sidebar_balance\">(.+?)</span>", Pattern.DOTALL).matcher(driver.getPageSource());
        if (balanceMatcher.find()) {
            final float balance = Float.parseFloat(balanceMatcher.group(1));
            dbConnection.updateVlikeBalance(account.getId(), balance);
            return balance;
        } else {
            System.out.println("Couldn't retrieve the balance for acc: " + account.getId());
        }
        return null;
    }


    private static int verificationsInRow = 0;
    private static Long cooldownForVerfifications = null;

    private static boolean verifyVkSerfing(final ChromeDriver driver,
                                           final DbConnection dbConnection,
                                           final VkSerfingAccount account,
                                           final VkAccount vkAccount,
                                           Consumer<ChromeDriver> scenario) throws Exception {
        boolean startVerification = false;
        try {
            clickElement(driver, findElement(driver, "a[text()='верифицировать']"));
            startVerification = true;
        } catch (Exception e) {
        }
        if (startVerification) {
            System.out.println("Going to verify vkserfing acc [" + vkAccount.getId() + "]");
            final Date now = new Date();
            if (cooldownForVerfifications != null) {
                if (cooldownForVerfifications > now.getTime()) {
                    System.out.println("Cooldown for verfications until " + new Date(cooldownForVerfifications));
                    startVerification = false;
                } else {
                    verificationsInRow = 0;
                }
            }
            if (verificationsInRow > 3) {
//                cooldownForVerfifications = now.getTime() + 1800000;//30min
                cooldownForVerfifications = now.getTime() + 900000;//15min
                startVerification = false;
            }
            if (startVerification) {
                verificationsInRow++;
            } else {
                if (scenario != null) {
                    try {
                        System.out.println("going to execute scenario for acc " + account.getVkAccount().getId() + " " + scenario);
                        scenario.accept(driver);
                        Thread.sleep(getRandomNumber(3000, 4000));
                    } catch (Exception ignore) {
                    }
                    return false;
                }
            }
            try {
                WebElement phoneNumberField = waitUntilElementWillBePresent(driver, "input");
                clickElement(driver, phoneNumberField);
//                FiveSimService instance = FiveSimService.getInstance();
                SmsActivate instance = SmsActivate.getInstance();
                SmsActivationResponse smsActivationResponse = instance.purchaseNumber("ot", "tele2", "&verification=true");
//                SmsActivationResponse smsActivationResponse = instance.purchaseCallResetForVkSerfing();
                if (smsActivationResponse == null) {
                    return false;
                }
                String verificationCode = null;
                try {
                    WebElement verifyButton = waitUntilElementWillBePresent(driver, "span[text()='Верифицировать']").findElement(By.xpath("./.."));
                    phoneNumberField.sendKeys("+" + smsActivationResponse.getPhone());
//                    phoneNumberField.sendKeys(smsActivationResponse.getPhone());
                    clickElement(driver, verifyButton);
                    verificationCode = instance.retrieveLastResultCodeOfTheCallReset(smsActivationResponse.getId(), 1);
//                    verificationCode = instance.retrieveLastResultCodeOfThePurchase(smsActivationResponse.getId(), 1);
                    if (verificationCode == null) {
                        WebElement repeatCallButton = waitUntilElementWillBePresent(driver, "span[text()='Запросить звонок повторно ']", 300).findElement(By.xpath("./.."));
                        clickElement(driver, repeatCallButton);
                        Thread.sleep(3000);
                        verificationCode = instance.retrieveLastResultCodeOfTheCallReset(smsActivationResponse.getId(), 1);
//                        verificationCode = instance.retrieveLastResultCodeOfThePurchase(smsActivationResponse.getId(), 1);
                        if (verificationCode == null) {
                            try {
                                instance.cancelRequest(smsActivationResponse.getId());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Thread.sleep(getRandomNumber(10000, 15000));
                            smsActivationResponse = instance.purchaseNumber("ot", "tele2", "&verification=true");
//                            smsActivationResponse = instance.purchaseCallResetForVkSerfing();
                            if (smsActivationResponse == null) {
                                return false;
                            }
                            clickElement(driver, waitUntilElementWillBePresent(driver, "a[contains(text(), 'изменить')]"));
                            phoneNumberField = waitUntilElementWillBePresent(driver, "input");
                            clickElement(driver, phoneNumberField);
//                            phoneNumberField.sendKeys(smsActivationResponse.getPhone());
                            phoneNumberField.sendKeys("+" + smsActivationResponse.getPhone());
                            clickElement(driver, waitUntilElementWillBePresent(driver, "span[text()='Верифицировать']").findElement(By.xpath("./..")));
                            verificationCode = instance.retrieveLastResultCodeOfTheCallReset(smsActivationResponse.getId(), 1);
//                            verificationCode = instance.retrieveLastResultCodeOfThePurchase(smsActivationResponse.getId(), 1);
                            if (verificationCode == null) {
                                repeatCallButton = waitUntilElementWillBePresent(driver, "span[text()='Запросить звонок повторно ']", 300).findElement(By.xpath("./.."));
                                clickElement(driver, repeatCallButton);
                                verificationCode = instance.retrieveLastResultCodeOfTheCallReset(smsActivationResponse.getId(), 1);
//                                verificationCode = instance.retrieveLastResultCodeOfThePurchase(smsActivationResponse.getId(), 1);
                                if (verificationCode == null) {
                                    try {
                                        instance.cancelRequest(smsActivationResponse.getId());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    instance.finishRequest(smsActivationResponse.getId());
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("blyaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaat going to wait!!!!");
                    Thread.sleep(999999999);
                    clickElement(driver, waitUntilElementWillBePresent(driver, "a[contains(text(), 'изменить')]"));
                    phoneNumberField = waitUntilElementWillBePresent(driver, "input");
                    clickElement(driver, phoneNumberField);
                    phoneNumberField.sendKeys(smsActivationResponse.getPhone());
                    clickElement(driver, waitUntilElementWillBePresent(driver, "span[text()='Верифицировать']").findElement(By.xpath("./..")));
                    verificationCode = instance.retrieveLastResultCodeOfThePurchase(smsActivationResponse.getId(), 1);
                }
                try {
                    if (verificationCode == null) {
                        instance.cancelRequest(smsActivationResponse.getId());
                    } else {
                        instance.finishRequest(smsActivationResponse.getId());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println(verificationCode);
                WebElement codeField = waitUntilElementWillBePresent(driver, "input[@style='letter-spacing: 2px;']");
                clickElement(driver, codeField);
                codeField.sendKeys(verificationCode.substring(verificationCode.length() - 4));
                clickElement(driver, waitUntilElementWillBePresent(driver, "span[text()='Продолжить']").findElement(By.xpath("./..")));
                Thread.sleep(getRandomNumber(5000, 10000));
                System.out.println("Account " + vkAccount.getId() + " successfully verified");
                return true;
            } catch (Exception e) {
                System.out.println("!!!!!!!!!Failed to verify acc " + vkAccount.getBrowserProfile());
                e.printStackTrace();
                dbConnection.vkSerfingAccRequiresVerification(account.getId());
                throw e;
            }
        }
        return true;
    }

    private static void processPayUpVideoAcc(final ChromeDriver driver,
                                             final VkSerfingAccount account) throws Exception {
        driver.get("https://payup.video/tasks/video/");
        PayUpVideoAcc payUpVideoAcc = account.getVkAccount().getPayUpVideoAcc();
        try {
            waitUntilElementWillBePresent(driver, "div[@id='swal2-content'][contains(text(), 'Account has been blocked')]", getRandomNumber(4, 8));
            System.out.println(payUpVideoAcc.getVkAccount().getId() + " blocked by payupvideo");
            dbConnection.markPayUpVideoAccAsBlocked(payUpVideoAcc.getId());
            return;
        } catch (Exception ignored) {
        }
        try {
            waitUntilElementWillBePresent(driver, "div[@id='swal2-content'][contains(text(), 'Аккаунт был заблокирован')]", 1);
            System.out.println(payUpVideoAcc.getVkAccount().getId() + " blocked by payupvideo");
            dbConnection.markPayUpVideoAccAsBlocked(payUpVideoAcc.getId());
            return;
        } catch (Exception ignored) {
        }
//        if (true) {
//            return;
//        }

        String sessionId = driver.manage().getCookieNamed("PHPSESSID").getValue();
       // PayUpScenarious.performTest(driver);
        if (checkForChance(40)) {
            PayUpScenarious.perform(driver, payUpVideoAcc);
        }
        String videoTabInnerText = waitUntilElementWillBePresent(driver, "a[@class='btn btn-light-success bg-transparent px-0 d-flex align-items-center font-weight-bolder']")
                .getDomProperty("innerText");
        if (!videoTabInnerText.contains("Start earning")) {
            clickElement(driver, "div[@data-lng='en']");
            Thread.sleep(getRandomNumber(2000, 3500));
            while (!waitUntilElementWillBePresent(driver, "a[@class='btn btn-light-success bg-transparent px-0 d-flex align-items-center font-weight-bolder']")
                    .getDomProperty("innerText")
                    .contains("Start earning")) {
                clickElement(driver, "div[@data-lng='en']");
            }
        }
        clickElement(driver, "button[@class='card_run btn btn-success py-4 pl-6 pr-8 d-flex align-items-center justify-content-center mb-12 w-100 w-lg-auto']");
        Thread.sleep(getRandomNumber(500, 1500));
        ArrayList<String> windows = new ArrayList<>(driver.getWindowHandles());
        if (windows.size() == 1 && checkForChance(75)) {
            driver.navigate().refresh();
            clickElement(driver, "button[@class='card_run btn btn-success py-4 pl-6 pr-8 d-flex align-items-center justify-content-center mb-12 w-100 w-lg-auto']");
            Thread.sleep(getRandomNumber(500, 1500));
            windows = new ArrayList<>(driver.getWindowHandles());
        }
        String popWindow = windows.get(windows.size() - 1);

        int amountOfVideosGoingToBeWatched = getRandomNumber(15, 30);
        final Date startTime = new Date();
        System.out.println(startTime + " PayUp acc [" + payUpVideoAcc.getVkAccount().getId() + "] going to watch [" + amountOfVideosGoingToBeWatched
                + "] videos");
        driver.switchTo().window(popWindow);
        Thread.sleep(1000);
        int amountOfSolvedCaptchas = 0;
        Integer videosPassedSinceLastCaptcha = null;
        RuCaptcha.CaptchaAnswer lastAnswer = null;
        RuCaptcha ruCaptcha = RuCaptcha.getInstance();
        int videosWatched = 0;

        while (amountOfVideosGoingToBeWatched > videosWatched) {
            Long timer = null;
            int attempt = 0;
            while (true) {
                try {
                    attempt++;
                    timer = Long.valueOf(waitUntilElementWillBePresent(driver, "div[@id='timer']", 2).getText());
                    break;
                } catch (Exception e) {
                    try {
                        WebElement nextVideoButton = waitUntilElementWillBePresent(driver, "button[@id='nextTask']", 2);
                        if (nextVideoButton.isDisplayed()) {
                            clickElement(driver, nextVideoButton);
                        } else if (attempt > 2) {
                            WebElement skipButton = waitUntilElementWillBePresent(driver, "button[@id='skipVideo']", 2);
                            if (skipButton.isDisplayed()) {
                                clickElement(driver, skipButton);
                            }
                        } else if (attempt > 5) {
                            driver.navigate().refresh();
                        }
                    } catch (Exception ex) {

                    }
                }
            }
            WebElement videoIframe = findElement(driver, "iframe[@id='task_video']");
            String videoTitle = videoIframe.getAttribute("title");
            System.out.println(new Date() + " [" + payUpVideoAcc.getVkAccount().getId() + "] Going to watch [" + videoTitle + "] for " + timer + "s");
            driver.switchTo().frame(videoIframe);
//            if (getRandomNumber(0, 100) > 50) {
//                clickElement(driver, "button[@aria-label='Смотреть']");
//            } else {
//                missClick(driver, "button[@aria-label='Смотреть']");
//            }
            clickElementWithoutPause(driver, findElement(driver, "div[@class='ytp-cued-thumbnail-overlay']"), 100);
            driver.switchTo().window(popWindow);
            Thread.sleep(timer * 1000 + getRandomNumber(1000, 2500));
            WebElement nextVideoButton = waitUntilElementWillBePresent(driver, "button[@id='nextTask']", timer);
            WebElement captchaNoteDiv = findElement(driver, "div[contains(text(), 'Verify you are human:')]");
            if (Integer.parseInt(captchaNoteDiv.getDomProperty("offsetWidth")) != 0) {
                try {
                    List<WebElement> captchaElements = captchaNoteDiv.findElement(By.xpath("./.."))
                            .findElements(By.xpath(".//*"))
                            .get(1)
                            .findElements(By.xpath(".//*"))
                            .get(0)
                            .findElements(By.xpath(".//*"))
                            .get(0)
                            .findElements(By.xpath(".//*"));
                    if (captchaElements.isEmpty()) {
                        captchaElements = captchaNoteDiv.findElement(By.xpath("./.."))
                                .findElements(By.xpath(".//*"))
                                .get(1)
                                .findElements(By.xpath(".//*"))
                                .get(0)
                                .findElements(By.xpath(".//*"));
                    }
                    WebElement captcha = null;
                    for (final WebElement captchaElement : captchaElements) {
                        if (Integer.parseInt(captchaElement.getDomProperty("offsetWidth")) > 100) {
                            captcha = captchaElement;
                            break;
                        }
                    }
                    final String captchaBase64Image = captcha.getCssValue("background");
                    System.out.println(new Date() + " [" + payUpVideoAcc.getVkAccount().getId() + "] got captcha: " + captchaBase64Image);

                    RuCaptcha.CaptchaAnswer answer = solvePayUpCaptcha(driver, captchaBase64Image, sessionId, nextVideoButton);
                    if (answer != null) {
                        amountOfSolvedCaptchas++;
                        videosPassedSinceLastCaptcha = 0;
                        lastAnswer = answer;
                        ruCaptcha.reportGood(lastAnswer);
                        dbConnection.logCaptcha(payUpVideoAcc.getId());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (getRandomNumber(0, 100) > 50) {
                    missClick(driver, nextVideoButton);
                }
                clickElement(driver, nextVideoButton);
            } else {
                if (videosPassedSinceLastCaptcha != null) {
                    videosPassedSinceLastCaptcha++;
                }
                if (nextVideoButton.isDisplayed()) {
                    if (getRandomNumber(0, 100) > 50) {
                        missClick(driver, nextVideoButton);
                    }
                    clickElement(driver, nextVideoButton);
                }
            }
            float newBalance = Float.parseFloat(waitUntilElementWillBePresent(driver, "b[@id='balance']").getText().replaceAll("\\$", ""));
            System.out.println(new Date() + " [" + payUpVideoAcc.getVkAccount().getId() + "] submitted task [" + videoTitle + "]. Balance: " + newBalance);
            dbConnection.updatePayUpVideoBalance(payUpVideoAcc.getId(), newBalance);
            videosWatched++;
        }
        if (checkForChance(45)) {
            driver.close();
            driver.switchTo().window(windows.get(0));
            PayUpScenarious.perform(driver, payUpVideoAcc);
        }
        final Date endTime = new Date();
        System.out.println(endTime + " finished processing payup acc [" + account.getVkAccount().getId() + "]" + " Time - [" + ((endTime.getTime() - startTime.getTime()) / 1000 / 60) + "m]. Amount of solved captchas [" + amountOfSolvedCaptchas + "]. Amount of videos watched [" + amountOfVideosGoingToBeWatched + "]");
    }

    private static RuCaptcha.CaptchaAnswer solvePayUpCaptcha(final ChromeDriver driver,
                                                             final String captchaBase64Image,
                                                             final String sessionId,
                                                             final WebElement nextVideoButton
    ) throws Exception {
        RuCaptcha.CaptchaAnswer answer = RuCaptcha.getInstance().solveCoordinatesCaptcha(
                captchaBase64Image.split("base64,")[1].split("\"\\)")[0]
        );
        Map<String, Integer> coordinate = answer.getCoordinates().get(0);
        try {
            if (findElement(driver, "div[@id='captcha-alert-title'][contains(text(), 'Verification timeout')]").isDisplayed()) {
                System.out.println("Verification timeout");
                return null;
            }
        } catch (Exception ignored) {
        }
        String response = (String) driver.executeScript(
                "var xhr = new XMLHttpRequest();" +
                        "xhr.open('POST', 'https://payup.video/captcha/control/check.php', false);" +  // Making request synchronous
                        "xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8');" +
                        "xhr.setRequestHeader('Accept', 'application/json, text/javascript, */*; q=0.01');" +
                        "xhr.send('x=" + coordinate.get("x") + "&y=" + coordinate.get("y") + "&s_id=" + sessionId + "');" +
                        "return xhr.responseText;"
        );
        //      System.out.println(response);
        if (response.contains("\"status\":\"data\"")) {
            RuCaptcha.getInstance().reportBad(answer);
            return null;
        }
        driver.executeScript("arguments[0].removeAttribute('disabled')", nextVideoButton);
        return answer;
    }

    private static void processVkSerfingTaskListV2(final ChromeDriver driver,
                                                   final DbConnection dbConnection,
                                                   final VkSerfingAccount account,
                                                   final VkService vkService,
                                                   RestTemplate restTemplate,
                                                   final boolean executeScenario) throws Exception {
        final List<String> processedLinks = new ArrayList<>();
        VkAccount vkAccount = account.getVkAccount();
        driver.get("https://vkserfing.ru/assignments/vk");
        Thread.sleep(getRandomNumber(5000, 10000));
        while (true) {
            try {
                if (driver.getPageSource().contains("Ваш аккаунт заблокирован")) {
                    System.out.println(account.getId() + " blocked by vkserfing");
                    return;
                }
                break;
            } catch (JavascriptException e) {
                System.out.println(vkAccount.getId());
                e.printStackTrace();
                Thread.sleep(1000);
            }
        }
        Consumer<ChromeDriver> scenario = null;
        if (executeScenario) {
            if (getRandomNumber(0, 100) > 45) {
                final int scenarioId = getRandomNumber(0, 11);
                System.out.println("Account " + account.getVkAccount().getId() + " will execute scenario " + scenarioId);
                scenario = vkSerfingScenarious.get(scenarioId);
            }
        } else {
            scenario = null;
        }
        try {
            clickElement(driver, driver.findElement(By.xpath("//button[@title='Закрыть']")));

        } catch (Exception ignored) {
        }
        if (!verifyVkSerfing(driver, dbConnection, account, vkAccount, scenario)) {
            driver.quit();
            localRestTemplate.exchange(BASE_URL + "/api/v1/browser/stop?user_id=" + vkAccount.getBrowserProfile(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
        }
        List<WebElement> taskDivs = driver.findElements(By.xpath("//div[@class='cards__col col-12 col-xxs-6 col-sm-4 col-lg-3']"));
        while (taskDivs.isEmpty()) {
            taskDivs = driver.findElements(By.xpath("//div[@class='cards__col col-12 col-xxs-6 col-sm-4 col-lg-3']"));
            Thread.sleep(1000);
        }
        for (int index = 0; index < taskDivs.size(); index++) {
            final WebElement taskDiv = taskDivs.get(index);
            try {
                if (account.getVkserfingFailsInRow() > 4) {
                    System.out.println("VkSerfing account " + account.getVkAccount().getId() + " fails in row more than 4");
                    return;
                }
                final TaskInfo taskInformation;
                try {
                    taskInformation = startPerformingTaskForVkSerfing(driver, taskDiv, vkAccount);
                } catch (final NoSuchWindowException e) {
                    System.out.println(account.getVkAccount().getId() + " task is not available");
                    closeAllTabsExceptFirstOne(driver);
                    continue;
                } catch (final Exception e) {
                    e.printStackTrace();
                    System.out.println(account.getVkAccount().getId() + " " + taskDiv.getAttribute("innerHTML"));
                    try {
                        clickElement(driver, "button[@title='Закрыть']", 5);
                    } catch (Exception ex) {
                    }
                    continue;
                }
                if (taskInformation == null || processedLinks.contains(taskInformation.getLink())) {
                    // clickButton(driver, "button[@title='Закрыть']", 10);
                    continue;
                }
                processedLinks.add(taskInformation.getLink());
                if (dbConnection.isLinkBlackListed(taskInformation.getLink())) {
                    skipTask(driver, taskInformation.getIntermediateLink());
                    Thread.sleep(getRandomNumber(1000, 2500));
                    taskDivs = driver.findElements(By.xpath("//div[@class='cards__col col-12 col-xxs-6 col-sm-4 col-lg-3']"));
                    continue;
                }
                taskInformation.setAccount(account);
                taskInformation.setDriver(driver);
                final String type = taskInformation.getType();

                switch (type) {
                    case "Подписчики": {
                        performTaskV2(
                                taskInfo -> {
                                    boolean subscribe = vkService.subscribe(vkAccount, taskInformation.getLink(), restTemplate, driver, taskInformation.getProfileHtml());
                                    if (subscribe) {
                                        //      processSubscription(taskInformation.getLink(), vkAccount.getId());
                                        vkAccount.setAvailableSubscribes(vkAccount.getAvailableSubscribes() - 1);
                                        dbConnection.updateVkLimitsForSubscribes(vkAccount);
                                    }
                                    return subscribe;
                                },
                                () -> dbConnection.isLinkOnCooldown(taskInformation.getLink()),
                                taskInformation, dbConnection, getRandomNumber(0, 2000));
                        break;
                    }
                    case "Лайки": {
                        final ThrowableFunction<TaskInfo, Boolean> task;
                        if (taskInformation.getLink().contains("/photo")) {
                            task = taskInfo -> vkService.likePhoto(vkAccount, taskInfo.getLink(), restTemplate, taskInfo.getProfileHtml());
                        } else if (taskInformation.getLink().contains("/clip")) {
                            task = taskInfo -> vkService.likeClip(vkAccount, taskInfo.getLink(), restTemplate, taskInfo.getProfileHtml());
                        } else if (taskInformation.getLink().contains("/video")) {
                            task = taskInfo -> vkService.likeVideo(vkAccount, taskInfo.getLink(), restTemplate, taskInfo.getProfileHtml());
                        } else if (taskInformation.getLink().contains("reply=") || taskInformation.getLink().contains("_r")) {
                            task = taskInfo -> vkService.likeReply(vkAccount, taskInfo.getLink(), restTemplate, taskInfo.getProfileHtml());
                        } else {
                            task = taskInfo -> vkService.like(vkAccount, taskInfo.getLink(), restTemplate, taskInfo.getProfileHtml());
                        }
                        performTaskV2(task, null, taskInformation, dbConnection, getRandomNumber(0, 2000));
                        break;
                    }
                    case "Репосты": {
                        performTaskV2(
                                taskInfo -> {
                                    boolean share = vkService.share(vkAccount, handleVkLinks(taskInformation.getLink()), restTemplate, taskInformation.getProfileHtml());
                                    if (share) {
                                        //    processSubscription(taskInformation.getLink(), vkAccount.getId());
                                        vkAccount.setAvailableShares(vkAccount.getAvailableShares() - 1);
                                        dbConnection.updateVkLimitsForShares(vkAccount);
                                    }
                                    return share;
                                },
                                () -> dbConnection.isLinkOnCooldown(handleVkLinks(taskInformation.getLink())),
                                taskInformation, dbConnection, 1000 + getRandomNumber(1000, 3500));
                        break;
                    }
                    case "Друзья": {
                        performTaskV2(
                                taskInfo -> {
                                    boolean share = vkService.addFriend(vkAccount, handleVkLinks(taskInformation.getLink()), restTemplate, taskInformation.getProfileHtml());
                                    if (share) {
                                        //    processSubscription(taskInformation.getLink(), vkAccount.getId());
                                        vkAccount.setAvailableFriendRequests(vkAccount.getAvailableSubscribes() - 1);
                                        dbConnection.updateVkLimitsForFriendRequests(vkAccount);
                                    }
                                    return share;
                                },
                                () -> dbConnection.isLinkOnCooldown(taskInformation.getLink()),
                                taskInformation, dbConnection, getRandomNumber(0, 2000));
                        break;
                    }
                    case "Просмотры записей": {
                        performTaskV2(
                                taskInfo -> vkService.showPost(vkAccount, taskInfo.getLink(), restTemplate, taskInfo.getProfileHtml()),
                                null, taskInformation, dbConnection, 9000 + getRandomNumber(1500, 6500));
                        break;
                    }
                    case "Просмотры видео": {
                        performTaskV2(
                                taskInfo -> vkService.showVideo(vkAccount, taskInfo.getLink(), restTemplate, taskInfo.getProfileHtml()),
                                null, taskInformation, dbConnection, 16000 + getRandomNumber(1500, 7500));
                        break;
                    }
                    case "Комментарии": {
                        performTaskV2(
                                taskInfo -> vkService.comment(vkAccount, taskInfo.getLink(), taskInfo.getComment(), restTemplate, taskInfo.getProfileHtml()),
                                () -> dbConnection.isLinkOnCooldown(handleVkLinks(taskInformation.getLink())),
                                taskInformation, dbConnection, getRandomNumber(1500, 3500));
                        break;
                    }
                    case "Опросы": {
                        performTaskV2(
                                taskInfo -> vkService.vote(vkAccount, handleVkLinks(taskInfo.getLink()), restTemplate, taskInfo.getVoteOption(), taskInfo.getProfileHtml()),
                                null, taskInformation, dbConnection, getRandomNumber(2500, 5500));
                        break;
                    }
                    default: {
                        System.out.println("Unknown tag: " + taskDiv);
                    }
                }
                if (scenario != null && getRandomNumber(0, 100) > 75) {
                    System.out.println("going to execute scenario for acc " + account.getVkAccount().getId() + " " + scenario);
                    scenario.accept(driver);
                    scenario = null;
                    index = 0;
                    Thread.sleep(getRandomNumber(3000, 4000));
                    taskDivs = driver.findElements(By.xpath("//div[@class='cards__col col-12 col-xxs-6 col-sm-4 col-lg-3']"));
                }
                try {
                    clickElement(driver, findElement(driver, "span[contains(text(), 'Обновить страницу')]"));
                    index = 0;
                    Thread.sleep(getRandomNumber(2500, 3400));
                    taskDivs = driver.findElements(By.xpath("//div[@class='cards__col col-12 col-xxs-6 col-sm-4 col-lg-3']"));
                    System.out.println(new Date() + " Refreshed tasks list for vk acc " + vkAccount.getId());
                } catch (Exception e) {
                    Thread.sleep(getRandomNumber(500, 3000));
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(account.getVkAccount().getId() + " " + taskDiv.getAttribute("innerHTML"));
                account.incrementVkSerfingFailsInRow();
            }
        }
    }

    private static void performTaskV2(final ThrowableFunction<TaskInfo, Boolean> task,
                                      final Supplier<Boolean> restriction,
                                      final TaskInfo taskInfo,
                                      final DbConnection dbConnection,
                                      final int delay) throws Exception {
        if (restriction != null && restriction.get()) {
            return;
        }
        Boolean result;
        try {
            result = task.apply(taskInfo);
        } catch (Exception e) {
            result = false;
        }
        Thread.sleep(sleepTime(delay, taskInfo));
        if (result) {
            final Float balance = submitTask(taskInfo.getDriver(), taskInfo.getIntermediateLink(), taskInfo.getAccount(), dbConnection);
            taskInfo.getAccount().setBalance(balance);
            System.out.println(taskInfo.getVkAccId() + " submitted task. Balance: " + balance);
            handleVkSerfingBalance(balance, taskInfo.getAccount(), dbConnection, taskInfo.getDriver());
        } else {
            skipTask(taskInfo.getDriver(), taskInfo.getIntermediateLink());
        }

    }

    private final static Map<String, Integer> thresholdSuccessPerTaskType = new HashMap<>();

    static {
        thresholdSuccessPerTaskType.put("Подписчики", 15);
        thresholdSuccessPerTaskType.put("Лайки", 10);
        thresholdSuccessPerTaskType.put("Репосты", 55);
        thresholdSuccessPerTaskType.put("Друзья", 65);
        thresholdSuccessPerTaskType.put("Просмотры записей", 50);
        thresholdSuccessPerTaskType.put("Просмотры видео", 55);
        thresholdSuccessPerTaskType.put("Комментарии", 15);
        thresholdSuccessPerTaskType.put("Опросы", 15);
    }

    private static TaskInfo startPerformingTaskForVkSerfing(final ChromeDriver driver, final WebElement taskDiv, final VkAccount vkAccount) throws Exception {
        final TaskInfo taskInfo = new TaskInfo();
        final List<WebElement> performLinks = taskDiv.findElements(By.xpath(".//a[contains(@href,'/assignments')]"));
        final String type = performLinks.get(1).getText();
//        final int chanceOfExecution = vkAccount.getVkSerfingAccount().getBalanceVkTarget() == -1 ? 100 : getRandomNumber(40, 100);
        final int chanceOfExecution = 100;
        if ("Подписчики".equals(type)
                && (vkAccount.isCantSubscribe() || vkAccount.isLimitForSubscribtions())) {
            return null;
        }
        if ("Репосты".equals(type) && vkAccount.isLimitForShares()) {
            return null;
        }
        if ("Друзья".equals(type)
                && (vkAccount.isCantAddFriends() || vkAccount.isLimitForFriendRequestsIsOver())) {
            return null;
        }
        try {
            if (chanceOfExecution < thresholdSuccessPerTaskType.get(type)) {
                System.out.println(vkAccount.getId() + " skipped " + type + " due to low chance " + chanceOfExecution);
                return null;
            }
        } catch (Exception e) {
            System.out.println("Failed to check chance for type " + type);
            e.printStackTrace();
        }
        final WebElement performLink = getRandomNumber(0, 100) > 50 ? performLinks.get(1) : performLinks.get(2);
        if (getRandomNumber(0, 100) > 80) {
            //  System.out.println("Account " + vkAccount.getId() + " is going to missclick");
            missClick(driver, performLink);
            Thread.sleep(getRandomNumber(200, 400));
        }
        closeAllTabsExceptFirstOne(driver);
        final String intermediateLink = performLink.getAttribute("href").replaceAll("https://vkserfing.ru", "");
        taskInfo.setIntermediateLink(intermediateLink);
        clickElement(driver, performLink);
        taskInfo.setType(type);
        if ("Опросы".equals(type)) {
            if (vkAccount.isSkipVotes()) {
                return null;
            }
            String option = null;
            try {
                option = waitUntilElementWillBePresent(driver, "blockquote/b").getText();
                //  option = driver.findElement(By.xpath("//blockquote/b")).getText();
            } catch (Exception e) {
                try {
                    findElement(driver, "*[contains(text(), 'любой вариант')]");
                    taskInfo.setVoteOption(getRandomNumber(1, 3));
                } catch (Exception ex) {
                    System.out.println("Failed to get option for task: \n" + taskDiv.getAttribute("innerHTML"));
                    e.printStackTrace();
                    return null;
                }
            }
            if (taskInfo.getVoteOption() == null) {
                try {
                    taskInfo.setVoteOption(Integer.valueOf(option.split("\\.")[0]));
                } catch (Exception e) {
                    System.out.println("Strange value for vote option [" + option + "] not going to vote...");
                    return null;
                }
            }
        } else if ("Комментарии".equals(type)) {
            try {
                Thread.sleep(getRandomNumber(500, 1200));
                final WebElement commentField = driver.findElements(By.xpath("//textarea")).get(1);
                final String commentMessage = commentField.getText();

                System.out.println("Comment message: " + commentMessage);
                clickElement(driver, commentField);
                taskInfo.setComment(commentMessage);
            } catch (Exception ignored) {
                clickElement(driver, "button[@class='modal-close'][@title='Закрыть']");
            }
            if (taskInfo.getComment() == null || StringUtils.isEmpty(taskInfo.getComment())) {
                skipTask(driver, intermediateLink);
                return null;
            }
        }
        try {
            Thread.sleep(getRandomNumber(200, 450));
            WebElement performButton = findElement(driver, "a[@href='" + intermediateLink + "'][@class='form__btn btn btn--block']");
            clickElement(driver, performButton);
        } catch (Exception e) {
            //   e.printStackTrace();
        }
        try {
            waitUntilElementWhichContainsTextWillBePresent(driver, "div[@class='notyf__message']", "Слишком много неудачных попыток проверки заданий", 1);
            if ("Опросы".equals(type)) {
                vkAccount.setSkipVotes(true);
                System.out.println("Vkserfing acc " + vkAccount.getId() + " - vk serfing can't check faking votes");
            } else {
                System.out.println("Vkserfing acc " + vkAccount.getId() + " - Слишком много неудачных попыток проверки заданий " + type);
            }
            return null;
        } catch (Exception e) {
//            e.printStackTrace();
        }
        try {
            waitUntilElementWhichContainsTextWillBePresent(driver, "div[@class='notyf__message']", "Задание было приостановлено", 1);
            System.out.println("Vkserfing acc " + vkAccount.getId() + " - task is not available " + intermediateLink);
            return null;
        } catch (Exception e) {
//            e.printStackTrace();
        }
        waitUntilNumbersOfWindows(driver, 10, 2);
        Thread.sleep(getRandomNumber(800, 1200));
        ArrayList<String> windows = new ArrayList<>(driver.getWindowHandles());
        try {
            driver.switchTo().window(windows.get(1));
            //Thread.sleep(getRandomNumber(3000, 5000));
            scanCookiesFromCurrentPage(driver, vkAccount, taskInfo);
        } catch (Exception e) {
            e.printStackTrace();
            driver.close();
            driver.switchTo().window(windows.get(0));
            return null;
        }
        driver.close();
        driver.switchTo().window(windows.get(0));
        return taskInfo;
    }


    private static String handleVkLinks(String link) {
        if (link.contains("?w=")) {
            String object = link.split("\\?w=")[1];
            link = "https://vk.com/" + object;
        }
        return link;
    }

    private static void handleVkSerfingBalance(final float balance, final VkSerfingAccount account, final DbConnection dbConnection, final ChromeDriver driver) throws Exception {
        if (balance > 110 && getRandomNumber(0, 100) > 80) {
            try {
                System.out.println("Account " + account.getVkAccount().getId() + " is going to invoice. Balance: " + balance);
                final WebElement balanceButton;
                if (getRandomNumber(0, 100) > 80) {
                    //   System.out.println("Account " + account.getVkAccount().getId() + " is going to missclick for balance");
                    balanceButton = missClick(driver, "a[@href='/cash']");
                } else {
                    balanceButton = null;
                }
                if (balanceButton == null) {
                    clickElement(driver, "a[@href='/cash']");
                } else {
                    clickElement(driver, balanceButton);
                }
                Thread.sleep(getRandomNumber(300, 1000));
                clickElement(driver, "a[@href='/cashout'][@class='btn btn--light btn--block']");
                Thread.sleep(getRandomNumber(300, 1000));
                clickElement(driver, "div[@class='p-balance__action-right']");
                Thread.sleep(getRandomNumber(800, 1300));
                final WebElement payeerTypeButton = driver.findElement(By.xpath("//span[@class='pay-type__name'][contains(text(), 'Payeer')]"));
                clickElement(driver, payeerTypeButton.findElement(By.xpath("./..")));
                final PayeerAccount payeerAccount = account.getVkAccount().getPayeerAccount();
                Thread.sleep(getRandomNumber(300, 1000));
                List<WebElement> inputs = driver.findElements(By.xpath("//input"));
                final WebElement payeerFieldName = inputs.get(inputs.size() - 1);
                Thread.sleep(getRandomNumber(300, 1000));
                clickElement(driver, payeerFieldName);
                Thread.sleep(getRandomNumber(200, 450));
                payeerFieldName.sendKeys(payeerAccount.getAccName());
                Thread.sleep(getRandomNumber(400, 800));
                clickElement(driver, "button[@type='submit'][@class='form__btn btn btn--block']");
                Thread.sleep(4000, 7000);
                float newBalance = Float.parseFloat(driver.findElement(By.xpath("//a[@href='/cash']")).getText().replaceAll(" ₽", ""));
                if (newBalance < 100) {
                    account.setBalance(newBalance);
                    dbConnection.updateVKSerfingBalance(account.getId(), newBalance);
                    dbConnection.updatePayeerBalance(payeerAccount.getId(), balance - 9);
                    clickElement(driver, "a[@href='/assignments'][contains(@class, 'js-section-link header__menu-link')]");
                    dbConnection.logInvoice(account.getVkAccount().getId(), balance, "vkserfing");
                    System.out.println("Account " + account.getVkAccount().getId() + " successfully invoiced");
                } else {
                    System.out.println("Account " + account.getVkAccount().getId() + " failed to invoice - going to retry later");
                }
            } catch (Exception e) {
                System.out.println("Failed to invoice acc " + account.getVkAccount().getId());
                e.printStackTrace();
            }
        } else {
            dbConnection.updateVKSerfingBalance(account.getId(), balance);
        }
    }

    private static void setAccCooldown(final DbConnection dbConnection, final VkSerfingAccount account, final String taskId, final int cooldown) throws Exception {
        final Date coolDown = new Date();
        coolDown.setTime(coolDown.getTime() + cooldown);
        account.setCoolDown(coolDown);
        dbConnection.setVkSerfingCooldown(coolDown, account.getId());
        System.out.println("Cooldown for acc [" + account.getVkAccount().getId() + "] " + coolDown + " task [" + taskId + "]");
        account.setCoolDownIsSet(true);
    }


    private static TaskInfo scanCookiesFromCurrentPage(final ChromeDriver driver, final VkAccount vkAccount, TaskInfo taskInfo) throws Exception {
        if (taskInfo == null) {
            taskInfo = new TaskInfo();
        }
        Date start = new Date();
        try {
            waitUntilElementWillBePresent(driver, "div[@id='page_wrap']", 10);
        } catch (Exception e) {
            System.out.println(vkAccount.getId() + " start: " + start + " end: " + new Date() + " " + driver.getCurrentUrl());
            //  System.out.println(driver.findElement(By.xpath("//a[@id='top_profile_link']")));
            throw e;
        }
        taskInfo.setVkAccId(vkAccount.getId());
        String link = driver.getCurrentUrl();
        if (link.contains("?w=") && !link.contains("_r")) {
//            String[] adsLink = link.split("_");
//            if (adsLink.length == 2) {
//                link = "https://vk.com/" + link.substring(link.lastIndexOf("?w=") + 3, link.lastIndexOf("_"));
//            } else {
            link = "https://vk.com/" + link.split("\\?w=")[1];
//            }
            driver.get(link);
        }
        taskInfo.setLink(link);
        try {
            new WebDriverWait(driver, Duration.ofSeconds(getRandomNumber(10, 20))).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[@id='top_profile_link']")));
        } catch (Exception e) {
            waitUntilElementWillBePresent(driver, "button[@class='quick_login_button flat_button button_wide']", 10).click();
            waitUntilElementWillBePresent(driver, "input[@name='login']").sendKeys(vkAccount.getPhone());
            System.out.println("Going to login into vk acc " + vkAccount.getId());
            findElement(driver, "button[@type='submit']").click();
            Thread.sleep(5000);
            try {
                findElement(driver, "input[@type='password']").sendKeys(vkAccount.getPassword());
            } catch (Exception ez) {
                findElement(driver, "button[@type='button']/span").click();
                waitUntilElementWillBePresent(driver, "input[@type='password']").sendKeys(vkAccount.getPassword());
            }
            findElement(driver, "button[@type='submit']").click();
            new WebDriverWait(driver, Duration.ofSeconds(45)).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[@id='top_profile_link']")));
        }
        final Date startTime = new Date();
        int index = 0;
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            final StringBuilder cookies = new StringBuilder();
            for (Cookie cookie : driver.manage().getCookies()) {
                cookies.append(cookie.getName()).append("=").append(cookie.getValue()).append(";");
            }
            if (StringUtils.isNotEmpty(cookies.toString()) && cookies.toString().contains("remixnsid")) {
                vkAccount.setCookie(cookies.toString());
                // System.out.println("Successfully scanned cookies for vk acc " + vkAccount.getId() + ": " + vkAccount.getCookie());
                while (true) {
                    String script = "for (var i = 0; i < localStorage.length; i++){"
                            + "   var key = window.localStorage.key(i);"
                            + "   if(key.includes('web_token:login:auth'))"
                            + "   {return window.localStorage.getItem(key);}"
                            + "}";
                    final String authItem = (String) driver.executeScript(script);
                    final Matcher matcher = Pattern.compile("\"access_token\":\"(.*?)\"", Pattern.DOTALL).matcher(authItem);
                    if (authItem.equals("1")) {
                        Thread.sleep(500);
                        continue;
                    }
                    if (matcher.find()) {
                        vkAccount.setAccessToken(matcher.group(1));
                    } else {
                        System.out.println(new Date() + " Failed to get access token for vk acc " + vkAccount.getId());
                        System.out.println(authItem);
                    }
                    break;
                }
                break;
            } else if (index > 60) {
                System.out.println("Failed to update cookies for vk acc " + vkAccount.getId());
                break;
            } else {
                index++;
            }
        }
//        String currentUrl = driver.getCurrentUrl();
//        AtomicReference<String> responseBody = new AtomicReference<>();
//        while (true) {
//            DevTools chromeDevTools = driver.getDevTools();
//
//            chromeDevTools.createSession();
//            chromeDevTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
//            chromeDevTools.addListener(Network.loadingFinished(),
//                    entry -> {
//                        try {
//                            String htmlResponseBody = chromeDevTools.send(Network.getResponseBody(entry.getRequestId())).getBody();
//                            if (responseBody.get() == null && htmlResponseBody.contains("<!DOCTYPE html>")) {
//                                responseBody.set(htmlResponseBody);
//                            }
//                        } catch (Exception e) {
//
//                        }
//                    });
//            driver.get(currentUrl);
//            chromeDevTools.send(Network.disable());
//            index = 0;
//            while (responseBody.get() == null) {
//                index++;
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                if (index > 3) {
//                    System.out.println("failed to get html for acc " + vkAccount.getId() + " link: " + currentUrl + " going to retry to acquire html");
//                    break;
//                }
//            }
//            chromeDevTools.clearListeners();
//            chromeDevTools.close();
//            if (responseBody.get() != null) {
//                break;
//            }
//        }
        int loads = 0;
        try {
            taskInfo.setProfileHtml(driver.findElement(By.xpath("//body")).getAttribute("innerHTML"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (taskInfo.getProfileHtml() == null) {
            loads++;
            driver.get(link);
            try {
                new WebDriverWait(driver, Duration.ofSeconds(45)).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[@id='top_profile_link']")));
            } catch (Exception e) {
                System.out.println("Failed to load link " + link + " for acc " + vkAccount.getId());
                e.printStackTrace();
            }
            Thread.sleep(1000);
            try {
                taskInfo.setProfileHtml(driver.findElement(By.xpath("//div[@id='page_wrap']")).getAttribute("innerHTML"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (loads > 30) {
                break;
            }
        }
        taskInfo.setExecuteTime(new Date().getTime() - startTime.getTime());
        if (taskInfo.getProfileHtml() == null) {
            System.out.println("Provided null html for " + taskInfo.getLink() + " for acc " + vkAccount.getId());
        }
        try {
            String title = findElement(driver, "title").getText();
            if (title.contains("Запись удалена")
                    || title.contains("Post deleted")) {
                taskInfo.setProfileHtml(null);
            }
        } catch (Exception e) {

        }
        //    taskInfo.setProfileHtml(waitUntilElementWillBePresent(driver, "body").getAttribute("innerHTML"));
        return taskInfo;
    }

//    private static final Map<String, SubscriptionInfo> subInfo = new HashMap<>();
//
//    private static void processSubscription(final String link, final Integer accId) {
//        try {
//            SubscriptionInfo subscriptionInfo = subInfo.get(link);
//            if (subscriptionInfo == null) {
//                subscriptionInfo = new SubscriptionInfo(accId);
//                subInfo.put(link, subscriptionInfo);
//            } else {
//                subscriptionInfo.addAccId(accId, link);
//            }
//        } catch (Exception e) {
//            System.out.println("Failed to process subscription for acc " + accId);
//            e.printStackTrace();
//        }
//    }
//
//    private static boolean isSubscriptionOnCooldown(final String link) {
//        SubscriptionInfo subscriptionInfo = subInfo.get(link);
//        if (subscriptionInfo != null) {
//            if (subscriptionInfo.isOnCooldown()) {
//                System.out.println(link + " is on cooldown");
//                return true;
//            }
//        }
//        return false;
//    }

    private static Float submitTask(final ChromeDriver driver, final String intermediateLink, final VkSerfingAccount account, DbConnection dbConnection) throws Exception {
        final WebElement button;
        if (getRandomNumber(0, 100) > 90) {
            // System.out.println("Account " + account.getVkAccount().getId() + " is going to missclick on submit button");
            button = missClick(driver, "a[@href='" + intermediateLink + "'][@class='task__do']");
        } else {
            button = null;
        }
        if (button == null) {
            clickElement(driver, "a[@href='" + intermediateLink + "'][@class='task__do']");
        } else {
            Thread.sleep(getRandomNumber(200, 400));
            clickElement(driver, button);
        }
        Thread.sleep(1000, 1500);
        float newBalance = Float.parseFloat(driver.findElement(By.xpath("//a[@href='/cash']")).getText().replaceAll(" ₽", ""));
        if (account.getBalance() != null && newBalance == account.getBalance()) {
            try {
                waitUntilElementWillBePresent(driver, "div[@class='notyf__message'][text()='Для продолжения работы вам требуется пройти верификацию']", 2);
                Thread.sleep(getRandomNumber(2000, 4000));
                driver.navigate().refresh();
                Thread.sleep(getRandomNumber(5000, 10000));
                if (!verifyVkSerfing(driver, dbConnection, account, account.getVkAccount(), null)) {
                    driver.quit();
                    localRestTemplate.exchange(BASE_URL + "/api/v1/browser/stop?user_id=" + account.getVkAccount().getBrowserProfile(), HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);
                }
            } catch (Exception e) {
            }
            System.out.println(account.getVkAccount().getId() + " failed to perform task " + intermediateLink);
            account.incrementVkSerfingFailsInRow();
            Map<String, Integer> accFailedTasks = failedTasks.get(account.getId());
            if (accFailedTasks == null) {
                accFailedTasks = new HashMap<>();
                accFailedTasks.put(intermediateLink, 1);
                failedTasks.put(account.getId(), accFailedTasks);
            } else {
                accFailedTasks.merge(intermediateLink, 1, Integer::sum);
            }
        } else {
            account.setVkserfingFailsInRow(0);
        }
        return newBalance;
    }

    private static void skipTask(final ChromeDriver driver, final String intermediateLink) throws Exception {
        WebElement performTaskButton = waitUntilElementWillBePresent(driver, "a[@href='" + intermediateLink + "'][@class='task__do']");
        WebElement taskBottomPanel = performTaskButton.findElement(By.xpath("./.."));
        WebElement taskGroup = taskBottomPanel.findElement(By.xpath("./.."));
        List<WebElement> options = taskGroup.findElements(By.xpath("./div[@class='task__top']/ul/li[@class='card-menu__item']"));
        final Actions builder = new Actions(driver);
        Dimension size = options.get(1).getSize();
        builder.moveToElement(options.get(1), getRandomNumber(size.width / 3 * -1 + 2, size.width / 3 - 2), getRandomNumber(size.height / 3 * -1 + 2, size.height / 3 - 2))
                .pause(Duration.ofMillis(getRandomNumber(500, 1000)))
                .click().build().perform();
        WebElement messageBlock = waitUntilElementWithTextWillBePresent(driver, "div", "Вы уверены что хотите скрыть данное задание?");
        WebElement messageContainer = messageBlock.findElement(By.xpath("./.."));
        List<WebElement> yesNoOptions = messageContainer.findElements(By.xpath("./div[@class='row']/div[@class='col-6']"));
        WebElement confirmButton = yesNoOptions.get(0).findElement(By.xpath("./a"));
        Thread.sleep(getRandomNumber(300, 600));
        clickElement(driver, confirmButton);
    }

    public static class VLikeScenarious {
        public static void checkYouTubeTasks(final ChromeDriver driver) throws Exception {
            clickElement(driver, "a[@href='https://v-like.ru/tasks/yt']");
            Thread.sleep(getRandomNumber(1000, 2000));
            clickElement(driver, "a[@href='https://v-like.ru/tasks/vk']");
        }

        public static void checkProfile(final ChromeDriver driver) throws Exception {
            clickElement(driver, "a[@href='https://v-like.ru/account/payments']");
            Thread.sleep(getRandomNumber(1000, 2000));
            clickElement(driver, "a[@href='https://v-like.ru/tasks/vk']");
        }

        public static void checkWithdraws(final ChromeDriver driver) throws Exception {
            clickElement(driver, "a[@href='https://v-like.ru/account/payments']");
            Thread.sleep(getRandomNumber(600, 800));
            clickElement(driver, "a[@href='https://v-like.ru/account/withdraws']");
            Thread.sleep(getRandomNumber(2000, 3000));
            clickElement(driver, "a[@href='https://v-like.ru/tasks/vk']");
        }

        public static void checkHistory(final ChromeDriver driver) throws Exception {
            clickElement(driver, "a[@href='https://v-like.ru/account/payments']");
            Thread.sleep(getRandomNumber(600, 800));
            clickElement(driver, "a[@href='https://v-like.ru/account/history']");
            Thread.sleep(getRandomNumber(2000, 3000));
            clickElement(driver, "a[@href='https://v-like.ru/tasks/vk']");
        }

        public static void checkSettings(final ChromeDriver driver) throws Exception {
            clickElement(driver, "a[@href='https://v-like.ru/account/payments']");
            Thread.sleep(getRandomNumber(600, 800));
            clickElement(driver, "a[@href='https://v-like.ru/account/settings']");
            Thread.sleep(getRandomNumber(1000, 2000));
            clickElement(driver, "a[@href='https://v-like.ru/tasks/vk']");
        }
    }

    private static final List<Consumer<ChromeDriver>> vlikeScenarious = Arrays.asList(
            driver -> {
                try {
                    VLikeScenarious.checkYouTubeTasks(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },
            driver -> {
                try {
                    VLikeScenarious.checkProfile(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },
            driver -> {
                try {
                    VLikeScenarious.checkWithdraws(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },
            driver -> {
                try {
                    VLikeScenarious.checkHistory(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },
            driver -> {
                try {
                    VLikeScenarious.checkSettings(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
    );

    public static class VkTargetScenarious {
        public static void clickStatisticForWeek(final ChromeDriver driver) throws Exception {
            clickElement(driver, "div[@data-chart-mode='week']");
        }

        public static void clickStatisticForAllTime(final ChromeDriver driver) throws Exception {
            clickElement(driver, "div[@data-chart-mode='all']");
        }

        public static void clickNextPageForNews(final ChromeDriver driver) throws Exception {
            WebElement element = driver.findElement(By.xpath("(//div[@class='news__btns__wrap']/div)[2]"));
            clickElement(driver, element);
        }

        public static void switchToNightMode(final ChromeDriver driver) throws Exception {
            WebElement input = waitUntilElementWillBePresent(driver, "input[@id='i_night']");
            if (input.getDomProperty("checked").equals("false")) {
                clickElement(driver, input.findElement(By.xpath("./..")));
            }
        }
    }

    private static final List<Consumer<ChromeDriver>> vkTargetScenarious = Arrays.asList(
            driver -> {
                try {
                    VkTargetScenarious.clickStatisticForWeek(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },
            driver -> {
                try {
                    VkTargetScenarious.clickStatisticForAllTime(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },
            driver -> {
                try {
                    VkTargetScenarious.clickNextPageForNews(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },
            driver -> {
                try {
                    VkTargetScenarious.switchToNightMode(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
    );

    public static class PayUpScenarious {
        public static final List<Consumer<ChromeDriver>> scenarious = Arrays.asList(
                driver -> {
                    try {
                        checkDashboardInfo(driver);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                driver -> {
                    try {
                        checkReferrals(driver);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                driver -> {
                    try {
                        checkPayouts(driver);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                driver -> {
                    try {
                        checkNews(driver);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                driver -> {
                    try {
                        checkMacros(driver);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                driver -> {
                    try {
                        checkContest(driver);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                driver -> {
                    try {
                        checkMyProfile(driver);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                driver -> {
                    try {
                        checkGeneralSettings(driver);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                driver -> {
                    try {
                        checkPayoutSettings(driver);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                driver -> {
                    try {
                        checkSecurity(driver);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        );

        public static void perform(final ChromeDriver driver, PayUpVideoAcc payUpVideoAcc) throws Exception {
            try {
                fireRandomScenario(driver, payUpVideoAcc);
                Thread.sleep(getRandomNumber(2000, 5000));
                if (checkForChance(45)) {
                    fireRandomScenario(driver, payUpVideoAcc);
                }
                Thread.sleep(getRandomNumber(2000, 5000));
                if (checkForChance(15)) {
                    fireRandomScenario(driver, payUpVideoAcc);
                }
                Thread.sleep(getRandomNumber(2000, 5000));
                backToYouTubeTab(driver);
            } catch (Exception e) {
                e.printStackTrace();
                driver.navigate().refresh();
                Thread.sleep(getRandomNumber(2000, 3500));
                backToYouTubeTab(driver);
            }
        }

        public static void performTest(final ChromeDriver driver) throws Exception {
            for (int index = 5; index < scenarious.size(); index++) {
                try {
                    scenarious.get(index).accept(driver);
                    Thread.sleep(2000, 5000);
                    backToYouTubeTab(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                    Thread.sleep(9999999);
                }
            }

        }

        private static void backToYouTubeTab(final ChromeDriver driver) throws Exception {
            List<WebElement> yotubeHrefs = driver.findElements(By.xpath("//a[@href='/tasks/video/']"));
            if (fiftyFifty()) {
                clickElementWithMissClick(driver, yotubeHrefs.get(0), 40);
            } else {
                clickElementWithMissClick(driver, yotubeHrefs.get(1), 40);
            }
        }

        private static void fireRandomScenario(final ChromeDriver driver, PayUpVideoAcc payUpVideoAcc) {
            int scenarioIndex = getRandomNumber(0, scenarious.size() - 1);
            System.out.println(new Date() + " PayUp account [" + payUpVideoAcc.getVkAccount().getId() + "] going to execute scenario " + scenarioIndex);
            scenarious.get(scenarioIndex).accept(driver);
        }

        private static void checkDashboardInfo(final ChromeDriver driver) throws Exception {
            if (fiftyFifty()) {
                clickElement(driver, "a[@href='/dashboard/'][contains(@class, 'menu-link')]");
            } else {
                clickElementWithMissClick(driver, "div[@class='logo']", 60);
            }
            Thread.sleep(1000, 2000);
            if (checkForChance(35)) {
                clickElementWithMissClick(driver, "div[@id='activity-online-users']/button", 50);
                Thread.sleep(1500, 4500);
                clickElementWithMissClick(driver, waitUntilElementWhichContainsTextWillBePresent(driver, "button", "Got it"), 40);
            }
        }

        private static void checkReferrals(final ChromeDriver driver) throws Exception {
            clickElementWithMissClick(driver, "a[@href='/partners/'][contains(@class, 'menu-link')]", 40);
        }

        private static void checkPayouts(final ChromeDriver driver) throws Exception {
            clickElementWithMissClick(driver, "a[@href='/payout/'][contains(@class, 'menu-link')]", 40);
        }

        private static void checkNews(final ChromeDriver driver) throws Exception {
            clickElementWithMissClick(driver, "a[@href='/news/'][contains(@class, 'menu-link')]", 40);
        }

        private static void checkMacros(final ChromeDriver driver) throws Exception {
            clickElementWithMissClick(driver, "a[@href='/macros/'][contains(@class, 'menu-link')]", 40);
        }

        private static void checkContest(final ChromeDriver driver) throws Exception {
            clickElementWithMissClick(driver, "a[@href='/contests/'][contains(@class, 'menu-link')]", 40);
            if (checkForChance(65)) {
                clickElementWithMissClick(driver, "a[@href='/contests/all/']", 25);
            }
        }

        private static void checkMyProfile(final ChromeDriver driver) throws Exception {
            clickElementWithMissClick(driver, "div[@class='dropdown dropdown-inline dropdown-user-profile']", 40);
            Thread.sleep(1000, 2500);
            clickElement(driver, waitUntilElementWithTextWillBePresent(driver, "span", "My profile").findElement(By.xpath("./..")).findElement(By.xpath("./..")));
        }

        private static void checkGeneralSettings(final ChromeDriver driver) throws Exception {
            clickElementWithMissClick(driver, "div[@class='dropdown dropdown-inline dropdown-user-profile']", 40);
            Thread.sleep(1000, 2500);
            clickElement(driver, waitUntilElementWithTextWillBePresent(driver, "span", "General settings").findElement(By.xpath("./..")).findElement(By.xpath("./..")));
        }

        private static void checkPayoutSettings(final ChromeDriver driver) throws Exception {
            clickElementWithMissClick(driver, "div[@class='dropdown dropdown-inline dropdown-user-profile']", 40);
            Thread.sleep(1000, 2500);
            clickElement(driver, waitUntilElementWithTextWillBePresent(driver, "span", "Payout settings").findElement(By.xpath("./..")).findElement(By.xpath("./..")));
        }

        private static void checkSecurity(final ChromeDriver driver) throws Exception {
            clickElementWithMissClick(driver, "div[@class='dropdown dropdown-inline dropdown-user-profile']", 40);
            Thread.sleep(1000, 2500);
            clickElement(driver, waitUntilElementWithTextWillBePresent(driver, "span", "Security").findElement(By.xpath("./..")).findElement(By.xpath("./..")));
        }
    }

    public static class VkSerfingScenarious {
        public static void checkExecutedTasks(final ChromeDriver driver) throws Exception {
            clickElement(driver, "a[@href='/assignments/logs']");
            Thread.sleep(getRandomNumber(1000, 4500));
            if (random()) {
                clickElement(driver, "a[@href='/assignments'][contains(@class, 'sidebar__link')]");
            } else {
                clickElement(driver, "a[@href='/assignments'][contains(@class, 'js-section-link header__menu-link is-active')]");
            }
        }

        public static void checkFails(final ChromeDriver driver) throws Exception {
            clickElement(driver, "a[@href='/assignments/fails']");
            Thread.sleep(getRandomNumber(1000, 4500));
            if (random()) {
                clickElement(driver, "a[@href='/assignments'][contains(@class, 'sidebar__link')]");
            } else {
                clickElement(driver, "a[@href='/assignments'][contains(@class, 'js-section-link header__menu-link is-active')]");
            }
        }

        public static void checkHelp(final ChromeDriver driver) throws Exception {
            clickElement(driver, "a[@href='/help'][contains(@class, 'js-section-link header__menu-link')]");
            Thread.sleep(getRandomNumber(1000, 5000));
            clickElement(driver, "a[@href='/assignments'][contains(@class, 'js-section-link header__menu-link')]");
        }

        public static void checkHelpAndAccountAndProfiles(final ChromeDriver driver) throws Exception {
            clickElement(driver, "a[@href='/help'][contains(@class, 'js-section-link header__menu-link')]");
            Thread.sleep(getRandomNumber(1000, 3000));
            clickElement(driver, "a[@href='/help/accounts']");
            Thread.sleep(getRandomNumber(1000, 5000));
            clickElement(driver, "a[@href='/assignments'][contains(@class, 'js-section-link header__menu-link')]");
        }

        public static void checkHelpAndViolations(final ChromeDriver driver) throws Exception {
            clickElement(driver, "a[@href='/help'][contains(@class, 'js-section-link header__menu-link')]");
            Thread.sleep(getRandomNumber(1000, 3000));
            clickElement(driver, "a[@href='/help/violations']");
            Thread.sleep(getRandomNumber(1000, 5000));
            clickElement(driver, "a[@href='/assignments'][contains(@class, 'js-section-link header__menu-link')]");
        }

        public static void checkHelpAndAssignments(final ChromeDriver driver) throws Exception {
            clickElement(driver, "a[@href='/help'][contains(@class, 'js-section-link header__menu-link')]");
            Thread.sleep(getRandomNumber(1000, 3000));
            clickElement(driver, "a[@href='/help/assignments']");
            Thread.sleep(getRandomNumber(1000, 5000));
            clickElement(driver, "a[@href='/assignments'][contains(@class, 'js-section-link header__menu-link')]");
        }

        public static void checkHelpAndSettings(final ChromeDriver driver) throws Exception {
            clickElement(driver, "a[@href='/help'][contains(@class, 'js-section-link header__menu-link')]");
            Thread.sleep(getRandomNumber(1000, 3000));
            clickElement(driver, "a[@href='/help/settings']");
            Thread.sleep(getRandomNumber(1000, 5000));
            clickElement(driver, "a[@href='/assignments'][contains(@class, 'js-section-link header__menu-link')]");
        }

        public static void checkHelpAndPayments(final ChromeDriver driver) throws Exception {
            clickElement(driver, "a[@href='/help'][contains(@class, 'js-section-link header__menu-link')]");
            Thread.sleep(getRandomNumber(1000, 3000));
            clickElement(driver, "a[@href='/help/payments']");
            Thread.sleep(getRandomNumber(1000, 5000));
            clickElement(driver, "a[@href='/assignments'][contains(@class, 'js-section-link header__menu-link')]");
        }

        public static void checkHelpAndOrders(final ChromeDriver driver) throws Exception {
            clickElement(driver, "a[@href='/help'][contains(@class, 'js-section-link header__menu-link')]");
            Thread.sleep(getRandomNumber(1000, 3000));
            clickElement(driver, "a[@href='/help/orders']");
            Thread.sleep(getRandomNumber(1000, 5000));
            clickElement(driver, "a[@href='/assignments'][contains(@class, 'js-section-link header__menu-link')]");
        }

        public static void checkHelpAndTaskVerifications(final ChromeDriver driver) throws Exception {
            clickElement(driver, "a[@href='/help'][contains(@class, 'js-section-link header__menu-link')]");
            Thread.sleep(getRandomNumber(1000, 3000));
            clickElement(driver, "a[@href='/help/check_users']");
            Thread.sleep(getRandomNumber(1000, 5000));
            clickElement(driver, "a[@href='/assignments'][contains(@class, 'js-section-link header__menu-link')]");
        }

        public static void checkHelpAndReferrals(final ChromeDriver driver) throws Exception {
            clickElement(driver, "a[@href='/help'][contains(@class, 'js-section-link header__menu-link')]");
            Thread.sleep(getRandomNumber(1000, 3000));
            clickElement(driver, "a[@href='/help/ref']");
            Thread.sleep(getRandomNumber(1000, 5000));
            clickElement(driver, "a[@href='/assignments'][contains(@class, 'js-section-link header__menu-link')]");
        }

        public static void checkHelpAndOthers(final ChromeDriver driver) throws Exception {
            clickElement(driver, "a[@href='/help'][contains(@class, 'js-section-link header__menu-link')]");
            Thread.sleep(getRandomNumber(1000, 3000));
            clickElement(driver, "a[@href='/support/add']");
            Thread.sleep(getRandomNumber(1000, 5000));
            clickElement(driver, "a[@href='/assignments'][contains(@class, 'js-section-link header__menu-link')]");
        }

        public static boolean random() {
            return getRandomNumber(0, 100) > 50;
        }
    }


    public static final List<Consumer<ChromeDriver>> vkSerfingScenarious = Arrays.asList(
            driver -> {
                try {
                    VkSerfingScenarious.checkExecutedTasks(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },
            driver -> {
                try {
                    VkSerfingScenarious.checkFails(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },
            driver -> {
                try {
                    VkSerfingScenarious.checkHelp(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },
            driver -> {
                try {
                    VkSerfingScenarious.checkHelpAndAccountAndProfiles(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },
            driver -> {
                try {
                    VkSerfingScenarious.checkHelpAndViolations(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },
            driver -> {
                try {
                    VkSerfingScenarious.checkHelpAndAssignments(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },
            driver -> {
                try {
                    VkSerfingScenarious.checkHelpAndSettings(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },
            driver -> {
                try {
                    VkSerfingScenarious.checkHelpAndPayments(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },
            driver -> {
                try {
                    VkSerfingScenarious.checkHelpAndOrders(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },
            driver -> {
                try {
                    VkSerfingScenarious.checkHelpAndTaskVerifications(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },
            driver -> {
                try {
                    VkSerfingScenarious.checkHelpAndReferrals(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },
            driver -> {
                try {
                    VkSerfingScenarious.checkHelpAndOthers(driver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
    );//12

}
