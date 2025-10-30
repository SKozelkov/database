package ru.mospolytech.cards.specification;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import ru.mospolytech.cards.entity.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CardRequestSpecification {

    /**
     * Создает спецификацию для фильтрации заявок с учетом всех параметров
     */
    public static Specification<CardRequest> filterBy(
            Long userId,
            Long organizationId,
            Long cardTypeId,
            Long assignedEmployeeId,
            Long statusId,
            LocalDate dateFrom,
            LocalDate dateTo) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Фильтр по пользователю (клиенту)
            if (userId != null) {
                Join<CardRequest, UserOrganization> userOrgJoin = root.join("userOrganization");
                predicates.add(criteriaBuilder.equal(userOrgJoin.get("user").get("id"), userId));
            }

            // Фильтр по организации
            if (organizationId != null) {
                Join<CardRequest, UserOrganization> userOrgJoin = root.join("userOrganization");
                predicates.add(criteriaBuilder.equal(userOrgJoin.get("organization").get("id"), organizationId));
            }

            // Фильтр по типу карты
            if (cardTypeId != null) {
                predicates.add(criteriaBuilder.equal(root.get("cardType").get("id"), cardTypeId));
            }

            // Фильтр по ответственному сотруднику
            if (assignedEmployeeId != null) {
                predicates.add(criteriaBuilder.equal(root.get("assignedEmployee").get("id"), assignedEmployeeId));
            }

            // Фильтр по статусу
            if (statusId != null) {
                predicates.add(criteriaBuilder.equal(root.get("status").get("id"), statusId));
            }

            // Фильтр по дате создания (от)
            if (dateFrom != null) {
                LocalDateTime dateTimeFrom = dateFrom.atStartOfDay();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), dateTimeFrom));
            }

            // Фильтр по дате создания (до)
            if (dateTo != null) {
                LocalDateTime dateTimeTo = dateTo.plusDays(1).atStartOfDay();
                predicates.add(criteriaBuilder.lessThan(root.get("createdAt"), dateTimeTo));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Фильтр для менеджера - показывает только свои заявки
     */
    public static Specification<CardRequest> byAssignedEmployee(Long employeeId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("assignedEmployee").get("id"), employeeId);
    }

    /**
     * Добавляет сортировку к спецификации
     */
    public static Specification<CardRequest> withSort(String sortBy, String sortDirection) {
        return (root, query, criteriaBuilder) -> {
            if (query == null) {
                return null;
            }

            // Определяем направление сортировки
            boolean isAsc = "asc".equalsIgnoreCase(sortDirection);

            // Применяем сортировку в зависимости от поля
            Order order;
            switch (sortBy) {
                case "client":
                    Join<CardRequest, UserOrganization> userOrgJoin = root.join("userOrganization");
                    Join<UserOrganization, User> userJoin = userOrgJoin.join("user");
                    order = isAsc ? criteriaBuilder.asc(userJoin.get("lastName"))
                                  : criteriaBuilder.desc(userJoin.get("lastName"));
                    break;
                case "organization":
                    Join<CardRequest, UserOrganization> userOrgJoin2 = root.join("userOrganization");
                    Join<UserOrganization, Organization> orgJoin = userOrgJoin2.join("organization");
                    order = isAsc ? criteriaBuilder.asc(orgJoin.get("name"))
                                  : criteriaBuilder.desc(orgJoin.get("name"));
                    break;
                case "createdAt":
                    order = isAsc ? criteriaBuilder.asc(root.get("createdAt"))
                                  : criteriaBuilder.desc(root.get("createdAt"));
                    break;
                default:
                    // По умолчанию сортировка по дате создания (DESC)
                    order = criteriaBuilder.desc(root.get("createdAt"));
            }

            query.orderBy(order);
            return null;
        };
    }
}
