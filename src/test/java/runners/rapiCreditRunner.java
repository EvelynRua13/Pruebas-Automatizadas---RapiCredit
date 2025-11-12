package runners;

import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = "com.rapicredit.stepdefinitions",
        tags = "@login or @login_fail or @register or @contact_form or @faq or @pay_credit",
        snippets = CucumberOptions.SnippetType.CAMELCASE
)
public class rapiCreditRunner {
}
