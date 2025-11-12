package com.rapicredit.stepdefinitions;

import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.actors.OnlineCast;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.serenitybdd.screenplay.Actor;
import com.rapicredit.userInterface.loginPage;
import com.rapicredit.tasks.login;
import com.rapicredit.utils.CommonActions;
import com.rapicredit.interactions.ShowMessageOnPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import java.util.UUID;
import java.util.List;
import org.openqa.selenium.JavascriptExecutor;
import com.rapicredit.utils.TestState;
import org.junit.Assert;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class LoginStep {

    private String successUrlSubstring;

    // Helper para mostrar un mensaje temporal con manejo de excepciones
    private void showTemp(Actor actor, String message) {
        try {
            if (actor != null) actor.attemptsTo(ShowMessageOnPage.show(message, 2));
        } catch (Exception ignored) { /* ignore: best-effort UI message */ }
    }

    // Versión simplificada y confiable para detectar popups
    private boolean waitForSweetAlert(WebDriver driver, int timeoutSeconds) {
        if (driver == null) return false;
        try {
            WebDriverWait wait = new WebDriverWait(driver, Math.max(1, timeoutSeconds));
            // Intentar localizar contenedor de su contenido
            By[] selectors = new By[]{By.cssSelector("#swal2-content"), By.cssSelector(".swal2-popup"), By.cssSelector(".swal2-container"), By.cssSelector(".swal2-html-container")};
            for (By s : selectors) {
                try {
                    wait.until(ExpectedConditions.visibilityOfElementLocated(s));
                    return true;
                } catch (Exception e) {
                    // continuar con el siguiente selector
                }
            }
        } catch (Exception ignored) { 
            //ignore  
            }
        return false;
    }

    @Before
    public void setStage() {
        OnStage.setTheStage(new OnlineCast());
        // cada escenario tendrá su propio actor con nombre único para aislar la ejecución
        String actorName = "User-" + UUID.randomUUID().toString().substring(0, 8);
        OnStage.theActorCalled(actorName);
        // Leer propiedad JVM login.success.url si está presente
        String prop = System.getProperty("login.success.url");
        if (prop != null && !prop.isBlank()) {
            this.successUrlSubstring = prop;
        } else {
            this.successUrlSubstring = loginPage.URL_AFTER_LOGIN;
        }
    }

    @After
    public void tearDown() {
        try {
            // cerrar navegador y limpiar para aislar escenarios
            Actor actor = OnStage.theActorInTheSpotlight();
            WebDriver driver = BrowseTheWeb.as(actor).getDriver();
            if (driver != null) {
                try { driver.manage().deleteAllCookies(); } catch (Exception ignored) 
                {
                    //ignore
                }
                try { driver.quit(); } catch (Exception ignored)
                {
                    //ignore
                }
            }
        } catch (Exception ignored)
        {
            //ignore
        }
        // limpiar estado del actor
        try { TestState.clearAll(); } catch (Exception ignored)
        {
            //ignore
        }
    }

    @When("the user enters email {string} and password {string}")
    public void the_user_enters_email_and_password(String email, String password) {
        OnStage.theActorInTheSpotlight().attemptsTo(
                login.withCredentials(email, password)
        );
    }

    @When("the user submits the login form")
    public void the_user_submits_the_login_form() {
        Actor actor = OnStage.theActorInTheSpotlight();
        // versión revertida: comportamiento simple y estable
        boolean ok = CommonActions.submitAndCheck(actor, loginPage.BTN_LOGIN, successUrlSubstring, 8);

        if (ok) {
            // Caso exitoso: intentar detectar la página destino o elementos clave
            try {
                WebDriver driver = BrowseTheWeb.as(actor).getDriver();
                int postTimeout = Integer.getInteger("login.post.timeout", 15);
                waitForPostLogin(driver, successUrlSubstring, postTimeout);
            } catch (Exception ignored)
            {
                //ignore
            }
            showTemp(actor, "Login correcto");
            // registrar éxito explícito
            try { TestState.putMessage(actor.getName(), "Login exitoso"); } catch (Exception ignored)
            {
                //ignore
            }
        } else {
            // Caso fallido: esperar popup y registrar estado para el Then
            WebDriver driver = null;
            try { driver = BrowseTheWeb.as(actor).getDriver(); } catch (Exception ignored)
            {
                //ignore
            }

            boolean popupDetected = false;
            if (driver != null) {
                // intentar detectar popup en la ventana actual
                popupDetected = waitForSweetAlert(driver, Math.max(5, Integer.getInteger("login.popup.timeout", 10)));
            }

            if (popupDetected) {
                try { ((JavascriptExecutor) BrowseTheWeb.as(actor).getDriver()).executeScript("var b=document.querySelector('.swal2-confirm'); if(b){b.click();}"); }
                catch (Exception ignored)
                {
                    //ignore
                }
                showTemp(actor, "Login fallido (popup)");
                try { TestState.putMessage(actor.getName(), "Login fallido - popup"); } catch (Exception ignored)
                {
                    //ignore
                }
            } else {
                showTemp(actor, "Login fallido");
                try { TestState.putMessage(actor.getName(), "Login fallido - sin popup"); } catch (Exception ignored)
                {
                    //ignore
                }
            }
        }
    }

    @When("the user submits the login form and accepts failure banner")
    public void the_user_submits_and_accepts_failure() {
        // reutilizar los pasos existentes para mantener la lógica en un solo lugar
        the_user_submits_the_login_form();
        the_user_accepts_the_failure_banner();
    }

    // Esperar activamente a que la página posterior al login sea estable y contenga los elementos esperados
    private void waitForPostLogin(WebDriver driver, String successUrlSubstring, int timeoutSeconds) {
        if (driver == null) return;
        try {
            WebDriverWait wait = new WebDriverWait(driver, Math.max(1, timeoutSeconds));
            // esperar a que la URL contenga la subcadena o que elementos clave sean visibles
            if (successUrlSubstring != null && !successUrlSubstring.isBlank()) {
                try { wait.until(ExpectedConditions.urlContains(successUrlSubstring)); return; } catch (Exception ignored)
                {
                    //ignore
                }
            }
            By[] selectors = new By[]{By.cssSelector("div.dashboard"), By.cssSelector("nav.main-navbar"), By.cssSelector("footer.main-footer")};
            for (By s : selectors) {
                try { wait.until(ExpectedConditions.visibilityOfElementLocated(s)); return; } catch (Exception ignored)
                {
                    //ignore
                }
            }
        } catch (Exception ignored) {
            //ignore
        }
    }

    // Step adicional: aceptar el banner/modal de fallo si está presente
    @When("the user accepts the failure banner")
    public void the_user_accepts_the_failure_banner() {
        Actor actor = OnStage.theActorInTheSpotlight();
        WebDriver driver = null;
        try { driver = BrowseTheWeb.as(actor).getDriver(); } catch (Exception ignored) 
        {
            //ignore
        }
        if (driver == null) return;
        try {
            try {
                WebElement btn = driver.findElement(By.cssSelector(".swal2-confirm"));
                if (btn != null && btn.isDisplayed()) { btn.click(); return; }
            } catch (Exception ignored) 
            {
                //ignore
            }
            try { ((JavascriptExecutor) driver).executeScript("var b=document.querySelector('.swal2-confirm, .swal2-close'); if(b){b.click();}"); } catch (Exception ignored) 
            {
                //ignore
            }
        } catch (Exception ignored) 
        {
            //ignore
        }
    }

    // Then para verificar que se mostró el banner de éxito (registro en TestState por submit)
    @Then("the test shows a success banner")
    public void the_test_shows_a_success_banner() {
        Actor actor = OnStage.theActorInTheSpotlight();
        String msg = null;
        try { msg = TestState.getMessage(actor.getName()); } catch (Exception ignored) 
        {
            //ignore
        }
        if (msg == null) {
            try {
                WebDriver driver = BrowseTheWeb.as(actor).getDriver();
                List<WebElement> els = driver.findElements(By.cssSelector("#swal2-content, .swal2-html-container"));
                if (els != null && !els.isEmpty()) {
                    for (WebElement e : els) {
                        try { if (e.isDisplayed() && e.getText() != null && !e.getText().isBlank()) return; } catch (Exception ignored) 
                        {
                            //ignore
                        }
                    }
                }
            } catch (Exception ignored) 
            {
                //ignore
            }
            Assert.fail("No se detectó banner de éxito ni mensaje en TestState");
        }
        Assert.assertTrue("El mensaje de estado no indica éxito: " + msg, msg.toLowerCase().contains("exitos") || msg.toLowerCase().contains("test exitoso") || msg.toLowerCase().contains("login exitoso"));
    }

}
