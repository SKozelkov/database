package ru.mospolytech.cards.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mospolytech.cards.entity.RequestHistory;

import java.util.List;

@Repository
public interface RequestHistoryRepository extends JpaRepository<RequestHistory, Long> {
    List<RequestHistory> findByRequestIdOrderByChangedAtDesc(Long requestId);
    List<RequestHistory> findByChangedByIdOrderByChangedAtDesc(Long employeeId);
}
