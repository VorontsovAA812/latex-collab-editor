package com.example.demo.repos;

import com.example.demo.domain.Document;
import com.example.demo.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocumentRepo extends JpaRepository<Document,Long> {
    @Query(value = "SELECT * FROM usr WHERE id = :id", nativeQuery = true)
    Optional<User> findByIdNative(@Param("id") Long id);
    // В интерфейсе DocumentRepository

    @Query(value = "SELECT DISTINCT d.* FROM documents d " +
            "LEFT JOIN user_documents ud ON d.document_id = ud.document_id " +
            "WHERE d.owner_user_id = :userId OR ud.user_id = :userId", nativeQuery = true)
    List<Document> findAllDocumentsByUserId(@Param("userId") Long userId);
}
