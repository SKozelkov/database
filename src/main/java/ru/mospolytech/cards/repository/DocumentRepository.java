package ru.mospolytech.cards.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mospolytech.cards.entity.Document;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    // Найти все документы для заявки
    List<Document> findByRequestIdOrderByUploadedAtDesc(Long requestId);

    // Найти документы по типу
    List<Document> findByDocumentTypeOrderByUploadedAtDesc(String documentType);

    // Найти документы, загруженные конкретным сотрудником
    List<Document> findByUploadedByIdOrderByUploadedAtDesc(Long employeeId);
}
