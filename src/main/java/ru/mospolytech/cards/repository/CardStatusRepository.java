package ru.mospolytech.cards.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mospolytech.cards.entity.CardStatus;

import java.util.Optional;

@Repository
public interface CardStatusRepository extends JpaRepository<CardStatus, Long> {
    Optional<CardStatus> findByStatusName(String statusName);
}
