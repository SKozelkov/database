package ru.mospolytech.cards.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mospolytech.cards.entity.*;

@Repository
public interface CardTypeRepository extends JpaRepository<CardType, Long> {
}