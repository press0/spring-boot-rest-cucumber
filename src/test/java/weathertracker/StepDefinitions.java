package weathertracker;

import static org.junit.Assert.assertEquals;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.java8.En;
import io.cucumber.datatable.DataTable;
import weathertracker.measurements.Measurement;
import weathertracker.statistics.AggregateResult;
import weathertracker.statistics.Statistic;


public class StepDefinitions extends SpringIntegrationTest implements En {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Measurement.Builder builder = new Measurement.Builder();
    private final double epsilon = 0.0000001;
    private final String TIMESTAMP = "timestamp";

    public StepDefinitions() {

        When("I get a measurement for {string}", this::getMeasurement);

        Then("the response body is:", (DataTable dataTable) -> {
            List<Map<String, String>> maps = dataTable.asMaps();
            Measurement expected = buildMeasurement(maps.get(0));

            String responseBody = getLatestResponse().getBody();
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            JsonNode jsonNode = mapper.readTree(responseBody);
            Measurement results = mapper.readValue(jsonNode.toString(), Measurement.class);

            assertEquals(expected.getMetrics().size(), results.getMetrics().size());
            assertEquals(expected.getTimestamp().toEpochSecond(), results.getTimestamp().toEpochSecond());

            for (Map.Entry<String, Double> entry : expected.getMetrics().entrySet()) {
                assertEquals(expected.getMetric(entry.getKey()), results.getMetric(entry.getKey()));
            }
        });

        When("I get stats with parameters:", (DataTable dataTable) -> {
            List<Map<String, String>> list = dataTable.asMaps();
            StringBuilder sb = new StringBuilder("?");
            for (Map<String, String> map : list) {
                sb.append(map.get("param"));
                sb.append("=");
                sb.append(map.get("value"));
                sb.append("&");
            }
            sb.delete(sb.length() - 1, sb.length());
            getStats(sb.toString());
        });

        Then("the response body is an array of:", (DataTable dataTable) -> {
            List<Map<String, String>> list = dataTable.asMaps();
            List<AggregateResult> expected = buildExpectedResults(list);

            String responseBody = getLatestResponse().getBody();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(responseBody);

            List<AggregateResult> results = mapper.readValue(jsonNode.toString(), new TypeReference<>() {
            });

            assertEquals(expected.size(), results.size());
            for (int i = 0; i < expected.size(); i++) {
                //todo:
                //assertEquals(0, expected.get(i).compareTo(results.get(i)));
            }

        });

        Then("the response body is an empty array", () -> {

            String responseBody = getLatestResponse().getBody();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(responseBody);

            List<AggregateResult> results = mapper.readValue(jsonNode.toString(), new TypeReference<>() {
            });
            assertEquals(0, results.size());
        });


    }

    @When("^I submit a new measurement as follows:$")
    public void i_submit_a_new_measurement_as_follows(List<Map<String, String>> list) {
        try {
            for (Map<String, String> map : list) {
                Measurement measurement = buildMeasurement(map);
                post(measurement);
            }
        } catch (Exception e) {
            setStatusCode(400);
        }
    }

    @Then("^the response has a status code of (\\d+)$")
    public void the_response_has_a_status_code_of(int statusCode) {
        assertEquals(statusCode, getStatusCode());
    }

    @And("^the Location header has the path \"([^\"]*)\"$")
    public void the_location_header_has_the_path(String location) {
        assertEquals(location, getLocation());
    }


    @Given("^I have submitted new measurements as follows:$")
    public void i_have_submitted_new_measurements_as_follows(List<Map<String, String>> list) {
        i_submit_a_new_measurement_as_follows(list);
    }

    private Measurement buildMeasurement(Map<String, String> map) {
        Measurement.Builder builder = new Measurement.Builder();
        builder.withTimestamp(ZonedDateTime.parse(replace(map.get(TIMESTAMP))));
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (!entry.getKey().equals(TIMESTAMP) && !"".equals(entry.getValue())) {
                builder.withMetric(entry.getKey(), Double.valueOf(entry.getValue()));
            }
        }
        return builder.build();
    }

    private List<AggregateResult> buildExpectedResults(List<Map<String, String>> asMaps) {
        List<AggregateResult> aggregateResults = new ArrayList<>();
        for (Map<String, String> map : asMaps) {
            String metric = replace(map.get("metric"));
            double value = Double.parseDouble(map.get("value"));
            Statistic statistic = Statistic.valueOf(replace(map.get("stat")).toUpperCase());
            AggregateResult aggregateResult = new AggregateResult(metric, statistic, value);
            aggregateResults.add(aggregateResult);
        }
        return aggregateResults;
    }

    private String replace(String string) {
        return string.replaceAll("^\"|\"$", "");
    }

}
