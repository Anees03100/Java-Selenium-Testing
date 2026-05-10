import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;
import java.time.Duration;

/**
 * Module    : Login
 * Website   : http://tutorialsninja.com/demo
 * Test IDs  : TC_LGN_001 to TC_LGN_007
 * Techniques: Functional, Smoke, Cross-Browser, UI Testing
 *
 * OpenCart Login locators – verified from live site:
 *   id="input-email"     id="input-password"
 *   input[value='Login'] div.alert-danger
 *
 * !! UPDATE BEFORE RUNNING !!
 *   Register an account on tutorialsninja.com/demo first, then update:
 *   VALID_EMAIL / VALID_PASS
 */
public class LoginTests {

    private WebDriver driver;
    private WebDriverWait wait;
    private final String BASE_URL  = "https://tutorialsninja.com/demo";
    private final String LOGIN_URL = BASE_URL + "/index.php?route=account/login";

    // !! UPDATE THESE with your registered account !!
    private static final String VALID_EMAIL = "your_email@test.com";
    private static final String VALID_PASS  = "Test@1234";

    @BeforeMethod(groups = {"functional", "smoke", "ui", "e2e"})
    public void setup() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.get(LOGIN_URL);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driver != null){
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            driver.quit();
        }
    }

    private void doLogin(String email, String pass) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("input-email")));
        driver.findElement(By.id("input-email")).clear();
        driver.findElement(By.id("input-email")).sendKeys(email);
        driver.findElement(By.id("input-password")).clear();
        driver.findElement(By.id("input-password")).sendKeys(pass);
        driver.findElement(By.cssSelector("input[value='Login']")).click();
    }

    // ── TC_LGN_001 ────────────────────────────────────────────────────
    @Test(priority = 1, description = "TC_LGN_001 - Successful login with valid credentials")
    public void TC_LGN_001_validLogin() {
        doLogin(VALID_EMAIL, VALID_PASS);
        wait.until(ExpectedConditions.urlContains("account/account"));
        Assert.assertTrue(driver.getCurrentUrl().contains("account/account"),
                "TC_LGN_001 FAILED – URL: " + driver.getCurrentUrl());
    }

    // ── TC_LGN_002 ────────────────────────────────────────────────────
    @Test(priority = 2, description = "TC_LGN_002 - Login with incorrect password")
    public void TC_LGN_002_wrongPassword() {
        doLogin(VALID_EMAIL, "WrongPassword999");
        String warn = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("div.alert-danger"))).getText();
        Assert.assertTrue(warn.contains("Warning") || warn.contains("match"),
                "TC_LGN_002 FAILED – Expected login warning.");
    }

    // ── TC_LGN_003 ────────────────────────────────────────────────────
    @Test(priority = 3, description = "TC_LGN_003 - Login with unregistered email")
    public void TC_LGN_003_unregisteredEmail() {
        doLogin("nobody_" + System.currentTimeMillis() + "@fake.com", "Test@1234");
        String warn = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("div.alert-danger"))).getText();
        Assert.assertFalse(warn.isEmpty(), "TC_LGN_003 FAILED – Expected error message.");
    }

    // ── TC_LGN_004 ────────────────────────────────────────────────────
    @Test(priority = 4, description = "TC_LGN_004 - Smoke Test: Login page loads with all elements")
    public void TC_LGN_004_smokeTest() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("input-email")));
        Assert.assertTrue(driver.findElement(By.id("input-email")).isDisplayed(),              "Email field missing.");
        Assert.assertTrue(driver.findElement(By.id("input-password")).isDisplayed(),           "Password field missing.");
        Assert.assertTrue(driver.findElement(By.cssSelector("input[value='Login']")).isDisplayed(), "Login button missing.");
        Assert.assertTrue(driver.findElement(By.linkText("Forgotten Password")).isDisplayed(), "Forgot Password link missing.");
    }

    // ── TC_LGN_005 ────────────────────────────────────────────────────
    @Test(priority = 5, description = "TC_LGN_005 - Cross-Browser: Login on Microsoft Edge")
    public void TC_LGN_005_crossBrowserEdge() {
        // close chrome
        if (driver != null) {
            driver.quit();
        }

        System.setProperty("webdriver.edge.driver", "C:\\Users\\hp\\Downloads\\edgedriver_win64\\msedgedriver.exe");

        // set edge
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--remote-allow-origins=*");

        try {
            driver = new EdgeDriver(options);
            driver.manage().window().maximize();
            wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            driver.get(LOGIN_URL);
            doLogin(VALID_EMAIL, VALID_PASS);

            wait.until(ExpectedConditions.urlContains("account/account"));
            Assert.assertTrue(driver.getCurrentUrl().contains("account/account"),
                    "TC_LGN_005 FAILED – Cross-browser login failed on Edge.");
        } catch (Exception e) {
            System.out.println("Edge Manual Start Failed: " + e.getMessage());
            Assert.fail("Edge failed to open in LoginTests. Check driver path.");
        }
    }

    // ── TC_LGN_006 ────────────────────────────────────────────────────
    @Test(priority = 6, description = "TC_LGN_006 - UI: Password field masks characters")
    public void TC_LGN_006_passwordMasked() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("input-password")));
        String type = driver.findElement(By.id("input-password")).getAttribute("type");
        Assert.assertEquals(type, "password", "TC_LGN_006 FAILED – Password field not masked.");
    }

    // ── TC_LGN_007 ────────────────────────────────────────────────────
    @Test(priority = 7, description = "TC_LGN_007 - Login with both fields empty")
    public void TC_LGN_007_emptyFields() {
        driver.findElement(By.cssSelector("input[value='Login']")).click();
        String warn = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("div.alert-danger"))).getText();
        Assert.assertFalse(warn.isEmpty(), "TC_LGN_007 FAILED – Expected validation error.");
    }

    @Test
    public void verifyLoginWithSpaces() {

        driver.get("https://tutorialsninja.com/demo/index.php?route=account/login");

        String email = "      your_email@test.com    ".trim();
        String password = "   Test@1234    ".trim();
        driver.findElement(By.id("input-email")).sendKeys(email);
        driver.findElement(By.id("input-password")).sendKeys(password);
        driver.findElement(By.xpath("//input[@value='Login']")).click();
        Assert.assertTrue(driver.getCurrentUrl().contains("account/account"));
        System.out.print("Logged in successfull");
    }
}
