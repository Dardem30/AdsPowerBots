package util;

import bo.VkAccount;
import da.DbConnection;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.MoveTargetOutOfBoundsException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;

public class SeleniumUtils {
    public static void clickElement(final ChromeDriver driver, final WebElement element) throws Exception {
        int fails = 0;
        while (fails < 3) {
            try {
                final Actions builder = new Actions(driver);
                Dimension size = element.getSize();
                builder.moveToElement(element,
                                getRandomNumber(size.width / 2 * -1 + 2, size.width / 2 - 2),
                                getRandomNumber(size.height / 2 * -1 + 2, size.height / 2 - 2))
                        .pause(Duration.ofMillis(getRandomNumber(200, 500)))
                        .click().build().perform();
//                if (fails != 0) {
//                    System.out.println("clickButton 1453 successfull");
//                }
                return;
            } catch (MoveTargetOutOfBoundsException | ElementNotInteractableException e) {
                fails++;
         //       System.out.println(e.getClass().getSimpleName() + " - going to retry clickButton 1453");
                Thread.sleep(getRandomNumber(300, 600));
            }
        }
    }
    public static void clickElementWithoutPause(final ChromeDriver driver, final WebElement element) throws Exception {
        int fails = 0;
        while (fails < 3) {
            try {
                final Actions builder = new Actions(driver);
                Dimension size = element.getSize();
                builder.moveToElement(element,
                                getRandomNumber(size.width / 2 * -1 + 2, size.width / 2 - 2),
                                getRandomNumber(size.height / 2 * -1 + 2, size.height / 2 - 2))
                        .click().build().perform();
//                if (fails != 0) {
//                    System.out.println("clickButton 1453 successfull");
//                }
                return;
            } catch (MoveTargetOutOfBoundsException | ElementNotInteractableException e) {
                fails++;
         //       System.out.println(e.getClass().getSimpleName() + " - going to retry clickButton 1453");
                Thread.sleep(getRandomNumber(300, 600));
            }
        }
    }
    public static void clickElementWithoutPause(final ChromeDriver driver,
                                                final WebElement element,
                                                final int preserveHeight
    ) throws Exception {
        int fails = 0;
        while (fails < 3) {
            try {
                final Actions builder = new Actions(driver);
                Dimension size = element.getSize();
                builder.moveToElement(element,
                                getRandomNumber(size.width / 2 * -1 + 2, size.width / 2 - 2),
                                getRandomNumber(size.height / 2 * -1 + 2 + preserveHeight, size.height / 2 - 2 - preserveHeight))
                        .click().build().perform();
//                if (fails != 0) {
//                    System.out.println("clickButton 1453 successfull");
//                }
                return;
            } catch (MoveTargetOutOfBoundsException | ElementNotInteractableException e) {
                fails++;
         //       System.out.println(e.getClass().getSimpleName() + " - going to retry clickButton 1453");
                Thread.sleep(getRandomNumber(300, 600));
            }
        }
    }
    //ось начинается ровно в середине элемента - ахуеть!!!!
    public static void clickElement(final ChromeDriver driver, final WebElement element, final int x, final int y) throws Exception {


//        driver.executeScript("document.addEventListener('click', function(e) {" +
//                "var xPosition = e.clientX; " +
//                "var yPosition = e.clientY;" +
//                "console.log('Clicked at: ' + xPosition + ', ' + yPosition);" +
//                "var marker = document.createElement('div');" +
//                "marker.style.position = 'absolute';" +
//                "marker.style.top = yPosition + 'px'; " +
//                "marker.style.left = xPosition + 'px'; " +
//                "marker.style.width = '10px'; " +
//                "marker.style['z-index'] = 999999; " +
//                "marker.style.height = '10px';" +
//                "marker.style.backgroundColor = 'red';" +
//                "document.body.appendChild(marker);" +
//                "});");

        Dimension size = element.getSize();
        int centerX = x - (size.width / 2);
        int centerY = y - (size.height / 2);
        new Actions(driver)
                .moveToElement(element, centerX, centerY)
                .click()
                .build()
                .perform();
////        System.out.println("id " + element.getAttribute("id"));
////        System.out.println("absoluteX " + element.getLocation().getX());
////        System.out.println("absoluteY " + element.getLocation().getY());
////        System.out.println("offsetTop " + element.getAttribute("offsetTop"));
////        System.out.println("offsetLeft " + element.getAttribute("offsetLeft"));
////        int absoluteX = element.getLocation().getX() + x;
////        int absoluteY = element.getLocation().getY() + y;
//        new Actions(driver)
//                .moveByOffset(0, 0)
//                .click()
//                .build()
//                .perform();
//        new Actions(driver)
//                .moveByOffset(50, 50)
//                .click()
//                .build()
//                .perform();
    }
    public static void centerTopLeftAxis() {
        //@ToDO
    }

