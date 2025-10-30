package ru.mospolytech.cards.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mospolytech.cards.entity.CardLimitHistory;
import java.util.List;

@Repository
public interface CardLimitHistoryRepository extends JpaRepository<CardLimitHistory, Long> {

    // Найти всю историю изменений для карты
    List<CardLimitHistory> findByCardIdOrderByChangedAtDesc(Long cardId);

    // Найти всю историю изменений для лимита карты
    List<CardLimitHistory> findByCardLimitIdOrderByChangedAtDesc(Long cardLimitId);

    // Найти изменения, сделанные конкретным сотрудником
    List<CardLimitHistory> findByChangedByIdOrderByChangedAtDesc(Long employeeId);
}
