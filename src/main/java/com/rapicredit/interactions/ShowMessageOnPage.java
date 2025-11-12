package com.rapicredit.interactions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import com.rapicredit.utils.TestState;

public class ShowMessageOnPage implements Interaction {
    private final String message;
    private final int timeoutSeconds;

    public ShowMessageOnPage(String message, int timeoutSeconds) {
        this.message = message;
        this.timeoutSeconds = timeoutSeconds;
    }

    public static ShowMessageOnPage show(String message) {
        return new ShowMessageOnPage(message, 5);
    }

    public static ShowMessageOnPage show(String message, int timeoutSeconds) {
        return new ShowMessageOnPage(message, timeoutSeconds);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        try {
            String safeMessage = message.replace("\n", "\\n").replace("'", "\\'");
            String script = "(function(){" +
                    "var id='serenity-banner-'+Date.now();" +
                    "var d=document.createElement('div');" +
                    "d.id=id;" +
                    "d.setAttribute('data-serenity','true');" +
                    "d.style.position='fixed';" +
                    "d.style.zIndex='2147483647';" +
                    "d.style.left='10px';" +
                    "d.style.right='10px';" +
                    "d.style.top='10px';" +
                    "d.style.padding='12px 18px';" +
                    "d.style.background='#ffdddd';" +
                    "d.style.color='#660000';" +
                    "d.style.border='2px solid #cc0000';" +
                    "d.style.fontSize='16px';" +
                    "d.style.fontFamily='Arial, sans-serif';" +
                    "d.style.boxShadow='0 4px 8px rgba(0,0,0,0.2)';" +
                    "d.innerText='" + safeMessage + "';" +
                    "document.body.appendChild(d);" +
                    "window.__serenity_last_message = '" + safeMessage + "';" +
                    "setTimeout(function(){try{var el=document.getElementById(id); if(el){el.parentNode.removeChild(el);} window.__serenity_last_message='';}catch(e){}}," + timeoutSeconds*1000 + ");" +
                    "})();";
            ((JavascriptExecutor) driver).executeScript(script);

            // Registrar en TestState para que el step definition pueda leerlo
            try { TestState.putMessage(actor.getName(), message); } catch (Exception ignored) {}

            // Lanzar hilo daemon para limpiar el estado después de timeoutSeconds + 500ms
            try {
                final String actorName = actor.getName();
                final int ms = timeoutSeconds * 1000; // limpiar justo después de que termine el banner
                Thread cleaner = new Thread(() -> {
                    try { Thread.sleep(ms); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
                    try { TestState.clear(actorName); } catch (Exception ignored) {}
                });
                cleaner.setDaemon(true);
                cleaner.start();
            } catch (Exception ignored) {}
        } catch (Exception e) {
            throw new RuntimeException("Unable to show message on page: " + e.getMessage(), e);
        }
    }
}