    public static int getRandomNumber(int min, int max) {
        max += 1;
        return (int) ((Math.random() * (max - min)) + min);
    }

    public static WebElement waitUntilElementWithTextWillBePresent(final ChromeDriver driver, final String tag, final String text) {
        final By searchCriteria = By.xpath("//" + tag + "[text()='" + text + "']");
        new WebDriverWait(driver, Duration.ofSeconds(60)).until(ExpectedConditions.presenceOfElementLocated(searchCriteria));
        return driver.findElement(searchCriteria);

    }

    public static WebElement waitUntilElementWillBePresent(final ChromeDriver driver, final String xpath) throws Exception {
        final By searchCriteria = By.xpath("//" + xpath);
        new WebDriverWait(driver, Duration.ofSeconds(30)).until(ExpectedConditions.presenceOfElementLocated(searchCriteria));
        Thread.sleep(2000);
        return driver.findElement(searchCriteria);
    }

    public static WebElement waitUntilElementWillBePresent(final ChromeDriver driver, final String xpath, long timeout) throws Exception {
        final By searchCriteria = By.xpath("//" + xpath);
        new WebDriverWait(driver, Duration.ofSeconds(timeout)).until(ExpectedConditions.presenceOfElementLocated(searchCriteria));
        Thread.sleep(2000);
        return driver.findElement(searchCriteria);
    }

    public static WebElement clickElement(final ChromeDriver driver, final String xpath) throws Exception {
        int fails = 0;
        while (fails < 3) {
            try {
                final WebElement element = waitUntilElementWillBePresent(driver, xpath);
                final Actions builder = new Actions(driver);
                Dimension size = element.getSize();
                builder.moveToElement(element, getRandomNumber(size.width / 2 * -1 + 2, size.width / 2 - 2), getRandomNumber(size.height / 2 * -1 + 2, size.height / 2 - 2))
                        .pause(Duration.ofMillis(getRandomNumber(200, 500)))
                        .click().build().perform();
//                if (fails != 0) {
//                    System.out.println("clickButton 1393 successfull");
//                }
                return element;
            } catch (MoveTargetOutOfBoundsException | ElementNotInteractableException e) {
                fails++;
              //  System.out.println(e.getClass().getSimpleName() + " - going to retry clickButton 1393");
                Thread.sleep(getRandomNumber(300, 600));
            }
        }
        return null;
    }
    public static WebElement clickElementWithMissClick(final ChromeDriver driver, final String xpath, final int chanceToMissClick) throws Exception {
        WebElement element = null;
        if (checkForChance(chanceToMissClick)) {
            element = missClick(driver, xpath);
        }
        int fails = 0;
        while (fails < 3) {
            try {
                if (element == null) {
                    element = waitUntilElementWillBePresent(driver, xpath);
                }
                final Actions builder = new Actions(driver);
                Dimension size = element.getSize();
                builder.moveToElement(element, getRandomNumber(size.width / 2 * -1 + 2, size.width / 2 - 2), getRandomNumber(size.height / 2 * -1 + 2, size.height / 2 - 2))
                        .pause(Duration.ofMillis(getRandomNumber(200, 500)))
                        .click().build().perform();
//                if (fails != 0) {
//                    System.out.println("clickButton 1393 successfull");
//                }
                return element;
            } catch (MoveTargetOutOfBoundsException | ElementNotInteractableException e) {
                fails++;
              //  System.out.println(e.getClass().getSimpleName() + " - going to retry clickButton 1393");
                Thread.sleep(getRandomNumber(300, 600));
            }
        }
        return null;
    }
    public static WebElement clickElementWithMissClick(final ChromeDriver driver,
                                                       final WebElement element,
                                                       final int chanceToMissClick) throws Exception {
        if (checkForChance(chanceToMissClick)) {
            missClick(driver, element);
        }
        int fails = 0;
        while (fails < 3) {
            try {
                final Actions builder = new Actions(driver);
                Dimension size = element.getSize();
                builder.moveToElement(element, getRandomNumber(size.width / 2 * -1 + 2, size.width / 2 - 2), getRandomNumber(size.height / 2 * -1 + 2, size.height / 2 - 2))
                        .pause(Duration.ofMillis(getRandomNumber(200, 500)))
                        .click().build().perform();
//                if (fails != 0) {
//                    System.out.println("clickButton 1393 successfull");
//                }
                return element;
            } catch (MoveTargetOutOfBoundsException | ElementNotInteractableException e) {
                fails++;
              //  System.out.println(e.getClass().getSimpleName() + " - going to retry clickButton 1393");
                Thread.sleep(getRandomNumber(300, 600));
            }
        }
        return null;
    }

