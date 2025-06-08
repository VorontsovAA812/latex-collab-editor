package com.example.demo.repos;

import com.example.demo.domain.UserDocument;
import com.example.demo.domain.UserDocumentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserDocumentRepo extends JpaRepository<UserDocument, UserDocumentId> {

    // Поиск связи между пользователем и документом
    Optional<UserDocument> findByUserIdAndDocumentId(Long userId, Long documentId);

    // Обновление имени ветки после успешного слияния
    @Modifying
    @Query("UPDATE UserDocument ud SET ud.branchName = :newBranch WHERE ud.id.userId = :userId AND ud.id.documentId = :documentId")
    void updateBranchName(Long userId, Long documentId, String newBranch);
}
