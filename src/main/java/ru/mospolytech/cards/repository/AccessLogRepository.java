package ru.mospolytech.cards.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.mospolytech.cards.entity.AccessLog;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {

    // Найти все логи сотрудника
    List<AccessLog> findByEmployeeIdOrderByLoginTimeDesc(Long employeeId);

    // Найти успешные входы сотрудника
    List<AccessLog> findByEmployeeIdAndIsSuccessfulTrueOrderByLoginTimeDesc(Long employeeId);

    // Найти неудачные попытки входа
    List<AccessLog> findByIsSuccessfulFalseOrderByLoginTimeDesc();

    // Найти логи за определенный период
    @Query("SELECT a FROM AccessLog a WHERE a.loginTime BETWEEN :startDate AND :endDate ORDER BY a.loginTime DESC")
    List<AccessLog> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Найти последние N логов сотрудника
    List<AccessLog> findTop10ByEmployeeIdOrderByLoginTimeDesc(Long employeeId);
}
