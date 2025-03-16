package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "document_block_versions", schema = "public")
public class DocumentBlockVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "document_block_versions_id_gen")
    @SequenceGenerator(name = "document_block_versions_id_gen", sequenceName = "document_block_versions_block_version_id_seq", allocationSize = 1)
    @Column(name = "block_version_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "block_id", nullable = false)
    private DocumentBlock block;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "previous_block_version_id")
    private DocumentBlockVersion previousBlockVersion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "author_user_id", nullable = false)
    private User authorUser;

    @Column(name = "content", nullable = false, length = Integer.MAX_VALUE)
    private String content;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

}