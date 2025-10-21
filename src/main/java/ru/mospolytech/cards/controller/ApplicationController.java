package ru.mospolytech.cards.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.mospolytech.cards.entity.*;
import ru.mospolytech.cards.repository.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/applications")
public class ApplicationController {

    private final CardApplicationRepository applicationRepository;
    private final OrganizationRepository organizationRepository;
    private final CardTypeRepository cardTypeRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final ApplicationStatusRepository statusRepository;

    public ApplicationController(
            CardApplicationRepository applicationRepository,
            OrganizationRepository organizationRepository,
            CardTypeRepository cardTypeRepository,
            BranchRepository branchRepository,
            UserRepository userRepository,
            ApplicationStatusRepository statusRepository) {
        this.applicationRepository = applicationRepository;
        this.organizationRepository = organizationRepository;
        this.cardTypeRepository = cardTypeRepository;
        this.branchRepository = branchRepository;
        this.userRepository = userRepository;
        this.statusRepository = statusRepository;
    }

    /**
     * Главная страница - список заявок
     */
    @GetMapping
    public String listApplications(HttpSession session, Model model) {
        // Проверяем авторизацию
        Employee employee = (Employee) session.getAttribute("employee");
        if (employee == null) {
            return "redirect:/login";
        }

        List<CardApplication> applications;

        // Если сотрудник - администратор, показываем все заявки
        // Если менеджер - только свои заявки
        if (employee.isAdmin()) {
            applications = applicationRepository.findAll(
                Sort.by(Sort.Direction.DESC, "createdAt")
            );
            model.addAttribute("filterInfo", "Все заявки");
        } else {
            // Менеджер видит только свои заявки
            applications = applicationRepository.findByAssignedEmployeeIdOrderByCreatedAtDesc(employee.getId());
            model.addAttribute("filterInfo", "Мои заявки");
        }

        model.addAttribute("employee", employee);
        model.addAttribute("applications", applications);
        model.addAttribute("isAdmin", employee.isAdmin());

        return "applications";
    }

    /**
     * Форма создания новой заявки
     */
    @GetMapping("/new")
    public String newApplicationForm(HttpSession session, Model model) {
        // Проверяем авторизацию
        Employee employee = (Employee) session.getAttribute("employee");
        if (employee == null) {
            return "redirect:/login";
        }

        // Загружаем справочники
        model.addAttribute("organizations", organizationRepository.findByIsActiveTrue());
        model.addAttribute("cardTypes", cardTypeRepository.findAll());
        model.addAttribute("branches", branchRepository.findByIsActiveTrue());
        model.addAttribute("employee", employee);

        return "application-form";
    }

    /**
     * Создание новой заявки
     */
    @PostMapping("/create")
    public String createApplication(
            @RequestParam Long userId,
            @RequestParam Long organizationId,
            @RequestParam Long cardTypeId,
            @RequestParam Long branchId,
            @RequestParam(required = false) String comments,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Проверяем авторизацию
        Employee employee = (Employee) session.getAttribute("employee");
        if (employee == null) {
            return "redirect:/login";
        }

        try {
            // Создаем новую заявку
            CardApplication application = new CardApplication();

            // Устанавливаем связи
            application.setUser(userRepository.findById(userId).orElseThrow());
            application.setOrganization(organizationRepository.findById(organizationId).orElseThrow());
            application.setCardType(cardTypeRepository.findById(cardTypeId).orElseThrow());
            application.setBranch(branchRepository.findById(branchId).orElseThrow());

            // Устанавливаем статус "Новая"
            ApplicationStatus newStatus = statusRepository.findByStatusName("Новая")
                    .orElseThrow(() -> new RuntimeException("Статус 'Новая' не найден в базе данных"));
            application.setStatus(newStatus);

            // Автоматически назначаем заявку на текущего сотрудника, если он менеджер
            if (!employee.isAdmin()) {
                application.setAssignedEmployee(employee);
            }

            // Генерируем номер заявки
            String applicationNumber = generateApplicationNumber();
            application.setApplicationNumber(applicationNumber);

            // Устанавливаем комментарии
            application.setComments(comments);

            // Устанавливаем даты
            LocalDateTime now = LocalDateTime.now();
            application.setCreatedAt(now);
            application.setUpdatedAt(now);

            // Сохраняем заявку
            applicationRepository.save(application);

            redirectAttributes.addFlashAttribute("success",
                "Заявка " + applicationNumber + " успешно создана!");

            return "redirect:/applications";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                "Ошибка при создании заявки: " + e.getMessage());
            return "redirect:/applications/new";
        }
    }

    /**
     * Генерация номера заявки
     */
    private String generateApplicationNumber() {
        LocalDateTime now = LocalDateTime.now();
        String datePrefix = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = applicationRepository.count() + 1;
        return String.format("APP-%s-%04d", datePrefix, count);
    }
}
