package com.rapicredit.interactions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.JavascriptExecutor;

public class ShowSuccessBanner implements Interaction {
    private final String message;
    private final int secondsVisible;

    public ShowSuccessBanner(String message, int secondsVisible) {
        this.message = message;
        this.secondsVisible = secondsVisible;
    }

    public static ShowSuccessBanner now(String message, int secondsVisible) {
        return new ShowSuccessBanner(message, secondsVisible);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        try {
            // Ensure we are in the latest window/context before injecting UI
            try {
                actor.attemptsTo(SwitchToLastWindow.now());
            } catch (Exception ignored) {}

            System.out.println("[TEST] Injecting success banner with message: " + message);

            JavascriptExecutor js = (JavascriptExecutor) BrowseTheWeb.as(actor).getDriver();
            String script = "(function(){"
                    + "var b = document.createElement('div');"
                    + "b.id='serenity-success-banner';"
                    + "b.style.position='fixed';"
                    + "b.style.left='0';"
                    + "b.style.right='0';"
                    + "b.style.top='0';"
                    + "b.style.zIndex='2147483647';"
                    + "b.style.background='rgba(40,167,69,0.95)';"
                    + "b.style.color='#fff';"
                    + "b.style.fontSize='20px';"
                    + "b.style.textAlign='center';"
                    + "b.style.padding='12px';"
                    + "b.style.fontFamily='Arial,Helvetica,sans-serif';"
                    + "b.innerText='" + message.replace("'","\\'") + "';"
                    + "document.body.appendChild(b);"
                    + "setTimeout(function(){var e = document.getElementById('serenity-success-banner'); if(e) e.parentNode.removeChild(e);}," + (secondsVisible*1000) + ");"
                    + "})();";
            js.executeScript(script);
            // small pause to let browser render the banner for screenshots
            try { Thread.sleep(700); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
            System.out.println("[TEST] Banner injected (will be visible for " + secondsVisible + "s)");
        } catch (Exception e) {
            System.out.println("[WARN] Failed to inject banner: " + e.getMessage());
            // ignore errors in UI injection
        }
    }
}
