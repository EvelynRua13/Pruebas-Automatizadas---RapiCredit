package com.rapicredit.tasks;

import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.actions.Open;
import net.serenitybdd.screenplay.Performable;
import com.rapicredit.userInterface.loginPage;

public class openHomePage {
    public static Performable open() {
        return Task.where("{0} abre la página de RapiCredit",
                Open.url(loginPage.URL));
    }
}
