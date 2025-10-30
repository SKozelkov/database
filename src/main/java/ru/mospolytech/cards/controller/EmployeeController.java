package ru.mospolytech.cards.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.mospolytech.cards.entity.*;
import ru.mospolytech.cards.repository.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/employee")
public class EmployeeController {

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;

    public EmployeeController(
            EmployeeRepository employeeRepository,
            RoleRepository roleRepository,
            BranchRepository branchRepository) {
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.branchRepository = branchRepository;
    }

    /**
     * Страница профиля текущего сотрудника
     */
    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        // Проверяем авторизацию
        Employee employee = (Employee) session.getAttribute("employee");
        if (employee == null) {
            return "redirect:/login";
        }

        // Обновляем данные сотрудника из БД
        employee = employeeRepository.findById(employee.getId()).orElse(employee);

        model.addAttribute("employee", employee);
        model.addAttribute("isAdmin", employee.isAdmin());

        // Если администратор - показываем список менеджеров
        if (employee.isAdmin()) {
            // Получаем все роли
            List<Role> allRoles = roleRepository.findAll();

            // Находим роль администратора
            Role adminRole = roleRepository.findByRoleName("Администратор").orElse(null);

            // Получаем всех сотрудников, которые не являются администраторами
            List<Employee> managers = employeeRepository.findAll().stream()
                    .filter(emp -> !emp.isAdmin())
                    .sorted((e1, e2) -> {
                        int roleCompare = e1.getRole().getRoleName().compareTo(e2.getRole().getRoleName());
                        if (roleCompare != 0) return roleCompare;
                        return e1.getLastName().compareTo(e2.getLastName());
                    })
                    .toList();

            model.addAttribute("managers", managers);
            model.addAttribute("allRoles", allRoles.stream()
                    .filter(role -> !role.getRoleName().equals("Администратор"))
                    .toList());

            // Добавляем справочники для создания нового менеджера
            model.addAttribute("branches", branchRepository.findByIsActiveTrue());
        }

        return "employee/profile";
    }

    /**
     * Создание нового менеджера (только для администратора)
     */
    @PostMapping("/create-manager")
    public String createManager(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam(required = false) String middleName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String password,
            @RequestParam Long roleId,
            @RequestParam(required = false) Long branchId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Проверяем авторизацию
        Employee employee = (Employee) session.getAttribute("employee");
        if (employee == null) {
            return "redirect:/login";
        }

        // Проверяем права администратора
        if (!employee.isAdmin()) {
            redirectAttributes.addFlashAttribute("error", "У вас нет прав для выполнения этой операции");
            return "redirect:/employee/profile";
        }

        try {
            // Проверяем, что email не занят
            if (employeeRepository.findByEmail(email).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Сотрудник с таким email уже существует");
                return "redirect:/employee/profile";
            }

            // Находим роль
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new RuntimeException("Роль не найдена в базе данных"));

            // Проверяем, что не пытаются создать администратора
            if (role.getRoleName().equals("Администратор")) {
                redirectAttributes.addFlashAttribute("error", "Нельзя создать администратора через эту форму");
                return "redirect:/employee/profile";
            }

            // Создаем нового сотрудника
            Employee newEmployee = new Employee();
            newEmployee.setFirstName(firstName);
            newEmployee.setLastName(lastName);
            newEmployee.setMiddleName(middleName);
            newEmployee.setEmail(email);
            newEmployee.setPhone(phone);

            // Хешируем пароль (упрощенный вариант - в production использовать BCrypt)
            newEmployee.setPasswordHash(password); // TODO: Implement proper password hashing

            newEmployee.setRole(role);
            newEmployee.setIsActive(true);
            newEmployee.setCreatedAt(LocalDateTime.now());

            // Устанавливаем отделение, если выбрано
            if (branchId != null) {
                Branch branch = branchRepository.findById(branchId).orElse(null);
                newEmployee.setBranch(branch);
            }

            employeeRepository.save(newEmployee);

            redirectAttributes.addFlashAttribute("success",
                    "Сотрудник " + newEmployee.getFullName() + " (" + role.getRoleName() + ") успешно добавлен!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Ошибка при создании сотрудника: " + e.getMessage());
        }

        return "redirect:/employee/profile";
    }

    /**
     * Деактивация менеджера (только для администратора)
     */
    @PostMapping("/deactivate-manager/{id}")
    public String deactivateManager(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Проверяем авторизацию
        Employee employee = (Employee) session.getAttribute("employee");
        if (employee == null) {
            return "redirect:/login";
        }

        // Проверяем права администратора
        if (!employee.isAdmin()) {
            redirectAttributes.addFlashAttribute("error", "У вас нет прав для выполнения этой операции");
            return "redirect:/employee/profile";
        }

        try {
            Employee manager = employeeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Менеджер не найден"));

            // Проверяем, что это действительно менеджер
            if (!manager.isManager()) {
                redirectAttributes.addFlashAttribute("error", "Этот сотрудник не является менеджером");
                return "redirect:/employee/profile";
            }

            // Деактивируем менеджера
            manager.setIsActive(false);
            employeeRepository.save(manager);

            redirectAttributes.addFlashAttribute("success",
                    "Менеджер " + manager.getFullName() + " деактивирован");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Ошибка при деактивации менеджера: " + e.getMessage());
        }

        return "redirect:/employee/profile";
    }

    /**
     * Активация менеджера (только для администратора)
     */
    @PostMapping("/activate-manager/{id}")
    public String activateManager(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Проверяем авторизацию
        Employee employee = (Employee) session.getAttribute("employee");
        if (employee == null) {
            return "redirect:/login";
        }

        // Проверяем права администратора
        if (!employee.isAdmin()) {
            redirectAttributes.addFlashAttribute("error", "У вас нет прав для выполнения этой операции");
            return "redirect:/employee/profile";
        }

        try {
            Employee manager = employeeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Менеджер не найден"));

            // Активируем менеджера
            manager.setIsActive(true);
            employeeRepository.save(manager);

            redirectAttributes.addFlashAttribute("success",
                    "Менеджер " + manager.getFullName() + " активирован");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Ошибка при активации менеджера: " + e.getMessage());
        }

        return "redirect:/employee/profile";
    }
}
