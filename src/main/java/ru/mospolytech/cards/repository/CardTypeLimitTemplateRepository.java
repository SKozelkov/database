package ru.mospolytech.cards.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mospolytech.cards.entity.CardTypeLimitTemplate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardTypeLimitTemplateRepository extends JpaRepository<CardTypeLimitTemplate, Long> {

    // Найти все шаблоны для типа карты
    List<CardTypeLimitTemplate> findByCardTypeId(Long cardTypeId);

    // Найти шаблон по умолчанию для типа карты
    Optional<CardTypeLimitTemplate> findByCardTypeIdAndIsDefaultTrue(Long cardTypeId);

    // Найти все активные шаблоны по умолчанию
    List<CardTypeLimitTemplate> findByIsDefaultTrue();
}
