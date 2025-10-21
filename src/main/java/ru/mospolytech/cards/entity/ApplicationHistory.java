package ru.mospolytech.cards.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "application_history")
@Getter
@Setter
public class ApplicationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private CardApplication application;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "old_status_id")
    private ApplicationStatus oldStatus;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "new_status_id", nullable = false)
    private ApplicationStatus newStatus;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "changed_by", nullable = false)
    private Employee changedBy;

    @Column(name = "change_comment", columnDefinition = "TEXT")
    private String changeComment;

    @Column(name = "changed_at")
    private LocalDateTime changedAt;
}
