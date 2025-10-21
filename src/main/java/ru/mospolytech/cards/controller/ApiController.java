package ru.mospolytech.cards.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mospolytech.cards.entity.User;
import ru.mospolytech.cards.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final UserRepository userRepository;

    public ApiController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Получить список пользователей организации
     */
    @GetMapping("/organizations/{organizationId}/users")
    public List<Map<String, Object>> getUsersByOrganization(@PathVariable Long organizationId) {
        List<User> users = userRepository.findByOrganizationId(organizationId);

        return users.stream().map(user -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("fullName", user.getFullName());
            userMap.put("position", user.getPosition());
            userMap.put("email", user.getEmail());
            userMap.put("phone", user.getPhone());
            return userMap;
        }).collect(Collectors.toList());
    }
}
