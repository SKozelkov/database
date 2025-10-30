package ru.mospolytech.cards.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.mospolytech.cards.entity.UserOrganization;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserOrganizationRepository extends JpaRepository<UserOrganization, Long> {

    // Найти все связи пользователя
    List<UserOrganization> findByUserId(Long userId);

    // Найти все активные связи пользователя
    List<UserOrganization> findByUserIdAndIsActiveTrue(Long userId);

    // Найти всех сотрудников организации
    List<UserOrganization> findByOrganizationId(Long organizationId);

    // Найти всех активных сотрудников организации
    List<UserOrganization> findByOrganizationIdAndIsActiveTrue(Long organizationId);

    // Найти конкретную связь пользователя с организацией
    Optional<UserOrganization> findByUserIdAndOrganizationId(Long userId, Long organizationId);

    // Найти активную связь пользователя с организацией
    Optional<UserOrganization> findByUserIdAndOrganizationIdAndIsActiveTrue(Long userId, Long organizationId);

    // Проверить, работает ли пользователь в организации
    boolean existsByUserIdAndOrganizationIdAndIsActiveTrue(Long userId, Long organizationId);

    // Найти все связи с определенной должностью
    List<UserOrganization> findByPosition(String position);

    // Найти сотрудников организации с определенной должностью
    List<UserOrganization> findByOrganizationIdAndPosition(Long organizationId, String position);

    // Найти все текущие активные связи (учитывая даты)
    @Query("SELECT uo FROM UserOrganization uo WHERE uo.isActive = true " +
           "AND uo.dateFrom <= CURRENT_DATE " +
           "AND (uo.dateTo IS NULL OR uo.dateTo >= CURRENT_DATE)")
    List<UserOrganization> findAllCurrentlyActive();

    // Найти текущие активные связи пользователя
    @Query("SELECT uo FROM UserOrganization uo WHERE uo.user.id = :userId " +
           "AND uo.isActive = true " +
           "AND uo.dateFrom <= CURRENT_DATE " +
           "AND (uo.dateTo IS NULL OR uo.dateTo >= CURRENT_DATE)")
    List<UserOrganization> findCurrentlyActiveByUserId(@Param("userId") Long userId);

    // Найти текущие активные связи организации
    @Query("SELECT uo FROM UserOrganization uo WHERE uo.organization.id = :organizationId " +
           "AND uo.isActive = true " +
           "AND uo.dateFrom <= CURRENT_DATE " +
           "AND (uo.dateTo IS NULL OR uo.dateTo >= CURRENT_DATE)")
    List<UserOrganization> findCurrentlyActiveByOrganizationId(@Param("organizationId") Long organizationId);
}
