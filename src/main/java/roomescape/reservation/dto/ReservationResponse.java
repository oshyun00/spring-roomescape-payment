package roomescape.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.member.dto.MemberResponse;
import roomescape.reservation.domain.Reservation;

public record ReservationResponse(
        Long id,
        MemberResponse member,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        ThemeResponse theme,
        TimeResponse time
) {

    public static ReservationResponse toResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                MemberResponse.toResponse(reservation.getMember()),
                reservation.getDate(),
                ThemeResponse.toResponse(reservation.getTheme()),
                TimeResponse.toResponse(reservation.getTime())
        );
    }
}