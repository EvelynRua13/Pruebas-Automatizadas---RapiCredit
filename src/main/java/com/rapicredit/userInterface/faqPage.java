package com.rapicredit.userInterface;

import net.serenitybdd.screenplay.targets.Target;
import org.openqa.selenium.By;

public class faqPage {

    public static final String URL = "https://www.rapicredit.com";
    public static final Target FAQ_URL = Target.the("FAQ link").located(By.cssSelector("a[href*='preguntas-frecuentes']"));
    public static final Target INPUT_SEARCH = Target.the("FAQ search input").located(By.cssSelector(
            "input[id*='preguntas'], input[id*='search'], input[name*='preguntas'], input[name*='pregunta'], " +
            "input[placeholder*='pregunt'], input[placeholder*='Buscar'], input[aria-label*='Buscar'], textarea[name*='pregunta']"));

    private faqPage() { /* utility class */ }

}
