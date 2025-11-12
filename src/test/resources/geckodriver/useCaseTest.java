package geckodriver;
import io.github.bonigarcia.wdm.WebDriverManager;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import net.serenitybdd.core.annotations.Managed;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rapicredit.tasks.*;
import com.rapicredit.ui.HomePage;

@ExtendWith(SerenityJUnit5Extension.class)
class UseCaseTests {
    }

    @Managed(driver = "firefox")
    WebDriver browser;

    private Actor user;

    @BeforeEach
    void setUp() {
        user = Actor.named("user");
        user.can(BrowseTheWeb.with(browser));
        OnStage.setTheStage(new Cast());
    }

    @Test
    void iniciar_sesion() {
        user.attemptsTo(
                openHomePage.open(),
                login.withCredentials("everua77@gmail.com", "Megumihonnie78")
        );
        assertTrue(browser.getCurrentUrl().contains("rapicredit"), "Debe permanecer en el sitio");
    }

    @Test
    void registrarse() {
        user.attemptsTo(
                openHomePage.open(),
                register.withData("nuevo@example.com", "pass1234")
        );
        assertTrue(browser.getPageSource().toLowerCase().contains("gracias"), "Debe mostrar confirmación");
    }