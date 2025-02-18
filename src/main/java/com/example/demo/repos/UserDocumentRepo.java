package com.example.demo.repos;

import com.example.demo.domain.UserDocument;
import com.example.demo.domain.UserDocumentId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDocumentRepo extends JpaRepository<UserDocument, UserDocumentId> {
};
