package roomescape.payment.client;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import roomescape.exception.ParsingFailException;
import roomescape.exception.PaymentFailException;
import roomescape.exception.TossPayErrorHandler;
import roomescape.payment.dto.PaymentRequest;

class TossPayRestClientTest {

    private final RestClient.Builder testBuilder = RestClient.builder()
            .baseUrl("https://api.tosspayments.com")
            .defaultStatusHandler(new TossPayErrorHandler());

    private MockRestServiceServer server = MockRestServiceServer.bindTo(testBuilder).build();

    private TossPayRestClient tossPayRestClient = new TossPayRestClient(testBuilder.build());

    @BeforeEach
    void setUp() {
        server.reset();
    }

    @DisplayName("결제가 정상적으로 처리되면 예외가 발생하지 않는다.")
    @Test
    void pay() {
        server.expect(requestTo("https://api.tosspayments.com/v1/payments/confirm"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        PaymentRequest request = new PaymentRequest("orderId", 1000, "paymentKey");

        assertDoesNotThrow(() -> tossPayRestClient.pay(request));
    }

    @DisplayName("결제에 실패하면 예외가 발생한다.")
    @Test
    void throwException() {
        String expectedBody = """
                {
                  "code": "NOT_FOUND_PAYMENT",
                  "message": "존재하지 않는 결제 입니다."
                }
                """;

        server.expect(requestTo("https://api.tosspayments.com/v1/payments/confirm"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(
                        withStatus(HttpStatus.BAD_REQUEST).body(expectedBody).contentType(MediaType.APPLICATION_JSON));

        PaymentRequest request = new PaymentRequest("orderId", 1000, "paymentKey");

        assertThatCode(() -> tossPayRestClient.pay(request))
                .isInstanceOf(PaymentFailException.class)
                .hasMessage("존재하지 않는 결제 입니다.");
    }

    @DisplayName("올바르지 않은 형태의 예외데이터가 응답되면 Parsing 예외가 발생한다.")
    @Test
    void throwParsingException() {
        String expectedBody = """
                {
                  "error": "NOT_FOUND_PAYMENT",
                  "message": "존재하지 않는 결제 입니다."
                }
                """;

        server.expect(requestTo("https://api.tosspayments.com/v1/payments/confirm"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(
                        withStatus(HttpStatus.BAD_REQUEST).body(expectedBody).contentType(MediaType.APPLICATION_JSON));

        PaymentRequest request = new PaymentRequest("orderId", 1000, "paymentKey");

        assertThatCode(() -> tossPayRestClient.pay(request))
                .isInstanceOf(ParsingFailException.class);
    }
}