    public static void clickElement(final ChromeDriver driver, final String xpath, final int timeout) throws Exception {
        int fails = 0;
        while (fails < 3) {
            try {
                final WebElement element = waitUntilElementWillBePresent(driver, xpath, timeout);
                final Actions builder = new Actions(driver);
                Dimension size = element.getSize();
                builder.moveToElement(element, getRandomNumber(size.width / 2 * -1 + 2, size.width / 2 - 2), getRandomNumber(size.height / 2 * -1 + 2, size.height / 2 - 2))
                        .pause(Duration.ofMillis(getRandomNumber(200, 500)))
                        .click().build().perform();
//                if (fails != 0) {
//                    System.out.println("clickButton 1393 successfull");
//                }
            } catch (MoveTargetOutOfBoundsException | ElementNotInteractableException e) {
                fails++;
              //  System.out.println(e.getClass().getSimpleName() + " - going to retry clickButton 1393");
                Thread.sleep(getRandomNumber(300, 600));
            }
        }
    }

    public static WebElement missClick(final ChromeDriver driver, final String xpath) throws Exception {
        int fails = 0;
        while (fails < 3) {
            try {
                final WebElement button = waitUntilElementWillBePresent(driver, xpath);
                final Actions builder = new Actions(driver);
                Dimension size = button.getSize();

                int missClickAmounts = getRandomNumber(0, 100) > 50 ? 2 : 1;
                for (int index = 0; index < missClickAmounts; index++) {
                    boolean missClickByHeight = getRandomNumber(0, 100) > 50;
                    if (missClickByHeight) {
                        int heightOutsidePoint = getRandomNumber(0, 100) > 50 ? getRandomNumber(size.height / 2 * -1 - 2, size.height / 2 * -1 - 12) : getRandomNumber(size.height / 2 + 2, size.height / 2 + 12);
                        builder.moveToElement(button, getRandomNumber(size.width / 2 * -1 + 2, size.width / 2 - 2), heightOutsidePoint)
                                .pause(Duration.ofMillis(getRandomNumber(200, 450)))
                                .click().build().perform();
//                int heightOutsidePoint = size.height / 2 * -1 - 12;
//                builder.moveToElement(button, getRandomNumber(size.width / 2 * -1 + 2, size.width / 2 - 2), heightOutsidePoint)
//                        .pause(Duration.ofMillis(getRandomNumber(200, 500)))
//                        .click().build().perform();
//                heightOutsidePoint = size.height / 2 + 12;
//                builder.moveToElement(button, getRandomNumber(size.width / 2 * -1 + 2, size.width / 2 - 2), heightOutsidePoint)
//                        .pause(Duration.ofMillis(getRandomNumber(200, 500)))
//                        .click().build().perform();
                    } else {
                        int widthOutsidePoint = getRandomNumber(0, 100) > 50 ? getRandomNumber(size.width / 2 * -1 - 2, size.width / 2 * -1 - 12) : getRandomNumber(size.width / 2 + 2, size.width / 2 + 12);
                        builder.moveToElement(button, widthOutsidePoint, getRandomNumber(size.height / 2 * -1 + 2, size.height / 2 - 2))
                                .pause(Duration.ofMillis(getRandomNumber(200, 450)))
                                .click().build().perform();
//                int widthOutsidePoint = size.width / 2 * -1 - 12;
//                builder.moveToElement(button, widthOutsidePoint, getRandomNumber(size.height / 2 * -1 + 2, size.height / 2 - 2))
//                        .pause(Duration.ofMillis(getRandomNumber(200, 500)))
//                        .click().build().perform();
//                widthOutsidePoint = size.width / 2 + 12;
//                builder.moveToElement(button, widthOutsidePoint, getRandomNumber(size.height / 2 * -1 + 2, size.height / 2 - 2))
//                        .pause(Duration.ofMillis(getRandomNumber(200, 500)))
//                        .click().build().perform();
                    }
                }
//                if (fails != 0) {
//                    System.out.println("missClick successfull");
//                }
                return button;
            } catch (MoveTargetOutOfBoundsException | ElementNotInteractableException e) {
                fails++;
                //System.out.println(e.getClass().getSimpleName() + " - going to retry missClick");
                Thread.sleep(getRandomNumber(300, 600));
            }
        }
        return null;
    }

