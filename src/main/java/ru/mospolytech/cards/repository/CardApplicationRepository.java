package ru.mospolytech.cards.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mospolytech.cards.entity.*;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardApplicationRepository extends JpaRepository<CardApplication, Long> {
    List<CardApplication> findAllByOrderByCreatedAtDesc();
    List<CardApplication> findByAssignedEmployeeIdOrderByCreatedAtDesc(Long employeeId);
    Optional<CardApplication> findByApplicationNumber(String applicationNumber);
}