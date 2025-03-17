package com.example.demo.repos;

import com.example.demo.domain.DocumentBlockVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DocumentBlockVersionRepo extends JpaRepository<DocumentBlockVersion,Long> {
    @Query(value = "SELECT * FROM document_block_versions " +
            "WHERE block_id = :blockId " +
            "ORDER BY created_at DESC " +
            "LIMIT 1",
            nativeQuery = true)
    Optional<DocumentBlockVersion> findLastVersionByBlockId(@Param("blockId") Long blockId);
}

