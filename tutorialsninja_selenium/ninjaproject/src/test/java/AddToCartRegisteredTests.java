import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;
import java.time.Duration;
import java.util.List;

/**
 * Module    : Add to Cart – Registered User
 * Website   : http://tutorialsninja.com/demo
 * Test IDs  : TC_CART_R_001 to TC_CART_R_005
 * Techniques: Functional Testing, End-to-End Testing, UI Testing
 *
 * Verified product IDs from live site:
 *   MacBook  = product_id=43
 *   iPhone   = product_id=40
 *   Cinema   = product_id=42
 *
 * OpenCart Add to Cart button: id="button-cart"
 * Success alert: div.alert-success
 * Cart count: #cart-total (shows "X item(s) - $X.XX")
 *
 * !! UPDATE VALID_EMAIL / VALID_PASS !!
 */
public class AddToCartRegisteredTests {

    private WebDriver driver;
    private WebDriverWait wait;
    private final String BASE_URL = "https://tutorialsninja.com/demo";

    private static final String VALID_EMAIL  = "your_email@test.com"; // ← UPDATE
    private static final String VALID_PASS   = "Test@1234";           // ← UPDATE

    // Verified product URLs from live site
    private static final String MACBOOK_URL = "https://tutorialsninja.com/demo/index.php?route=product/product&product_id=43";
    private static final String IPHONE_URL  = "https://tutorialsninja.com/demo/index.php?route=product/product&product_id=40";

    @BeforeMethod
    public void setup() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) driver.quit();
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

    private void addProductToCart(String productUrl) {
        driver.get(productUrl);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("button-cart")));
        driver.findElement(By.id("button-cart")).click();
        // Wait for success alert to confirm product was added
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.alert-success")));
    }

    private int getCartCount() {
        try {
            // Cart button text: "X item(s) - $X.XX"
            String cartText = driver.findElement(By.id("cart-total")).getText().trim();
            return Integer.parseInt(cartText.split(" ")[0]);
        } catch (Exception e) {
            return 0;
        }
    }

    // ── TC_CART_R_001 ─────────────────────────────────────────────────
    @Test(priority = 1, description = "TC_CART_R_001 - Registered user adds single product to cart")
    public void TC_CART_R_001_addSingleProduct() {
        login();
        int before = getCartCount();
        addProductToCart(MACBOOK_URL);
        String alert = driver.findElement(By.cssSelector("div.alert-success")).getText();
        Assert.assertTrue(alert.contains("Success"),
                "TC_CART_R_001 FAILED – No success alert shown.");
        int after = getCartCount();
        Assert.assertTrue(after > before,
                "TC_CART_R_001 FAILED – Cart count did not increase. Before: " + before + " After: " + after);
    }

    // ── TC_CART_R_002 ─────────────────────────────────────────────────
    @Test(priority = 2, description = "TC_CART_R_002 - Registered user adds multiple products to cart")
    public void TC_CART_R_002_addMultipleProducts() {
        login();
        addProductToCart(MACBOOK_URL);
        addProductToCart(IPHONE_URL);

        driver.get(BASE_URL + "/index.php?route=checkout/cart");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table.table")));

        List<WebElement> rows = driver.findElements(By.cssSelector("tbody tr.cart-item, tbody tr"));
        Assert.assertTrue(rows.size() >= 2,
                "TC_CART_R_002 FAILED – Expected 2+ items in cart. Got: " + rows.size());
    }

    // ── TC_CART_R_003 ─────────────────────────────────────────────────
    @Test(priority = 3, description = "TC_CART_R_003 - E2E Test: Register -> Login -> Add to Cart -> View Cart")
    public void TC_CART_R_003_e2eRegisterLoginAddToCart() {
        String email = "e2e_" + System.currentTimeMillis() + "@test.com";
        driver.get(BASE_URL + "/index.php?route=account/register");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("input-firstname")));
        driver.findElement(By.id("input-firstname")).sendKeys("E2E");
        driver.findElement(By.id("input-lastname")).sendKeys("Tester");
        driver.findElement(By.id("input-email")).sendKeys(email);
        driver.findElement(By.id("input-telephone")).sendKeys("03001234567");
        driver.findElement(By.id("input-password")).sendKeys("Pass@5678");
        driver.findElement(By.id("input-confirm")).sendKeys("Pass@5678");
        WebElement agree = driver.findElement(By.name("agree"));
        if (!agree.isSelected()) agree.click();
        driver.findElement(By.cssSelector("input[value='Continue']")).click();
        wait.until(ExpectedConditions.urlContains("account/success"));
        Assert.assertTrue(driver.getCurrentUrl().contains("account/success"), "E2E: Registration failed.");
        addProductToCart(MACBOOK_URL);
        driver.get(BASE_URL + "/index.php?route=checkout/cart");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table.table")));
        boolean hasItems = !driver.findElements(By.cssSelector("table.table tbody tr")).isEmpty();
        Assert.assertTrue(hasItems, "TC_CART_R_003 FAILED – Cart should have product.");
    }

    // ── TC_CART_R_004 ─────────────────────────────────────────────────
    @Test(priority = 4, description = "TC_CART_R_004 - UI: Cart icon count updates after adding product")
    public void TC_CART_R_004_cartIconUpdates() {
        login();
        int before = getCartCount();
        addProductToCart(MACBOOK_URL);
        int after = getCartCount();
        Assert.assertTrue(after > before,
                "TC_CART_R_004 FAILED – Cart icon should increase. Before: " + before + " After: " + after);
    }

    // ── TC_CART_R_005 ─────────────────────────────────────────────────
    @Test(priority = 5, description = "TC_CART_R_005 - Functional: Out-of-stock product button disabled or OOS shown")
    public void TC_CART_R_005_outOfStockProduct() {
        login();

        // 1. Navigate to a known low-stock/OOS product (HTC Touch HD)
        driver.get(BASE_URL + "/index.php?route=product/product&product_id=28");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("button-cart"))).click();

        driver.get(BASE_URL + "/index.php?route=checkout/cart");
        boolean warningFound = false;
        try {
            String warningText = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".alert-danger, .text-danger"))).getText();
            warningFound = warningText.contains("Products marked with *** are not available");
        } catch (Exception e) {
            warningFound = driver.getPageSource().contains("***");
        }

        Assert.assertTrue(warningFound,
                "TC_CART_R_005 FAILED – No Out-of-Stock warning (***) found in the cart.");
    }

    private void sleep(long ms) { try { Thread.sleep(ms); } catch (InterruptedException ignored) {} }
}
