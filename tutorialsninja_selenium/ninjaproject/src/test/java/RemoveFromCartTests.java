import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;
import java.time.Duration;
import java.util.List;

/**
 * Module    : Remove from Cart
 * Website   : http://tutorialsninja.com/demo
 * Test IDs  : TC_REM_001 to TC_REM_005
 * Techniques: Functional Testing, UI Testing, End-to-End Testing
 *
 * Verified OpenCart cart locators:
 *   Cart page : /index.php?route=checkout/cart
 *   Cart rows : table.table tbody tr
 *   Remove btn: button[data-original-title='Remove']  OR  .btn.btn-danger
 *   Empty msg : #content p  (contains "Your shopping cart is empty!")
 *
 * !! UPDATE VALID_EMAIL / VALID_PASS !!
 */
public class RemoveFromCartTests {

    private WebDriver driver;
    private WebDriverWait wait;
    private final String BASE_URL   = "https://tutorialsninja.com/demo";
    private final String CART_URL   = BASE_URL + "/index.php?route=checkout/cart";
    private final String MACBOOK_URL= BASE_URL + "/index.php?route=product/product&product_id=43";
    private final String IPHONE_URL = BASE_URL + "/index.php?route=product/product&product_id=40";

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
        driver.get(BASE_URL + "/index.php?route=account/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("input-email")));
        driver.findElement(By.id("input-email")).sendKeys(VALID_EMAIL);
        driver.findElement(By.id("input-password")).sendKeys(VALID_PASS);
        driver.findElement(By.cssSelector("input[value='Login']")).click();
        wait.until(ExpectedConditions.urlContains("account/account"));
    }

    private void addProduct(String url) {
        driver.get(url);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("button-cart"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.alert-success")));
    }

    private List<WebElement> getCartRows() {
        driver.get(CART_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("content")));
        sleep(800);
        return driver.findElements(By.cssSelector("table.table tbody tr"));
    }

    private void removeFirstItem() {
        List<WebElement> removeBtns = driver.findElements(
                By.cssSelector("button[data-original-title='Remove']"));
        if (removeBtns.isEmpty()) {
            removeBtns = driver.findElements(By.cssSelector(".btn.btn-danger"));
        }
        Assert.assertFalse(removeBtns.isEmpty(), "Remove button not found in cart.");
        removeBtns.get(0).click();
        sleep(1500);
    }

    private boolean isCartEmpty() {
        sleep(800);
        String src = driver.getPageSource();
        return src.contains("Your shopping cart is empty")
                || driver.findElements(By.cssSelector("table.table tbody tr")).isEmpty();
    }

    // ── TC_REM_001 ────────────────────────────────────────────────────
    @Test(priority = 1, description = "TC_REM_001 - Functional: Remove one product from multi-item cart")
    public void TC_REM_001_removeOneFromMultiCart() {
        login();
        addProduct(MACBOOK_URL);
        addProduct(IPHONE_URL);

        List<WebElement> before = getCartRows();
        Assert.assertTrue(before.size() >= 2,
                "Setup: Need 2 items before removal. Got: " + before.size());

        removeFirstItem();

        List<WebElement> after = driver.findElements(By.cssSelector("table.table tbody tr"));
        Assert.assertTrue(after.size() < before.size(),
                "TC_REM_001 FAILED – Item count should decrease. Before: "
                + before.size() + " After: " + after.size());
    }

    // ── TC_REM_002 ────────────────────────────────────────────────────
    @Test(priority = 2, description = "TC_REM_002 - Functional: Remove last item shows empty cart message")
    public void TC_REM_002_removeLastItemShowsEmpty() {
        login();
        addProduct(MACBOOK_URL);

        List<WebElement> rows = getCartRows();
        Assert.assertFalse(rows.isEmpty(), "Cart should have 1 item before removal.");

        removeFirstItem();

        Assert.assertTrue(isCartEmpty(),
                "TC_REM_002 FAILED – Cart should show empty message after removing last item.");
    }

    // ── TC_REM_003 ────────────────────────────────────────────────────
    @Test(priority = 3, description = "TC_REM_003 - UI: Remove button is visible and clickable in cart")
    public void TC_REM_003_removeButtonVisible() {
        login();
        addProduct(MACBOOK_URL);

        getCartRows(); // navigates to cart
        List<WebElement> removeBtns = driver.findElements(
                By.cssSelector("button[data-original-title='Remove']"));
        if (removeBtns.isEmpty()) {
            removeBtns = driver.findElements(By.cssSelector(".btn.btn-danger"));
        }
        Assert.assertFalse(removeBtns.isEmpty(), "TC_REM_003 FAILED – Remove button not found.");
        Assert.assertTrue(removeBtns.get(0).isDisplayed(), "TC_REM_003 FAILED – Remove button not visible.");
        Assert.assertTrue(removeBtns.get(0).isEnabled(),   "TC_REM_003 FAILED – Remove button not enabled.");
    }

    // ── TC_REM_004 ────────────────────────────────────────────────────
    @Test(priority = 4, description = "TC_REM_004 - E2E: Login -> Add Product -> Remove -> Cart Empty")
    public void TC_REM_004_e2eAddAndRemove() {
        login();
        addProduct(MACBOOK_URL);

        List<WebElement> rows = getCartRows();
        Assert.assertFalse(rows.isEmpty(), "E2E: Cart should have product.");

        removeFirstItem();

        Assert.assertTrue(isCartEmpty(),
                "TC_REM_004 FAILED – Cart should be empty after E2E remove.");
    }

    // ── TC_REM_005 ────────────────────────────────────────────────────
    @Test(priority = 5, description = "TC_REM_005 - Functional: Guest user removes product from cart")
    public void TC_REM_005_guestRemovesProduct() {
        // No login – guest session
        addProduct(IPHONE_URL);

        List<WebElement> rows = getCartRows();
        Assert.assertFalse(rows.isEmpty(), "Guest cart should have product.");

        removeFirstItem();

        Assert.assertTrue(isCartEmpty(),
                "TC_REM_005 FAILED – Guest cart should be empty after removal.");
    }

    private void sleep(long ms) { try { Thread.sleep(ms); } catch (InterruptedException ignored) {} }
}
