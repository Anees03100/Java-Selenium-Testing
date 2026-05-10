import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;
import java.time.Duration;

/**
 * Module    : User Registration
 * Website   : http://tutorialsninja.com/demo
 * Test IDs  : TC_REG_001 to TC_REG_005
 * Techniques: Functional Testing, UI Testing
 *
 * OpenCart locators – verified from live site:
 *   id="input-firstname"  id="input-lastname"
 *   id="input-email"      id="input-telephone"
 *   id="input-password"   id="input-confirm"
 *   name="agree"          input[value='Continue']
 */
public class RegistrationTests {

    private WebDriver driver;
    private WebDriverWait wait;
    private final String URL = "https://tutorialsninja.com/demo/index.php?route=account/register";

    @BeforeMethod
    public void setup() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.get(URL);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterMethod
    public void tearDown() throws InterruptedException {
        if (driver != null) driver.quit();
        Thread.sleep(6000);
    }

    // ── Shared fill helper ────────────────────────────────────────────
    private void fillForm(String first, String last, String email,
                          String phone, String pass, String confirm) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("input-firstname")));
        driver.findElement(By.id("input-firstname")).clear();
        driver.findElement(By.id("input-firstname")).sendKeys(first);
        driver.findElement(By.id("input-lastname")).clear();
        driver.findElement(By.id("input-lastname")).sendKeys(last);
        driver.findElement(By.id("input-email")).clear();
        driver.findElement(By.id("input-email")).sendKeys(email);
        driver.findElement(By.id("input-telephone")).clear();
        driver.findElement(By.id("input-telephone")).sendKeys(phone);
        driver.findElement(By.id("input-password")).clear();
        driver.findElement(By.id("input-password")).sendKeys(pass);
        driver.findElement(By.id("input-confirm")).clear();
        driver.findElement(By.id("input-confirm")).sendKeys(confirm);
    }

    private void agreeAndSubmit() {
        WebElement agree = driver.findElement(By.name("agree"));
        if (!agree.isSelected()) agree.click();
        driver.findElement(By.cssSelector("input[value='Continue']")).click();
    }

    // ── TC_REG_001 ────────────────────────────────────────────────────
    @Test(priority = 1, description = "TC_REG_001 - Valid registration with all required fields")
    public void TC_REG_001_validRegistration() {
        String email = "sqe_" + System.currentTimeMillis() + "@test.com";
        fillForm("Ali", "Khan", email, "03001234567", "Test@1234", "Test@1234");
        agreeAndSubmit();

        wait.until(ExpectedConditions.urlContains("account/success"));
        Assert.assertTrue(driver.getCurrentUrl().contains("account/success"),
                "TC_REG_001 FAILED – URL: " + driver.getCurrentUrl());
    }

    // ── TC_REG_002 ────────────────────────────────────────────────────
    @Test(priority = 2, description = "TC_REG_002 - Registration with duplicate email")
    public void TC_REG_002_duplicateEmail() {
        String email = "dup" + System.currentTimeMillis() + "@test.com";
        fillForm("Ali", "Khan", email, "03001234567", "Test@1234", "Test@1234");
        agreeAndSubmit();
        wait.until(ExpectedConditions.urlContains("account/success"));
        driver.get("https://tutorialsninja.com/demo/index.php?route=account/logout");
        driver.get(URL);
        fillForm("Ali", "Khan", email, "03001234567", "Test@1234", "Test@1234");
        agreeAndSubmit();
        WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-danger")));
        Assert.assertTrue(alert.getText().contains("E-Mail Address is already registered!"));
    }

    // ── TC_REG_003 ────────────────────────────────────────────────────
    @Test(priority = 3, description = "TC_REG_003 - Registration with blank mandatory fields")
    public void TC_REG_003_blankFields() {
        // Click Continue without filling anything
        driver.findElement(By.cssSelector("input[value='Continue']")).click();

        // OpenCart shows inline validation – at least one field shows error
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("div.text-danger, .has-error")));
        boolean errorsShown = !driver.findElements(
                By.cssSelector("div.text-danger, .has-error")).isEmpty();
        Assert.assertTrue(errorsShown, "TC_REG_003 FAILED – Expected field validation errors.");
    }

    // ── TC_REG_004 ────────────────────────────────────────────────────
    @Test(priority = 4, description = "TC_REG_004 - UI: All registration form elements visible")
    public void TC_REG_004_uiElementsVisible() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("input-firstname")));
        Assert.assertTrue(driver.findElement(By.id("input-firstname")).isDisplayed(),   "First Name missing.");
        Assert.assertTrue(driver.findElement(By.id("input-lastname")).isDisplayed(),    "Last Name missing.");
        Assert.assertTrue(driver.findElement(By.id("input-email")).isDisplayed(),       "Email missing.");
        Assert.assertTrue(driver.findElement(By.id("input-telephone")).isDisplayed(),   "Phone missing.");
        Assert.assertTrue(driver.findElement(By.id("input-password")).isDisplayed(),    "Password missing.");
        Assert.assertTrue(driver.findElement(By.id("input-confirm")).isDisplayed(),     "Confirm Pwd missing.");
        Assert.assertTrue(driver.findElement(By.name("agree")).isDisplayed(),           "Privacy checkbox missing.");
        Assert.assertTrue(driver.findElement(By.cssSelector("input[value='Continue']")).isDisplayed(), "Continue button missing.");
    }

    // ── TC_REG_005 ────────────────────────────────────────────────────
    @Test(priority = 5, description = "TC_REG_005 - Registration with invalid email format")
    public void TC_REG_005_invalidEmail() {
        // Find the element FIRST before using it in the assertion
        WebElement emailField = driver.findElement(By.id("input-email"));
        fillForm("Sara", "Ahmed", "NOTANEMAIL", "03009876543", "Test@1234", "Test@1234");
        agreeAndSubmit();
        String validationMessage = emailField.getAttribute("validationMessage");
        Assert.assertTrue(!validationMessage.isEmpty(), "Browser did not block the invalid email!");
    }

    @Test
    public void verifySignupFormRefreshBehavior() {

        driver.get("https://tutorialsninja.com/demo/index.php?route=account/register");

        driver.findElement(By.id("input-firstname")).sendKeys("iftikhar");
        driver.findElement(By.id("input-lastname")).sendKeys("hussain");
        driver.findElement(By.id("input-email")).sendKeys("iffi@gmail.com");

        driver.navigate().refresh();

        String firstName = driver.findElement(By.id("input-firstname")).getAttribute("value");

        Assert.assertEquals(firstName, "");

        System.out.println("Form data cleared after refresh");
    }

}

