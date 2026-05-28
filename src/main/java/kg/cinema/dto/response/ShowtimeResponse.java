package kg.cinema.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeResponse {
    private Long id;
    private Long movieId;
    private String movieTitle;
    private HallInfo hall;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String language;
    private String subtitles;
    private BigDecimal basePrice;
    private Boolean isActive;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HallInfo {
        private Long id;
        private String name;
        private String type;
        private CinemaInfo cinema;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CinemaInfo {
        private Long id;
        private String name;
        private String address;
        private String city;
    }
}
