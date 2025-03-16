package com.example.demo.repos;

import com.example.demo.domain.DocumentBlockVersion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentBlockVersionRepo extends JpaRepository<DocumentBlockVersion,Long> {
}
