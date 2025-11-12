package com.rapicredit.interactions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Interaction;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

public class CloseOverlays implements Interaction {

    public static CloseOverlays now() { return new CloseOverlays(); }

    @Override
    public <T extends Actor> void performAs(T actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        try {
            String script = "(function(){\n" +
                    "  var tries=0; function attempt(){\n" +
                    "    try{\n" +
                    "      var closeSelectors = ['.popup .close', '.popup__close', '.close', '.close-btn', '.modal__close', '.mfp-close', '[aria-label\\*=\\\"close\\\"]', 'button[title\\*=\\\"close\\\"]', '.om-close', '.cookie-dismiss', '.newsletter .close', '.fancybox-close', '.swal2-close', '.banner-close'];\n" +
                    "      closeSelectors.forEach(function(sel){ try{ var els = document.querySelectorAll(sel); els.forEach(function(e){ try{ e.click(); e.remove(); }catch(x){} }); }catch(x){} });\n" +
                    "    }catch(e){}\n" +
                    "    try{ Array.from(document.querySelectorAll('[id^=\\\"om-\\\"], .popup, .overlay, .modal, .cookie-consent, .cookie-banner, .om-popup, .omnibar, .newsletter, .modal-backdrop, .banner, .advertisement')).forEach(function(e){ try{ e.style.display='none'; e.remove(); }catch(x){} }); }catch(e){}\n" +
                    "    try{ Array.from(document.querySelectorAll('iframe')).forEach(function(f){ try{ var r = f.getBoundingClientRect(); if(r.width>200 && r.height>150){ try{ f.parentNode && f.parentNode.removeChild(f); }catch(x){} } }catch(x){} }); }catch(e){}\n" +
                    "    try{ Array.from(document.querySelectorAll('body *')).forEach(function(el){ try{ var cs = window.getComputedStyle(el); if(cs && (cs.position==='fixed' || cs.position==='sticky') && cs.zIndex && cs.zIndex!=='auto' && parseInt(cs.zIndex)>500){ el.style.pointerEvents='none'; el.style.opacity='0'; } }catch(x){} }); }catch(e){}\n" +
                    "    try{ var evt = new KeyboardEvent('keydown', {key:'Escape', keyCode:27, which:27, code:'Escape', bubbles:true}); document.dispatchEvent(evt); }catch(e){}\n" +
                    "  }\n" +
                    "  while(tries++<6){ attempt(); try{ var now=Date.now(); while(Date.now()-now<200){} }catch(e){} }\n" +
                    "  return true;\n" +
                    "})();";
            ((JavascriptExecutor) driver).executeScript(script);
        } catch (Exception e) {
            // ignore
        }
    }
}
