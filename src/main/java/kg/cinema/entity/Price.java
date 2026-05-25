package kg.cinema.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "prices", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"showtime_id", "seat_type"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Price {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;

    @Column(name = "seat_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Seat.SeatType seatType;

    @Column(name = "price_modifier", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceModifier = BigDecimal.ZERO;
}
