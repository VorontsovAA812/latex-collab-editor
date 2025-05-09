CREATE TABLE users
(
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,  -- уникальность по имени явно прописана
    role VARCHAR(20) DEFAULT 'user' CHECK (role IN ('user', 'admin')),
    password VARCHAR(50) NOT NULL, -- Хранение хэшированных паролей,
    is_online BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP




);

CREATE TABLE documents(
                          document_id SERIAL PRIMARY KEY,
                          title VARCHAR(50) NOT NULL,
                          owner_user_id INTEGER NOT NULL,
                          FOREIGN KEY (owner_user_id) REFERENCES users(user_id)
                              ON DELETE CASCADE -- Если создатель документа удалится из систесы то и его документы удалятся
);

-- Таблица user_documents с составным первичным ключом
CREATE TABLE user_documents (
                                user_id INTEGER NOT NULL,
                                document_id INTEGER NOT NULL,
                                permission_level VARCHAR(50) NOT NULL CHECK (permission_level IN ('read', 'write', 'comment')),
                                added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                PRIMARY KEY (user_id, document_id),
                                FOREIGN KEY (user_id) REFERENCES users(user_id)
                                    ON DELETE CASCADE, --Если пользователь удаляется из системы, все его связи с документами (user_documents) также должны быть удалены. Это автоматически убирает все права доступа пользователя к документам.

                                FOREIGN KEY (document_id) REFERENCES documents(document_id)
                                    ON DELETE CASCADE --  Если документ удаляется, все связанные записи в user_documents должны быть удалены, так как доступ к несуществующему документу больше не имеет смысла.
);

CREATE TABLE document_versions (
                                   version_id SERIAL PRIMARY KEY,
                                   document_id INTEGER NOT NULL,
                                   author_user_id INTEGER NOT NULL,
                                   content TEXT NOT NULL,
                                   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   previous_version_id INTEGER,
                                   is_published BOOLEAN NOT NULL DEFAULT FALSE,
                                   FOREIGN KEY (document_id) REFERENCES documents(document_id)
                                       ON DELETE CASCADE, -- Если документ удаляется, все его версии должны быть удалены. Это предотвращает накопление версий документов, которые больше не существуют.

                                   FOREIGN KEY (author_user_id) REFERENCES users(user_id)
                                       ON DELETE SET NULL, -- Если пользователь, создавший версию документа, удаляется, поле author_user_id устанавливается в NULL. Это сохраняет информацию о версиях, но указывает, что автор больше не существует.

                                   FOREIGN KEY (previous_version_id) REFERENCES document_versions(version_id)
                                       ON DELETE SET NULL -- Если предыдущая версия документа удаляется, поле previous_version_id устанавливается в NULL. Это позволяет избежать нарушений ссылочной целостности, сохраняя текущую версию.

);







