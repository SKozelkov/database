package ru.mospolytech.cards.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "card_types")
@Getter
@Setter
public class CardType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_name", nullable = false, unique = true, length = 50)
    private String typeName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "annual_fee", nullable = false)
    private BigDecimal annualFee;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
}