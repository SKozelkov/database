package ru.mospolytech.cards.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.mospolytech.cards.entity.Employee;
import ru.mospolytech.cards.service.AuthService;
import java.util.Optional;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Страница входа
     */
    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        // Если пользователь уже вошел, перенаправляем на главную
        if (session.getAttribute("employee") != null) {
            return "redirect:/applications";
        }
        return "login";
    }

    /**
     * Обработка формы входа
     */
    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        Optional<Employee> employeeOpt = authService.authenticate(email, password);

        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            // Сохраняем сотрудника в сессии
            session.setAttribute("employee", employee);
            session.setAttribute("employeeId", employee.getId());
            session.setAttribute("isAdmin", employee.isAdmin());

            return "redirect:/applications";
        } else {
            model.addAttribute("error", "Неверный email или пароль");
            return "login";
        }
    }

    /**
     * Выход из системы
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    /**
     * Главная страница - редирект
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/applications";
    }
}