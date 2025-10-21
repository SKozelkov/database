package ru.mospolytech.cards.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import ru.mospolytech.cards.entity.Employee;
import ru.mospolytech.cards.repository.EmployeeRepository;
import java.util.Optional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final EmployeeRepository employeeRepository;

    public AuthService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    /**
     * Аутентификация сотрудника по email и паролю
     */
    public Optional<Employee> authenticate(String email, String password) {
        logger.info("Попытка входа с email: {}", email);
        logger.debug("Длина пароля: {}", password != null ? password.length() : "null");

        Optional<Employee> employeeOpt = employeeRepository.findByEmailAndIsActive(email, true);

        if (employeeOpt.isEmpty()) {
            logger.warn("Сотрудник с email {} не найден или не активен", email);
            return Optional.empty();
        }

        Employee employee = employeeOpt.get();
        logger.debug("Найден сотрудник: {} (ID: {})", employee.getFullName(), employee.getId());

        try {
            // Проверка пароля с использованием BCrypt
            boolean passwordMatches = BCrypt.checkpw(password, employee.getPasswordHash());
            logger.debug("Результат проверки пароля для {}: {}", email, passwordMatches);

            if (passwordMatches) {
                logger.info("Успешный вход пользователя: {}", email);
                return Optional.of(employee);
            } else {
                logger.warn("Неверный пароль для пользователя: {}", email);
            }
        } catch (Exception e) {
            logger.error("Ошибка при проверке пароля для пользователя: {}", email, e);
        }

        return Optional.empty();
    }

    /**
     * Получить сотрудника по ID
     */
    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }
}
