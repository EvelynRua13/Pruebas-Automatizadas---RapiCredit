package com.rapicredit.userInterface;

import net.serenitybdd.screenplay.targets.Target;
import org.openqa.selenium.By;

public class contactPage {
    private contactPage() {}

    public static final String URL = "https://www.rapicredit.com/";
    public static final Target LINK_CONTACT = Target.the("link Contáctanos").located(By.linkText("Contáctanos"));
    public static final Target NAME = Target.the("nombre").located(By.name("your-name"));
    public static final Target EMAIL = Target.the("email").located(By.name("your-email"));
    public static final Target ID = Target.the("cédula").located(By.name("cedula"));
    public static final Target MOBILE = Target.the("celular").located(By.name("celular"));
    public static final Target REASON_SELECT = Target.the("select razón").located(By.name("reason"));
    public static final Target OPTION_REASON_4 = Target.the("opción razón 4").located(By.cssSelector("select option:nth-child(4)"));
    public static final Target MESSAGE = Target.the("mensaje").located(By.name("your-message"));
    public static final Target ACCEPT = Target.the("aceptación").located(By.name("acceptance-726"));
    public static final Target BTN_SEND = Target.the("botón enviar").located(By.cssSelector(".has-spinner, button[type='submit'], .btn"));
}
