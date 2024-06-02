package roomescape.payment.dto;

import roomescape.reservation.dto.ReservationSaveRequest;

public record PaymentRequest(String orderId, int amount, String paymentKey) {

    public static PaymentRequest from(ReservationSaveRequest reservationSaveRequest) {
        return new PaymentRequest(
                reservationSaveRequest.getOrderId(),
                reservationSaveRequest.getAmount(),
                reservationSaveRequest.getPaymentKey()
        );
    }
}
