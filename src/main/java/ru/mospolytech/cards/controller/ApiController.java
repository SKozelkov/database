package ru.mospolytech.cards.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mospolytech.cards.entity.UserOrganization;
import ru.mospolytech.cards.repository.UserOrganizationRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final UserOrganizationRepository userOrganizationRepository;

    public ApiController(UserOrganizationRepository userOrganizationRepository) {
        this.userOrganizationRepository = userOrganizationRepository;
    }

    /**
     * Получить список пользователей организации
     */
    @GetMapping("/organizations/{organizationId}/users")
    public List<Map<String, Object>> getUsersByOrganization(@PathVariable Long organizationId) {
        List<UserOrganization> userOrganizations = userOrganizationRepository
                .findByOrganizationIdAndIsActiveTrue(organizationId);

        return userOrganizations.stream().map(userOrg -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", userOrg.getId());
            userMap.put("userId", userOrg.getUser().getId());
            userMap.put("fullName", userOrg.getUser().getFullName());
            userMap.put("position", userOrg.getPosition());
            userMap.put("email", userOrg.getUser().getEmail());
            userMap.put("phone", userOrg.getUser().getPhone());
            return userMap;
        }).collect(Collectors.toList());
    }
}
