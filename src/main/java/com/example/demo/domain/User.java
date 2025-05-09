
package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long id;

    @Column(name = "username", nullable = false, length = 50, unique = true)  // уникальность по имени явно прописана
    private String username;

    @ColumnDefault("'user'")
    @Column(name = "role", length = 20)
    private String role;

    @Column(name = "password", nullable = false, length = 50)
    private String password;

    @ColumnDefault("false")
    @Column(name = "is_online", nullable = false)
    private Boolean isOnline = false;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

}


/*
package com.example.demo.domain;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "usr")
public class User {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String role;

    @OneToOne(mappedBy = "author")
    private Document authoredDocument;

   @ManyToOne
   private Document document;
}
*/