    public static WebElement missClick(final ChromeDriver driver, final WebElement button) throws Exception {
        int fails = 0;
        while (fails < 3) {
            try {
                final Actions builder = new Actions(driver);
                Dimension size = button.getSize();
                int missClickAmounts = getRandomNumber(0, 100) > 50 ? 2 : 1;
                for (int index = 0; index < missClickAmounts; index++) {
                    boolean missClickByHeight = getRandomNumber(0, 100) > 50;
                    if (missClickByHeight) {
                        int heightOutsidePoint = getRandomNumber(0, 100) > 50 ? getRandomNumber(size.height / 2 * -1 - 2, size.height / 2 * -1 - 12) : getRandomNumber(size.height / 2 + 2, size.height / 2 + 12);
                        builder.moveToElement(button, getRandomNumber(size.width / 2 * -1 + 2, size.width / 2 - 2), heightOutsidePoint)
                                .pause(Duration.ofMillis(getRandomNumber(200, 450)))
                                .click().build().perform();
//                int heightOutsidePoint = size.height / 2 * -1 - 12;
//                builder.moveToElement(button, getRandomNumber(size.width / 2 * -1 + 2, size.width / 2 - 2), heightOutsidePoint)
//                        .pause(Duration.ofMillis(getRandomNumber(200, 500)))
//                        .click().build().perform();
//                heightOutsidePoint = size.height / 2 + 12;
//                builder.moveToElement(button, getRandomNumber(size.width / 2 * -1 + 2, size.width / 2 - 2), heightOutsidePoint)
//                        .pause(Duration.ofMillis(getRandomNumber(200, 500)))
//                        .click().build().perform();
                    } else {
                        int widthOutsidePoint = getRandomNumber(0, 100) > 50 ? getRandomNumber(size.width / 2 * -1 - 2, size.width / 2 * -1 - 12) : getRandomNumber(size.width / 2 + 2, size.width / 2 + 12);
                        builder.moveToElement(button, widthOutsidePoint, getRandomNumber(size.height / 2 * -1 + 2, size.height / 2 - 2))
                                .pause(Duration.ofMillis(getRandomNumber(200, 450)))
                                .click().build().perform();
//                int widthOutsidePoint = size.width / 2 * -1 - 12;
//                builder.moveToElement(button, widthOutsidePoint, getRandomNumber(size.height / 2 * -1 + 2, size.height / 2 - 2))
//                        .pause(Duration.ofMillis(getRandomNumber(200, 500)))
//                        .click().build().perform();
//                widthOutsidePoint = size.width / 2 + 12;
//                builder.moveToElement(button, widthOutsidePoint, getRandomNumber(size.height / 2 * -1 + 2, size.height / 2 - 2))
//                        .pause(Duration.ofMillis(getRandomNumber(200, 500)))
//                        .click().build().perform();
                    }
                }
//                if (fails != 0) {
//                    System.out.println("missClick successfull");
//                }
                return button;
            } catch (MoveTargetOutOfBoundsException | ElementNotInteractableException e) {
                fails++;
              //  System.out.println(e.getClass().getSimpleName() + " - going to retry missClick");
                Thread.sleep(getRandomNumber(300, 600));
            }
        }
        return null;
    }

