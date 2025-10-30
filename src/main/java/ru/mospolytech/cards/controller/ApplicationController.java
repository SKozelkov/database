package ru.mospolytech.cards.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.mospolytech.cards.entity.*;
import ru.mospolytech.cards.repository.*;
import ru.mospolytech.cards.specification.CardRequestSpecification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/applications")
public class ApplicationController {

    private final CardRequestRepository requestRepository;
    private final OrganizationRepository organizationRepository;
    private final CardTypeRepository cardTypeRepository;
    private final BranchRepository branchRepository;
    private final UserOrganizationRepository userOrganizationRepository;
    private final RequestStatusRepository statusRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    public ApplicationController(
            CardRequestRepository requestRepository,
            OrganizationRepository organizationRepository,
            CardTypeRepository cardTypeRepository,
            BranchRepository branchRepository,
            UserOrganizationRepository userOrganizationRepository,
            RequestStatusRepository statusRepository,
            UserRepository userRepository,
            EmployeeRepository employeeRepository) {
        this.requestRepository = requestRepository;
        this.organizationRepository = organizationRepository;
        this.cardTypeRepository = cardTypeRepository;
        this.branchRepository = branchRepository;
        this.userOrganizationRepository = userOrganizationRepository;
        this.statusRepository = statusRepository;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Главная страница - список заявок с фильтрацией и сортировкой
     */
    @GetMapping
    public String listApplications(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long organizationId,
            @RequestParam(required = false) Long cardTypeId,
            @RequestParam(required = false) Long assignedEmployeeId,
            @RequestParam(required = false) Long statusId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            HttpSession session,
            Model model) {

        // Проверяем авторизацию
        Employee employee = (Employee) session.getAttribute("employee");
        if (employee == null) {
            return "redirect:/login";
        }

        // Создаем спецификацию для фильтрации
        Specification<CardRequest> spec = CardRequestSpecification.filterBy(
                userId, organizationId, cardTypeId, assignedEmployeeId, statusId, dateFrom, dateTo
        );

        // Если сотрудник - менеджер (не администратор), добавляем фильтр по своим заявкам
        if (!employee.isAdmin()) {
            spec = spec.and(CardRequestSpecification.byAssignedEmployee(employee.getId()));
        }

        // Добавляем сортировку
        spec = spec.and(CardRequestSpecification.withSort(sortBy, sortDirection));

        // Выполняем запрос с фильтрацией и сортировкой
        List<CardRequest> requests = requestRepository.findAll(spec);

        // Формируем информацию о фильтрах
        StringBuilder filterInfo = new StringBuilder();
        if (!employee.isAdmin()) {
            filterInfo.append("Мои заявки");
        } else {
            filterInfo.append("Все заявки");
        }

        int activeFilters = 0;
        if (userId != null) activeFilters++;
        if (organizationId != null) activeFilters++;
        if (cardTypeId != null) activeFilters++;
        if (assignedEmployeeId != null) activeFilters++;
        if (statusId != null) activeFilters++;
        if (dateFrom != null || dateTo != null) activeFilters++;

        if (activeFilters > 0) {
            filterInfo.append(" (активных фильтров: ").append(activeFilters).append(")");
        }

        // Добавляем данные в модель
        model.addAttribute("employee", employee);
        model.addAttribute("applications", requests);
        model.addAttribute("isAdmin", employee.isAdmin());
        model.addAttribute("filterInfo", filterInfo.toString());

        // Добавляем справочники для фильтров
        model.addAttribute("allUsers", userRepository.findAll(Sort.by("lastName").and(Sort.by("firstName"))));
        model.addAttribute("allOrganizations", organizationRepository.findAll(Sort.by("name")));
        model.addAttribute("allCardTypes", cardTypeRepository.findAll(Sort.by("typeName")));
        model.addAttribute("allEmployees", employeeRepository.findAll(Sort.by("lastName").and(Sort.by("firstName"))));
        model.addAttribute("allStatuses", statusRepository.findAll(Sort.by("id")));

        // Сохраняем текущие значения фильтров для отображения в форме
        model.addAttribute("filterUserId", userId);
        model.addAttribute("filterOrganizationId", organizationId);
        model.addAttribute("filterCardTypeId", cardTypeId);
        model.addAttribute("filterAssignedEmployeeId", assignedEmployeeId);
        model.addAttribute("filterStatusId", statusId);
        model.addAttribute("filterDateFrom", dateFrom);
        model.addAttribute("filterDateTo", dateTo);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDirection", sortDirection);

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
            @RequestParam Long userOrganizationId,
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
            CardRequest request = new CardRequest();

            // Устанавливаем связи
            request.setUserOrganization(userOrganizationRepository.findById(userOrganizationId).orElseThrow());
            request.setCardType(cardTypeRepository.findById(cardTypeId).orElseThrow());
            request.setBranch(branchRepository.findById(branchId).orElseThrow());

            // Устанавливаем статус "Новая"
            RequestStatus newStatus = statusRepository.findByStatusName("Новая")
                    .orElseThrow(() -> new RuntimeException("Статус 'Новая' не найден в базе данных"));
            request.setStatus(newStatus);

            // Автоматически назначаем заявку на текущего сотрудника
            request.setAssignedEmployee(employee);

            // Генерируем номер заявки
            String requestNumber = generateApplicationNumber();
            request.setRequestNumber(requestNumber);

            // Устанавливаем комментарии
            request.setComments(comments);

            // Устанавливаем даты
            LocalDateTime now = LocalDateTime.now();
            request.setCreatedAt(now);
            request.setUpdatedAt(now);

            // Сохраняем заявку
            requestRepository.save(request);

            redirectAttributes.addFlashAttribute("success",
                "Заявка " + requestNumber + " успешно создана!");

            return "redirect:/applications";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                "Ошибка при создании заявки: " + e.getMessage());
            return "redirect:/applications/new";
        }
    }

    /**
     * Просмотр заявки
     */
    @GetMapping("/{id}")
    public String viewApplication(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        // Проверяем авторизацию
        Employee employee = (Employee) session.getAttribute("employee");
        if (employee == null) {
            return "redirect:/login";
        }

        try {
            CardRequest request = requestRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Заявка не найдена"));

            // Проверяем права доступа
            if (!employee.isAdmin() &&
                (request.getAssignedEmployee() == null || !request.getAssignedEmployee().getId().equals(employee.getId()))) {
                redirectAttributes.addFlashAttribute("error", "У вас нет прав для просмотра этой заявки");
                return "redirect:/applications";
            }

            // Логирование для отладки
            System.out.println("=== DEBUG INFO ===");
            System.out.println("Request Number: " + request.getRequestNumber());
            System.out.println("Status: " + request.getStatus().getStatusName());
            System.out.println("UserOrganization: " + (request.getUserOrganization() != null));
            if (request.getUserOrganization() != null) {
                System.out.println("User: " + request.getUserOrganization().getUser().getFullName());
                System.out.println("Organization: " + request.getUserOrganization().getOrganization().getName());
                System.out.println("Position: " + request.getUserOrganization().getPosition());
            }
            System.out.println("CardType: " + request.getCardType().getTypeName());
            System.out.println("Branch: " + request.getBranch().getName());
            System.out.println("==================");

            model.addAttribute("request", request);
            model.addAttribute("employee", employee);
            model.addAttribute("allStatuses", statusRepository.findAll());
            model.addAttribute("canEdit", request.getStatus() != null && !request.getStatus().getStatusName().equals("Выпущена"));

            // Данные для форм редактирования
            model.addAttribute("cardTypes", cardTypeRepository.findAll());
            model.addAttribute("branches", branchRepository.findByIsActiveTrue());

            return "applications/view";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Ошибка при загрузке заявки: " + e.getMessage());
            return "redirect:/applications";
        }
    }

    /**
     * Обновление заявки
     */
    @PostMapping("/{id}/update")
    public String updateApplication(
            @PathVariable Long id,
            @RequestParam Long userOrganizationId,
            @RequestParam Long cardTypeId,
            @RequestParam Long branchId,
            @RequestParam(required = false) String comments,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Employee employee = (Employee) session.getAttribute("employee");
        if (employee == null) {
            return "redirect:/login";
        }

        try {
            CardRequest request = requestRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Заявка не найдена"));

            // Проверяем, что заявка не выпущена
            if (request.getStatus().getStatusName().equals("Выпущена")) {
                redirectAttributes.addFlashAttribute("error", "Нельзя редактировать выпущенную заявку");
                return "redirect:/applications/" + id;
            }

            // Обновляем данные
            request.setUserOrganization(userOrganizationRepository.findById(userOrganizationId).orElseThrow());
            request.setCardType(cardTypeRepository.findById(cardTypeId).orElseThrow());
            request.setBranch(branchRepository.findById(branchId).orElseThrow());
            request.setComments(comments);
            request.setUpdatedAt(LocalDateTime.now());

            requestRepository.save(request);

            redirectAttributes.addFlashAttribute("success", "Заявка успешно обновлена");
            return "redirect:/applications/" + id;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении заявки: " + e.getMessage());
            return "redirect:/applications/" + id;
        }
    }

    /**
     * Смена статуса заявки
     */
    @PostMapping("/{id}/change-status")
    public String changeStatus(
            @PathVariable Long id,
            @RequestParam Long newStatusId,
            @RequestParam(required = false) String statusComment,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Employee employee = (Employee) session.getAttribute("employee");
        if (employee == null) {
            return "redirect:/login";
        }

        try {
            CardRequest request = requestRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Заявка не найдена"));

            RequestStatus newStatus = statusRepository.findById(newStatusId)
                    .orElseThrow(() -> new RuntimeException("Статус не найден"));

            RequestStatus oldStatus = request.getStatus();

            // Обновляем статус
            request.setStatus(newStatus);
            request.setUpdatedAt(LocalDateTime.now());
            requestRepository.save(request);

            // Сохраняем историю изменения (если нужно - можно добавить позже)

            redirectAttributes.addFlashAttribute("success",
                "Статус заявки изменен: " + oldStatus.getStatusName() + " → " + newStatus.getStatusName());
            return "redirect:/applications/" + id;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при смене статуса: " + e.getMessage());
            return "redirect:/applications/" + id;
        }
    }

    /**
     * Генерация номера заявки
     */
    private String generateApplicationNumber() {
        LocalDateTime now = LocalDateTime.now();
        String datePrefix = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = requestRepository.count() + 1;
        return String.format("REQ-%s-%04d", datePrefix, count);
    }
}
