package kg.cinema.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatResponse {
    private Long id;
    private Integer rowNumber;
    private Integer seatNumber;
    private String seatType;
    private Boolean isAvailable;
}
