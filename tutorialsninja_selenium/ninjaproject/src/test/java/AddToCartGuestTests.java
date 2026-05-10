import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;
import java.time.Duration;

public class AddToCartGuestTests {

    private WebDriver driver;
    private WebDriverWait wait;
    private final String BASE_URL   = "https://tutorialsninja.com/demo";
    private final String IPHONE_URL = BASE_URL + "/index.php?route=product/product&product_id=40";

    @BeforeMethod
    public void setup() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        // Clear cookies to ensure a fresh Guest session every time
        driver.manage().deleteAllCookies();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            try {
                // Increased page close time (5 seconds)
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            driver.quit();
        }
    }

    private void addAsGuest(String productUrl) {
        driver.get(productUrl);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("button-cart"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.alert-success")));
    }

    private int getCartCount() {
        try {
            // Updated locator to be more robust for the "X item(s)" text
            WebElement cartTotal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cart-total")));
            String text = cartTotal.getText().trim();
            return Integer.parseInt(text.split(" ")[0]);
        } catch (Exception e) {
            return 0;
        }
    }

    @Test(priority = 1, description = "TC_CART_G_001 - Functional: Guest user adds product to cart")
    public void TC_CART_G_001_guestAddsProduct() {
        addAsGuest(IPHONE_URL);
        String alert = driver.findElement(By.cssSelector("div.alert-success")).getText();
        Assert.assertTrue(alert.contains("Success"), "Success alert not found!");
    }

    @Test(priority = 2)
    public void TC_CART_G_002_cartPersistsAfterNavigation() {
        addAsGuest(IPHONE_URL);
        int countBefore = getCartCount();
        driver.get(BASE_URL);
        int countAfter = getCartCount();
        Assert.assertEquals(countAfter, countBefore, "Cart count dropped after navigating home!");
    }

    @Test(priority = 4, description = "TC_CART_G_004 - Cross-Browser Manual Path Fix")
    public void TC_CART_G_004_crossBrowserEdge() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
        System.setProperty("webdriver.edge.driver", "C:\\Users\\hp\\Downloads\\edgedriver_win64\\msedgedriver.exe");
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--remote-allow-origins=*");

        try {
            driver = new EdgeDriver(options);
            driver.manage().window().maximize();
            wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            driver.get(IPHONE_URL);
            WebElement cartBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("button-cart")));
            cartBtn.click();

            WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.alert-success")));
            Assert.assertTrue(alert.getText().contains("Success"), "Manual Edge Test: Success message missing!");
        } catch (Exception e) {
            System.out.println("Manual Edge Start Failed: " + e.getMessage());
            Assert.fail("Edge still won't open. Ensure msedgedriver.exe is inside the folder.");
        }
    }

    @Test(priority = 5)
    public void TC_CART_G_005_smokeGuestViewCart() {
        addAsGuest(IPHONE_URL);
        driver.get(BASE_URL + "/index.php?route=checkout/cart");
        wait.until(ExpectedConditions.titleContains("Shopping Cart"));
        boolean isTablePresent = !driver.findElements(By.cssSelector(".table-responsive")).isEmpty();
        Assert.assertTrue(isTablePresent, "Shopping cart table not found!");
    }
}