package com.rapicredit.userInterface;

import net.serenitybdd.screenplay.targets.Target;
import org.openqa.selenium.By;

public class loginPage {
    public static final String URL = "https://www.rapicredit.com/";
    public static final String URL_AFTER_LOGIN = "https://cliente.rapicredit.com";
    public static final Target INICIAR_SESION = Target.the("Iniciar sesión").located(By.linkText("Iniciar Sesión"));
    public static final Target NOMBRE_USUARIO = Target.the("Nombre de usuario").located(By.id("username"));
    public static final Target CONTRASENA = Target.the("Contraseña").located(By.id("password"));
    public static final Target BTN_LOGIN = Target.the("Botón login").located(By.cssSelector("#btnLogin > .mat-button-wrapper"));

    private void loginPage() {}
}
