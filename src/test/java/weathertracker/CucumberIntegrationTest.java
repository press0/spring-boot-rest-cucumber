package weathertracker;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/features/weathertracker")
public class CucumberIntegrationTest extends SpringIntegrationTest {
}