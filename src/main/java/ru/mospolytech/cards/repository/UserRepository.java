package ru.mospolytech.cards.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.mospolytech.cards.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Найти пользователя по email
    Optional<User> findByEmail(String email);

    // Найти пользователя по паспортным данным
    Optional<User> findByPassportSeriesAndPassportNumber(String passportSeries, String passportNumber);

    // Найти пользователей по номеру телефона
    List<User> findByPhone(String phone);

    // Найти всех пользователей организации (через user_organizations)
    @Query("SELECT DISTINCT uo.user FROM UserOrganization uo WHERE uo.organization.id = :organizationId AND uo.isActive = true")
    List<User> findByOrganizationId(@Param("organizationId") Long organizationId);
}