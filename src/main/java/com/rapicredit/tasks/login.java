package com.rapicredit.tasks;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;
import net.serenitybdd.screenplay.actions.Enter;
import com.rapicredit.userInterface.loginPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;
import java.util.List;
import net.serenitybdd.core.pages.WebElementFacade;
import org.openqa.selenium.Keys;
import com.rapicredit.interactions.SafeClick;

public class login implements Task {
    private static final Logger LOGGER = LoggerFactory.getLogger(login.class);

    private final String email;
    private final String password;

    public login(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public static login withCredentials(String email, String password) {
        return Tasks.instrumented(login.class, email, password);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        LOGGER.debug("Filling credentials for: {}", email);
        WebDriver driver = null;
        try {
            driver = BrowseTheWeb.as(actor).getDriver();
        } catch (Exception e) {
            LOGGER.warn("No WebDriver available in actor: {}", e.getMessage());
        }

        try {
            boolean enteredEmail = tryEnterUsingTarget(actor, loginPage.NOMBRE_USUARIO, email);
            if (!enteredEmail) {
                enteredEmail = tryEnterUsingAlternatives(driver, "email", "username", "user", email);
            }

            boolean enteredPassword = tryEnterUsingTarget(actor, loginPage.CONTRASENA, password);
            if (!enteredPassword) {
                enteredPassword = tryEnterUsingAlternatives(driver, "password", "pass", "contraseña", password);
            }

            if (enteredEmail && enteredPassword) {
                LOGGER.info("Credentials filled for: {}", email);
            } else {
                LOGGER.warn("Could not reliably fill both credentials for {} (enteredEmail={}, enteredPassword={})", email, enteredEmail, enteredPassword);
            }

            // Intento de envío: 1) SafeClick en BTN_LOGIN 2) fallback: enviar ENTER desde campo contraseña o usar JS click
            try {
                actor.attemptsTo(SafeClick.on(loginPage.BTN_LOGIN));
                LOGGER.debug("Tried SafeClick on BTN_LOGIN");
            } catch (Exception e) {
                LOGGER.debug("SafeClick on BTN_LOGIN failed: {}", e.getMessage());
                try {
                    // intentar enviar ENTER desde el campo contraseña si está disponible
                    WebElementFacade pwd = null;
                    try {
                        pwd = loginPage.CONTRASENA.resolveFor(actor);
                    } catch (Exception ignored) { }

                    if (pwd != null) {
                        try {
                            pwd.sendKeys(Keys.ENTER);
                            LOGGER.debug("Sent ENTER on password field as fallback");
                        } catch (Exception ex) {
                            // fallback a JS click en el primer botón encontrado
                            try {
                                WebDriver drv = BrowseTheWeb.as(actor).getDriver();
                                List<WebElement> buttons = drv.findElements(By.cssSelector("button[type='submit'], input[type='submit'], button, .btn"));
                                for (WebElement b : buttons) {
                                    if (b.isDisplayed()) {
                                        ((JavascriptExecutor) drv).executeScript("arguments[0].click();", b);
                                        LOGGER.debug("JS click on fallback button after enter failed");
                                        break;
                                    }
                                }
                            } catch (Exception ex2) {
                                LOGGER.debug("JS fallback click also failed: {}", ex2.getMessage());
                            }
                        }
                    } else {
                        // si no hay campo contraseña resolvible, intentar JS click directo
                        try {
                            WebDriver drv = BrowseTheWeb.as(actor).getDriver();
                            List<WebElement> buttons = drv.findElements(By.cssSelector("button[type='submit'], input[type='submit'], button, .btn"));
                            for (WebElement b : buttons) {
                                if (b.isDisplayed()) {
                                    ((JavascriptExecutor) drv).executeScript("arguments[0].click();", b);
                                    LOGGER.debug("JS click on fallback button performed (no pwd field)");
                                    break;
                                }
                            }
                        } catch (Exception ex2) {
                            LOGGER.debug("JS fallback click failed: {}", ex2.getMessage());
                        }
                    }
                } catch (Exception ex) {
                    LOGGER.debug("Fallback submit attempts failed: {}", ex.getMessage());
                }
            }

        } catch (Exception e) {
            LOGGER.error("Error filling credentials for {}: {}", email, e.getMessage(), e);
            // No rethrow: permitimos que el flujo continúe para que la verificación del banner ocurra
        }
    }

    private <T extends Actor> boolean tryEnterUsingTarget(T actor, net.serenitybdd.screenplay.targets.Target target, String value) {
        try {
            if (target != null) {
                try {
                    actor.attemptsTo(Enter.theValue(value).into(target));
                    return true;
                } catch (Exception e) {
                    LOGGER.debug("Entering using Target {} failed: {}", target.getName(), e.getMessage());
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Error resolving target {}: {}", target, e.getMessage());
        }
        return false;
    }

    private boolean tryEnterUsingAlternatives(WebDriver driver, String... args) {
        if (driver == null) return false;
        try {
            // Buscar inputs por varios criterios: id/name/type
            List<WebElement> inputs = driver.findElements(By.cssSelector("input[type='email'], input[type='password'], input[type='text'], input[type='tel']"));
            for (WebElement input : inputs) {
                try {
                    String id = input.getAttribute("id");
                    String name = input.getAttribute("name");
                    String type = input.getAttribute("type");
                    String combined = (id + " " + name + " " + type).toLowerCase();
                    for (String arg : args) {
                        if (combined.contains(arg.toLowerCase())) {
                            return setInputValue(driver, input, args[args.length-1]);
                        }
                    }
                } catch (Exception ignored) {}
            }
            // Fallback: si no encontramos por heurística, usar el primer input visible
            for (WebElement input : inputs) {
                if (input.isDisplayed() && input.isEnabled()) {
                    return setInputValue(driver, input, args[args.length-1]);
                }
            }
        } catch (Exception e) {
            LOGGER.debug("tryEnterUsingAlternatives error: {}", e.getMessage());
        }
        return false;
    }

    private boolean setInputValue(WebDriver driver, WebElement input, String value) {
        try {
            // Intentar clear + sendKeys primero
            try {
                input.clear();
                input.sendKeys(value);
                return true;
            } catch (Exception e) {
                // fallback a JS
                try {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].focus(); arguments[0].value=arguments[1]; arguments[0].dispatchEvent(new Event('input'));", input, value);
                    return true;
                } catch (Exception ex) {
                    LOGGER.debug("JS fallback failed: {}", ex.getMessage());
                }
            }
        } catch (Exception e) {
            LOGGER.debug("setInputValue error: {}", e.getMessage());
        }
        return false;
    }
}
