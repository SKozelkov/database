package ru.mospolytech.cards.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mospolytech.cards.entity.CardHistory;

import java.util.List;

@Repository
public interface CardHistoryRepository extends JpaRepository<CardHistory, Long> {

    // Найти всю историю по ID карты
    List<CardHistory> findByCardIdOrderByChangedAtDesc(Long cardId);

    // Найти историю изменений сотрудника
    List<CardHistory> findByChangedByIdOrderByChangedAtDesc(Long employeeId);
}
