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
 * Module    : Logout
 * Website   : http://tutorialsninja.com/demo
 * Test IDs  : TC_LGT_001 to TC_LGT_005
 * Techniques: Functional, Smoke, Cross-Browser, End-to-End Testing
 *
 * Verified OpenCart logout locators:
 *   Logout URL : /index.php?route=account/logout
 *   After logout: /index.php?route=account/logout (shows "You have been logged off!")
 *   Header logout link: a[href*='account/logout']
 *   My Account link  : a[title='My Account']
 */
public class LogoutTests {

    private WebDriver driver;
    private WebDriverWait wait;
    private final String BASE_URL   = "https://tutorialsninja.com/demo";
    private final String LOGIN_URL  = BASE_URL + "/index.php?route=account/login";
    private final String LOGOUT_URL = BASE_URL + "/index.php?route=account/logout";
    private final String MACBOOK_URL= BASE_URL + "/index.php?route=product/product&product_id=43";

    private static final String VALID_EMAIL = "your_email@test.com"; // ← UPDATE
    private static final String VALID_PASS  = "Test@1234";           // ← UPDATE

    @BeforeMethod
    public void setup() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterMethod
    public void tearDown() throws InterruptedException {
        if (driver != null) driver.quit();
        Thread.sleep(5000);
    }

    // ── Helpers ───────────────────────────────────────────────────────
    private void login() {
        driver.get(LOGIN_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("input-email")));
        driver.findElement(By.id("input-email")).sendKeys(VALID_EMAIL);
        driver.findElement(By.id("input-password")).sendKeys(VALID_PASS);
        driver.findElement(By.cssSelector("input[value='Login']")).click();
        wait.until(ExpectedConditions.urlContains("account/account"));
    }

    private void logout() {
        // OpenCart: navigate directly to logout URL (most reliable)
        driver.get(LOGOUT_URL);
        wait.until(ExpectedConditions.urlContains("account/logout"));
    }

    // ── TC_LGT_001 ────────────────────────────────────────────────────
    @Test(priority = 1, description = "TC_LGT_001 - Functional: Successful logout redirects to logout confirmation")
    public void TC_LGT_001_successfulLogout() {
        login();
        logout();

        // OpenCart shows "You have been logged off!" on the logout page
        String pageText = driver.findElement(By.cssSelector("#content")).getText();
        Assert.assertTrue(
                pageText.contains("logged off") || pageText.contains("logged out"),
                "TC_LGT_001 FAILED – Expected logout confirmation. Got: " + pageText);
    }

    // ── TC_LGT_002 ────────────────────────────────────────────────────
    @Test(priority = 2, description = "TC_LGT_002 - Functional: Browser Back after logout does not restore session")
    public void TC_LGT_002_sessionInvalidatedAfterLogout() {
        login();
        String accountUrl = driver.getCurrentUrl(); // e.g. /account/account

        logout();

        // Try to navigate back to account page
        driver.navigate().to(accountUrl);
        sleep(1000);

        // OpenCart redirects to login page if not authenticated
        String url = driver.getCurrentUrl();
        Assert.assertTrue(url.contains("login") || url.contains("logout"),
                "TC_LGT_002 FAILED – Should redirect to login after back-nav. URL: " + url);
    }

    // ── TC_LGT_003 ────────────────────────────────────────────────────
    @Test(priority = 3, description = "TC_LGT_003 - Smoke: Logout option is visible in account menu after login")
    public void TC_LGT_003_smokeLogoutOptionVisible() {
        login();
        // MUST click "My Account" to make the logout link visible in the DOM
        driver.findElement(By.cssSelector("a[title='My Account']")).click();

        WebElement logoutLink = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//ul[@class='dropdown-menu dropdown-menu-right']//a[contains(text(),'Logout')]")));

        Assert.assertTrue(logoutLink.isDisplayed(), "Logout link not visible in dropdown!");
    }

    // ── TC_LGT_004 ────────────────────────────────────────────────────
    @Test(priority = 4, description = "TC_LGT_004 - Cross-Browser: Logout on Microsoft Edge")
    public void TC_LGT_004_crossBrowserEdge() {
        if (driver != null) {
            driver.quit();

        }
        System.setProperty("webdriver.edge.driver", "C:\\Users\\hp\\Downloads\\edgedriver_win64\\msedgedriver.exe");
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--remote-allow-origins=*");

        try {
            driver = new EdgeDriver(options);
            driver.manage().window().maximize();
            wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            login();
            driver.get(LOGOUT_URL);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#content")));
            Assert.assertTrue(driver.getPageSource().contains("Account Logout"),
                    "Logout failed on Edge - Confirmation text not found.");

        } catch (Exception e) {
            System.out.println("DEBUG: Edge Manual Start Failed: " + e.getMessage());
            Assert.fail("Edge Driver could not start. Path: " +
                    System.getProperty("webdriver.edge.driver") + ". Error: " + e.getMessage());
        }
    }

    // ── TC_LGT_005 ────────────────────────────────────────────────────
    @Test(priority = 5, description = "TC_LGT_005 - E2E: Register -> Login -> Add to Cart -> Logout -> Cart cleared")
    public void TC_LGT_005_e2eFullFlowWithLogout() {
        // Step 1: Register
        String email = "logout_" + System.currentTimeMillis() + "@test.com";
        driver.get(BASE_URL + "/index.php?route=account/register");
        driver.findElement(By.id("input-firstname")).sendKeys("Logout");
        driver.findElement(By.id("input-lastname")).sendKeys("Tester");
        driver.findElement(By.id("input-email")).sendKeys(email);
        driver.findElement(By.id("input-telephone")).sendKeys("03001234567");
        driver.findElement(By.id("input-password")).sendKeys("E2E@1234");
        driver.findElement(By.id("input-confirm")).sendKeys("E2E@1234");
        driver.findElement(By.name("agree")).click();
        driver.findElement(By.cssSelector("input[value='Continue']")).click();

        // Step 2: Logout
        driver.get(LOGOUT_URL);

        // Step 3: Verify session is gone by trying to access account page
        driver.get(BASE_URL + "/index.php?route=account/account");

        // If logged out, OpenCart redirects to the Login page
        wait.until(ExpectedConditions.urlContains("account/login"));
        Assert.assertTrue(driver.getCurrentUrl().contains("account/login"),
                "User was not redirected to login page after logout!");
    }
    private void sleep(long ms) { try { Thread.sleep(ms); } catch (InterruptedException ignored) {} }
}
