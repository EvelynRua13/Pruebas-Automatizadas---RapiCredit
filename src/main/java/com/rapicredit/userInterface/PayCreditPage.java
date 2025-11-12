package com.rapicredit.userInterface;

import net.serenitybdd.screenplay.targets.Target;
import org.openqa.selenium.By;

public class PayCreditPage {
    public static final Target BTN_PAY_CREDIT = Target.the("Botón pagar crédito").located(By.linkText("Paga tu crédito"));
    // Target the actual input element inside possible wrappers
    public static final Target INPUT_CEDULA = Target.the("Input cédula").located(By.cssSelector(".rapicredit-input-wrapper__field"));
    public static final Target BTN_SUBMIT = Target.the("Botón submit pago").located(By.xpath("//button[@id='btnNextId']/span"));
    public static final Target PAYMENT_PAGE_INDICATOR = Target.the("Indicador página de pago").located(By.cssSelector(".sessionless-payments, .payment-page, .payment-container, #paymentPage"));
    public static final Target NO_PENDING_ALERT = Target.the("Aviso sin créditos").located(By.xpath("//div[contains(text(),'No hay créditos pendientes') or contains(.,'no hay créditos') or contains(@class,'confirmation-custom-dialog-container')]") );
}
