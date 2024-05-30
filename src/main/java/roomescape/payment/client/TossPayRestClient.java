package roomescape.payment.client;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;
import roomescape.exception.JsonParseException;
import roomescape.exception.PaymentFailException;
import roomescape.payment.dto.PaymentRequest;

public class TossPayRestClient {

    private static final String KEY_PREFIX = "Basic ";

    private final String paymentSecretKey;
    private final RestClient restClient;

    public TossPayRestClient(String paymentSecretKey, RestClient restClient) {
        this.paymentSecretKey = paymentSecretKey;
        this.restClient = restClient;
    }

    public void pay(PaymentRequest paymentRequest) {
        restClient.post()
                .uri("/v1/payments/confirm")
                .header("Authorization", createAuthorizations())
                .contentType(MediaType.APPLICATION_JSON)
                .body(paymentRequest)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> handleClientErrorResponse(response))
                .toBodilessEntity();
    }

    private String createAuthorizations() {
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encodedBytes = encoder.encode((paymentSecretKey + ":").getBytes(StandardCharsets.UTF_8));
        return KEY_PREFIX + new String(encodedBytes);
    }

    private void handleClientErrorResponse(ClientHttpResponse response) throws IOException {
        JSONParser parser = new JSONParser();
        Reader reader = new InputStreamReader(response.getBody(), StandardCharsets.UTF_8);
        try {
            JSONObject jsonObject = (JSONObject) parser.parse(reader);
            throw new PaymentFailException((String) jsonObject.get("code"), (String) jsonObject.get("message"));
        } catch (ParseException exception) {
            throw new JsonParseException(exception.getMessage());
        }
    }
}
