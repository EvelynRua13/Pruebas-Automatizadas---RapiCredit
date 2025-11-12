package com.rapicredit.interactions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CheckLoginSuccess implements Interaction {
    private final String urlContains;
    private final int timeoutSeconds;

    public CheckLoginSuccess(String urlContains, int timeoutSeconds) {
        this.urlContains = urlContains;
        this.timeoutSeconds = timeoutSeconds;
    }

    public static CheckLoginSuccess whenUrlContains(String substring, int timeoutSeconds) {
        return new CheckLoginSuccess(substring, timeoutSeconds);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        WebDriverWait wait = new WebDriverWait(driver, timeoutSeconds);

        ExpectedCondition<Boolean> loginDetected = drv -> {
            if (drv == null) return false;
            try {
                // 1) comprobar URL
                if (urlContains != null && !urlContains.isBlank()) {
                    try {
                        String current = drv.getCurrentUrl();
                        if (current != null && current.contains(urlContains)) return true;
                    } catch (Exception ignored) {}
                }

                // 2) comprobar texto de la página por indicadores típicos de sesión iniciada
                try {
                    Object res = ((JavascriptExecutor) drv).executeScript("return (document.body && document.body.innerText) ? document.body.innerText.toLowerCase() : '';" );
                    if (res != null) {
                        String body = res.toString();
                        if (body.contains("cerrar sesi") || body.contains("mi cuenta") || body.contains("mi perfil") || body.contains("salir") || body.contains("perfil") || body.contains("logout")) {
                            return true;
                        }
                    }
                } catch (Exception ignored) {}

                return false;
            } catch (Exception e) {
                return false;
            }
        };

        wait.until(loginDetected);
    }
}
