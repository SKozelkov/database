package ru.mospolytech.cards.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.mospolytech.cards.entity.Card;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    // Найти карту по номеру
    Optional<Card> findByCardNumber(String cardNumber);

    // Найти карту по заявке
    Optional<Card> findByRequestId(Long requestId);

    // Найти все карты по статусу
    List<Card> findByCardStatusId(Long statusId);

    // Найти все активные карты
    List<Card> findByIsActiveTrueOrderByCreatedAtDesc();

    // Найти все карты пользователя (через request -> user_organization -> user)
    @Query("SELECT c FROM Card c WHERE c.request.userOrganization.user.id = :userId ORDER BY c.createdAt DESC")
    List<Card> findByUserId(@Param("userId") Long userId);

    // Найти все карты организации (через request -> user_organization -> organization)
    @Query("SELECT c FROM Card c WHERE c.request.userOrganization.organization.id = :organizationId ORDER BY c.createdAt DESC")
    List<Card> findByOrganizationId(@Param("organizationId") Long organizationId);

    // Найти все карты по user_organization
    @Query("SELECT c FROM Card c WHERE c.request.userOrganization.id = :userOrganizationId ORDER BY c.createdAt DESC")
    List<Card> findByUserOrganizationId(@Param("userOrganizationId") Long userOrganizationId);

    // Найти активные карты пользователя
    @Query("SELECT c FROM Card c WHERE c.request.userOrganization.user.id = :userId AND c.isActive = true ORDER BY c.createdAt DESC")
    List<Card> findActiveByUserId(@Param("userId") Long userId);
}
