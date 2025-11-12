package com.rapicredit.stepdefinitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.When;
import io.cucumber.datatable.DataTable;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.actors.OnlineCast;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.serenitybdd.screenplay.Actor;
import com.rapicredit.userInterface.registerPage;
import com.rapicredit.tasks.register;
import com.rapicredit.utils.CommonActions;
import com.rapicredit.interactions.ShowMessageOnPage;
import org.openqa.selenium.WebDriver;

import java.util.List;
import java.util.Map;

public class RegisterStep {

    private String successUrlSubstring;

    @Before
    public void setStage() {
        OnStage.setTheStage(new OnlineCast());
        OnStage.theActorCalled("User");
        // Leer propiedad JVM register.success.url si está presente
        String prop = System.getProperty("register.success.url");
        if (prop != null && !prop.isBlank()) {
            this.successUrlSubstring = prop;
        } else {
            this.successUrlSubstring = registerPage.URL_AFTER_REGISTER;
        }
    }

    @When("the user fills the registration form with:")
    public void the_user_fills_registration_form_with(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);
        if (rows == null || rows.isEmpty()) {
            throw new IllegalArgumentException("Registration table must contain at least one row of data");
        }
        Map<String, String> data = rows.get(0);

        // Normalizar claves comunes (soportar acentos y nombres en español/inglés)
        String idNumber = firstNonEmptyKey(data, "identificaciónNúmero", "identificacionNumero", "identificaciónNúmero", "identificaciónNúmero", "identificaciónNumero", "idNumber", "identificationNumber");
        String firstName = firstNonEmptyKey(data, "primerNombre", "primerNombre", "firstName", "nombre");
        String lastName = firstNonEmptyKey(data, "primerApellido", "primerApellido", "lastName", "apellido");
        String email = firstNonEmptyKey(data, "email", "Email", "correo", "correoElectronico");
        String reEmail = firstNonEmptyKey(data, "reEmail", "reemail", "reEmail", "re_email");
        String mobile = firstNonEmptyKey(data, "móvil", "movil", "mobile", "telefono");
        String password = firstNonEmptyKey(data, "contraseña", "contrasena", "password", "pass");
        String rePassword = firstNonEmptyKey(data, "rePassword", "repassword", "re_password");

        // Si reEmail/rePassword están vacíos, usar email/password
        if (reEmail == null || reEmail.isBlank()) reEmail = email;
        if (rePassword == null || rePassword.isBlank()) rePassword = password;

        OnStage.theActorInTheSpotlight().attemptsTo(
                register.withData(idNumber == null ? "" : idNumber,
                        firstName == null ? "" : firstName,
                        lastName == null ? "" : lastName,
                        email == null ? "" : email,
                        reEmail == null ? "" : reEmail,
                        mobile == null ? "" : mobile,
                        password == null ? "" : password,
                        rePassword == null ? "" : rePassword)
        );
    }

    // helper
    private String firstNonEmptyKey(Map<String, String> data, String... keys) {
        for (String k : keys) {
            if (k == null) continue;
            // tries exact key first
            if (data.containsKey(k) && data.get(k) != null && !data.get(k).isBlank()) return data.get(k).trim();
            // try variants: trim keys from table headers (remove spaces)
            String k2 = k.trim();
            if (!k2.equals(k) && data.containsKey(k2) && data.get(k2) != null && !data.get(k2).isBlank()) return data.get(k2).trim();
        }
        // fallback: try to find a key ignoring accents and case
        for (Map.Entry<String, String> e : data.entrySet()) {
            String keyNormalized = normalize(e.getKey());
            for (String k : keys) {
                if (k == null) continue;
                if (keyNormalized.equalsIgnoreCase(normalize(k))) {
                    return e.getValue() != null ? e.getValue().trim() : null;
                }
            }
        }
        return null;
    }

    private String normalize(String s) {
        if (s == null) return "";
        String t = s.trim().toLowerCase();
        // remove common accent chars
        t = t.replace('á', 'a').replace('é', 'e').replace('í', 'i').replace('ó', 'o').replace('ú', 'u').replace('ñ', 'n');
        t = t.replaceAll("[^a-z0-9]", "");
        return t;
    }

    @When("the user submits the register form")
    public void the_user_submits_the_register_form() {
        Actor actor = OnStage.theActorInTheSpotlight();
        boolean ok = CommonActions.submitAndCheck(actor, registerPage.BTN_REGISTER, successUrlSubstring, 8);
        if (ok) {
            try {
                WebDriver driver = BrowseTheWeb.as(actor).getDriver();
                String current = driver != null ? driver.getCurrentUrl() : "<null>";
                System.out.println("[TEST] Registro exitoso - URL: " + current);
            } catch (Exception e) {
                System.out.println("[WARN] Unable to read current URL after register: " + e.getMessage());
            }
            // mostrar mensaje de éxito en la página
            actor.attemptsTo(ShowMessageOnPage.show("Registro exitoso", 5));
            // si existe una propiedad para la siguiente URL, abrirla
            String next = System.getProperty("register.next.url");
            if (next != null && !next.isBlank()) {
                CommonActions.openHome(actor, next);
            }
        } else {
            System.out.println("[TEST] Registro fallido - comprobación realizada por CommonActions");
        }
    }

}
