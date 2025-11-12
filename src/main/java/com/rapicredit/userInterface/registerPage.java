package com.rapicredit.userInterface;

import net.serenitybdd.screenplay.targets.Target;
import org.openqa.selenium.By;

public class registerPage {
    public static final String URL_AFTER_REGISTER = "/cliente";
    public static final Target LINK_REGISTRAR = Target.the("link Registrar").located(By.linkText("Regístrate"));
    public static final Target IDENTIFICATION_TYPE = Target.the("tipo de identificación").located(By.name("identificationType"));
    public static final Target OPTION_IDENTIFICATION = Target.the("opción identificación").located(By.cssSelector(".option"));
    public static final Target IDENTIFICATION_NUMBER = Target.the("número de identificación").located(By.id("identificationNumber"));
    public static final Target FIRST_NAME = Target.the("primer nombre").located(By.id("primerNombre"));
    public static final Target LAST_NAME = Target.the("primer apellido").located(By.name("primerApellido"));
    public static final Target EMAIL = Target.the("email").located(By.id("email"));
    public static final Target REEMAIL = Target.the("reEmail").located(By.id("reEmail"));
    public static final Target MOBILE = Target.the("móvil").located(By.id("mobile"));
    public static final Target PASSWORD = Target.the("contraseña").located(By.id("password"));
    public static final Target REPASSWORD = Target.the("rePassword").located(By.id("rePassword"));
    public static final Target TERMS = Target.the("terms and conditions").located(By.name("termsAndConditions"));
    public static final Target DATA_TREATMENT = Target.the("tratamiento de datos").located(By.name("tratamiento_de_datos"));
    public static final Target BTN_REGISTER = Target.the("botón registrar").located(By.cssSelector(".btn"));
}
