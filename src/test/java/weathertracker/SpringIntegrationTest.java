package weathertracker;

import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import weathertracker.measurements.Measurement;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WeatherTrackerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Ignore
public class SpringIntegrationTest {

    private final String SERVER_URL = "http://localhost";
    private final String MEASUREMENTS_ENDPOINT = "/measurements";
    private final String STATS_ENDPOINT = "/stats";

    private final RestTemplate restTemplate;

    private String locationHeader;
    private int statusCode;
    private ResponseResult latestResponse = null;

    @LocalServerPort
    protected int port = 8000;

    public SpringIntegrationTest() {
        this.restTemplate = new RestTemplate();
    }

    void getStats(String parameter) {
        try {
            getExceptionally(parameter, statsEndpoint());
        } catch (Exception e) {
            statusCode = 404;
        }
    }

    protected void getMeasurement(String parameter) {
        try {
            getExceptionally(parameter, measurementsEndpoint());
        } catch (Exception e) {
            statusCode = 404;
        }
    }

    private void getExceptionally(String parameter, String uri) {
        final Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        final HeaderSettingRequestCallback requestCallback = new HeaderSettingRequestCallback(headers);
        final ResponseResultErrorHandler errorHandler = new ResponseResultErrorHandler();

        String url = uri + "/" + parameter;
        restTemplate.setErrorHandler(errorHandler);

        latestResponse = restTemplate.execute(url, HttpMethod.GET, requestCallback, response -> {
            if (errorHandler.getHadError()) {
                statusCode = errorHandler.getResults().getTheResponse().getRawStatusCode();
                return (errorHandler.getResults());
            } else {
                latestResponse = new ResponseResult(response);
                statusCode = latestResponse.getTheResponse().getRawStatusCode();
                return (latestResponse);
            }
        });
    }

    protected void post(Measurement measurement) {
        final ResponseEntity<Void> responseEntity = restTemplate.postForEntity(measurementsEndpoint(), measurement, Void.class);
        HttpHeaders httpHeaders = responseEntity.getHeaders();
        locationHeader = httpHeaders.get("location").get(0);
        statusCode = responseEntity.getStatusCodeValue();
    }

    protected String getLocation() {
        return locationHeader;
    }

    protected int getStatusCode() {
        return statusCode;
    }

    protected void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    protected ResponseResult getLatestResponse() {
        return latestResponse;
    }

    private String measurementsEndpoint() {
        return SERVER_URL + ":" + port + MEASUREMENTS_ENDPOINT;
    }

    private String statsEndpoint() {
        return SERVER_URL + ":" + port + STATS_ENDPOINT;
    }
}
