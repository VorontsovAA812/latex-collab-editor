package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_documents")
public class UserDocument {
    @SequenceGenerator(name = "user_documents_id_gen", sequenceName = "users_user_id_seq", allocationSize = 1)
    @EmbeddedId
    private UserDocumentId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @MapsId("documentId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(name = "permission_level", nullable = false, length = 50)
    private String permissionLevel;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "added_at", nullable = false)
    private Instant addedAt;

    @Column(name = "branchName", nullable = false)
    private String branchName;




}