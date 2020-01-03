package weathertracker;

import java.io.IOException;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

public class ResponseResultErrorHandler implements ResponseErrorHandler {
    private ResponseResult results = null;
    private Boolean hadError = false;

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        hadError = response.getRawStatusCode() >= 400;
        return hadError;
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        results = new ResponseResult(response);
    }

    public ResponseResult getResults() {
        return results;
    }

    public boolean getHadError() {
        return hadError;
    }

}

