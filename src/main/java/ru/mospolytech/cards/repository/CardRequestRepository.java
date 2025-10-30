package ru.mospolytech.cards.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.mospolytech.cards.entity.*;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRequestRepository extends JpaRepository<CardRequest, Long>, JpaSpecificationExecutor<CardRequest> {
    List<CardRequest> findAllByOrderByCreatedAtDesc();
    List<CardRequest> findByAssignedEmployeeIdOrderByCreatedAtDesc(Long employeeId);
    Optional<CardRequest> findByRequestNumber(String requestNumber);
    List<CardRequest> findByUserOrganizationIdOrderByCreatedAtDesc(Long userOrganizationId);
    List<CardRequest> findByStatusIdOrderByCreatedAtDesc(Long statusId);
}
