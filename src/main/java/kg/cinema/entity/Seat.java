package kg.cinema.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "seats", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"hall_id", "row_number", "seat_number"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;

    @Column(name = "row_number", nullable = false)
    private Integer rowNumber;

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @Column(name = "seat_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SeatType seatType;

    @Column(name = "pos_x")
    private Float posX;

    @Column(name = "pos_y")
    private Float posY;

    @Column(name = "is_active")
    private Boolean isActive = true;

    public enum SeatType {
        STANDARD, VIP, COUCH
    }
}