    public static void closeAllTabsExceptFirstOne(final ChromeDriver driver) throws Exception {
        final ArrayList<String> windows = new ArrayList<>(driver.getWindowHandles());
        for (int index = 1; index < windows.size(); index++) {
            driver.switchTo().window(windows.get(index));
            driver.close();
            Thread.sleep(getRandomNumber(400, 700));
        }
        driver.switchTo().window(windows.get(0));

    }

    public static void waitUntilNumbersOfWindows(final ChromeDriver driver, int timeout, int windows) {
        final Date now = new Date();
        try {
            final WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(getRandomNumber(10, 20)));
            wait.until(ExpectedConditions.numberOfWindowsToBe(windows));
        } catch (Exception e) {
            System.out.println("Start " + now + " end " + new Date());
            throw e;
        }
    }

    public static WebElement findElement(final ChromeDriver driver, final String xpath) {
        return driver.findElement(By.xpath("//" + xpath));
    }

    public static WebElement waitUntilElementWhichContainsTextWillBePresent(final ChromeDriver driver, final String tag, final String text) {
        final By searchCriteria = By.xpath("//" + tag + "[contains(text(), '" + text + "')]");
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions.presenceOfElementLocated(searchCriteria));
        return driver.findElement(searchCriteria);

    }

    public static WebElement waitUntilElementWhichContainsTextWillBePresent(final ChromeDriver driver, final String tag, final String text, final int timeout) {
        final By searchCriteria = By.xpath("//" + tag + "[contains(text(), '" + text + "')]");
        new WebDriverWait(driver, Duration.ofSeconds(timeout)).until(ExpectedConditions.presenceOfElementLocated(searchCriteria));
        return driver.findElement(searchCriteria);

    }

    private static void dragElement(final ChromeDriver driver, final String fromElementPath, final String toElementPath) throws Exception {
        WebElement fromElement = waitUntilElementWillBePresent(driver, fromElementPath);
        WebElement toElement = waitUntilElementWillBePresent(driver, toElementPath);
        Actions builder = new Actions(driver);
        Action dragAndDrop = builder.clickAndHold(fromElement)
                .moveToElement(toElement)
                .release(toElement)
                .build();
        dragAndDrop.perform();
    }

    public static void clickAndFillField(final ChromeDriver driver,
                                         final String searchTerm,
                                         final String text
    ) throws Exception {
        final WebElement field = clickElement(driver, searchTerm);
        Thread.sleep(getRandomNumber(500, 1000));
        field.sendKeys(text);
    }

    public static void updatePersonalInfoForVkAcc(final ChromeDriver driver,
                                                  final VkAccount vkAccount) throws Exception {

        driver.get("https://id.vk.com/account/#/personal");
        vkAccount.setFirstName(waitUntilElementWillBePresent(driver, "input[@name='first_name']").getAttribute("value"));
        vkAccount.setLastName(findElement(driver, "input[@name='last_name']").getAttribute("value"));
        final Integer day = Integer.valueOf(findElement(driver, "div[@class='vkuiDatePicker__day']/label/span").getAttribute("value"));
        final Integer month = Integer.valueOf(findElement(driver, "div[@class='vkuiDatePicker__month']/label/span").getAttribute("value"));
        final Integer year = Integer.valueOf(findElement(driver, "div[@class='vkuiDatePicker__year']/label/span").getAttribute("value"));
        final String sex = findElement(driver, "span[@data-test-id='sex-dropdown']").getAttribute("value").equals("1") ? "Женский" : "Мужской";
        vkAccount.setDayOfBirth(day);
        vkAccount.setMonthOfBirth(month);
        vkAccount.setYearOfBirth(year);
        vkAccount.setSex(sex);
        DbConnection.getInstance().updateProfileInfoForVkAcc(vkAccount);
    }

    public static void switchToLastWindow(ChromeDriver driver) {
        ArrayList<String> windows = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(windows.get(windows.size() - 1));
    }

    public static String processMailByTitle(final ChromeDriver driver, final String mailTitle, final Function<ChromeDriver, String> resultFn) throws Exception {
        driver.switchTo().newWindow(WindowType.TAB);
        String result = null;
        try {
            try {
                driver.get("https://e.mail.ru/inbox/?filter_unread=1");
                clickElement(driver, waitUntilElementWithTextWillBePresent(driver, "span", mailTitle));
            } catch (Exception e) {
                driver.get("https://e.mail.ru/spam/?filter_unread=1");
                clickElement(driver, waitUntilElementWithTextWillBePresent(driver, "span", mailTitle));
            }
            Thread.sleep(1500);
            result = resultFn.apply(driver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        driver.close();
        Thread.sleep(getRandomNumber(500, 1000));
        ArrayList<String> windows = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(windows.get(windows.size() - 1));
        return result;
    }
    public static boolean fiftyFifty() {
        return getRandomNumber(0, 100) > 50;
    }
    public static boolean checkForChance(int chance) {
        return getRandomNumber(0, 100) < chance;
    }

    public static void pressEnter(final Robot robot) {
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
    }

    public static void pressDelete(final Robot robot) {
        robot.keyPress(KeyEvent.VK_DELETE);
        robot.keyRelease(KeyEvent.VK_DELETE);
    }

    public static String copyTextFromScreen(int x1, int y1, int x2, int y2, final Robot robot) {
        String copiedText = "";
        try {

            // Move the cursor to the starting coordinate
            robot.mouseMove(x1, y1);

            // Press the left mouse button (without releasing it)
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);

            // Move the cursor to the ending coordinate while the mouse button is pressed
            robot.mouseMove(x2, y2);

            // Release the mouse button
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

            // Command to copy the selected text (Ctrl+C)
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_C);
            robot.keyRelease(KeyEvent.VK_C);
            robot.keyRelease(KeyEvent.VK_CONTROL);

            // Sleep to give system time to recognize the copy to clipboard command
            Thread.sleep(1000);

            // Get the system clipboard
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

            // Get the contents of the clipboard
            copiedText = (String) clipboard.getData(DataFlavor.stringFlavor);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return copiedText;
    }

    public static void clickOn(final int x, final int y, final Robot robot) throws Exception {
        robot.mouseMove(x, y);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(150);
    }

    public static void pressKey(final int key, final Robot robot) throws Exception {
        robot.keyPress(key);
        robot.keyRelease(key);
        Thread.sleep(300);
    }

    public static void openNewTab(Robot robot) throws Exception {
        robot.keyPress(KeyEvent.VK_CONTROL);
        Thread.sleep(300);
        robot.keyPress(KeyEvent.VK_T);
        Thread.sleep(300);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        Thread.sleep(300);
        robot.keyRelease(KeyEvent.VK_T);
        Thread.sleep(1000);
    }

    private static final Map<Character, Character> rusToEngKeyboardMap;

    static {
        rusToEngKeyboardMap = new HashMap<>();
        rusToEngKeyboardMap.put('й', 'q');
        rusToEngKeyboardMap.put('ц', 'w');
        rusToEngKeyboardMap.put('у', 'e');
        rusToEngKeyboardMap.put('к', 'r');
        rusToEngKeyboardMap.put('е', 't');
        rusToEngKeyboardMap.put('н', 'y');
        rusToEngKeyboardMap.put('г', 'u');
        rusToEngKeyboardMap.put('ш', 'i');
        rusToEngKeyboardMap.put('щ', 'o');
        rusToEngKeyboardMap.put('з', 'p');
        rusToEngKeyboardMap.put('х', '[');
        rusToEngKeyboardMap.put('ъ', ']');
        rusToEngKeyboardMap.put('ф', 'a');
        rusToEngKeyboardMap.put('ы', 's');
        rusToEngKeyboardMap.put('в', 'd');
        rusToEngKeyboardMap.put('а', 'f');
        rusToEngKeyboardMap.put('п', 'g');
        rusToEngKeyboardMap.put('р', 'h');
        rusToEngKeyboardMap.put('о', 'j');
        rusToEngKeyboardMap.put('л', 'k');
        rusToEngKeyboardMap.put('д', 'l');
        rusToEngKeyboardMap.put('ж', ';');
        rusToEngKeyboardMap.put('э', '\'');
        rusToEngKeyboardMap.put('я', 'z');
        rusToEngKeyboardMap.put('ч', 'x');
        rusToEngKeyboardMap.put('с', 'c');
        rusToEngKeyboardMap.put('м', 'v');
        rusToEngKeyboardMap.put('и', 'b');
        rusToEngKeyboardMap.put('т', 'n');
        rusToEngKeyboardMap.put('ь', 'm');
        rusToEngKeyboardMap.put('б', ',');
        rusToEngKeyboardMap.put('ю', '.');
    }

    public static void switchLanguage(final Robot robot) {
        robot.keyPress(KeyEvent.VK_ALT);
        robot.keyPress(KeyEvent.VK_SHIFT);
        robot.keyRelease(KeyEvent.VK_SHIFT);
        robot.keyRelease(KeyEvent.VK_ALT);
    }

    public static void typeRussianString(String string, final Robot robot) throws Exception {

        string = string.replaceAll("ё", "е");
        switchLanguage(robot);
        Thread.sleep(300);
        //Looping through every char
        for (int i = 0; i < string.length(); i++) {
            //Getting current char
            char c = string.charAt(i);
            if (StringUtils.isEmpty(String.valueOf(c))) {
                return;
            }
            final boolean isUpperCase = Character.isUpperCase(c);
            if (rusToEngKeyboardMap.get(c) == null) {
                c = String.valueOf(rusToEngKeyboardMap.get(String.valueOf(c).toLowerCase(Locale.ROOT).charAt(0))).toUpperCase().charAt(0);
            } else {
                c = rusToEngKeyboardMap.get(c);
            }

            try {

                //Pressing shift if it's uppercase
                if (isUpperCase) {
                    robot.keyPress(KeyEvent.VK_SHIFT);
                }
                //Actually pressing the key
                robot.keyPress(Character.toUpperCase(c));
                robot.keyRelease(Character.toUpperCase(c));
                //Releasing shift if it's uppercase
                if (isUpperCase) {
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                }


            } catch (Exception e) {

            }
            //Optional delay to make it look like it's a human typing
            Thread.sleep(50);
        }
        switchLanguage(robot);
    }

    public static void typeString(String string, final Robot robot) throws Exception {

        //Looping through every char
        for (int i = 0; i < string.length(); i++) {
            //Getting current char
            char c = string.charAt(i);
            if (StringUtils.isEmpty(String.valueOf(c))) {
                return;
            }
            try {

                if (c == '@') {
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    Thread.sleep(150);
                    robot.keyPress(KeyEvent.VK_2);
                    Thread.sleep(150);
                    robot.keyRelease(KeyEvent.VK_2);
                    Thread.sleep(150);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                } else if (c == ':') {
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    Thread.sleep(150);
                    robot.keyPress(KeyEvent.VK_SEMICOLON);
                    Thread.sleep(150);
                    robot.keyRelease(KeyEvent.VK_SEMICOLON);
                    Thread.sleep(150);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                } else if (c == '?') {
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    Thread.sleep(150);
                    robot.keyPress(KeyEvent.VK_SLASH);
                    Thread.sleep(150);
                    robot.keyRelease(KeyEvent.VK_SLASH);
                    Thread.sleep(150);
                    robot.keyRelease(KeyEvent.VK_SHIFT);
                } else if (c == '.') {
                    robot.keyPress(KeyEvent.VK_PERIOD);
                    Thread.sleep(150);
                    robot.keyRelease(KeyEvent.VK_PERIOD);
                } else {
                    //Pressing shift if it's uppercase
                    if (Character.isUpperCase(c)) {
                        robot.keyPress(KeyEvent.VK_SHIFT);
                    }
                    //Actually pressing the key
                    robot.keyPress(Character.toUpperCase(c));
                    robot.keyRelease(Character.toUpperCase(c));
                    //Releasing shift if it's uppercase
                    if (Character.isUpperCase(c)) {
                        robot.keyRelease(KeyEvent.VK_SHIFT);
                    }
                }


            } catch (Exception e) {

            }
            //Optional delay to make it look like it's a human typing
            Thread.sleep(50);
        }
    }

}
