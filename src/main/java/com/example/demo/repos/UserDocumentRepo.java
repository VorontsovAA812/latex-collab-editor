package com.example.demo.repos;

import com.example.demo.domain.UserDocument;
import com.example.demo.domain.UserDocumentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface UserDocumentRepo extends JpaRepository<UserDocument, UserDocumentId> {
    @Modifying
    @Transactional
    @Query("UPDATE UserDocument ud SET ud.branch_name = :branchName WHERE ud.id.userId = :userId AND ud.id.documentId = :documentId")
    void updateBranchName(@Param("userId") Long userId,
                          @Param("documentId") Long documentId,
                          @Param("branchName") String branchName);
};
