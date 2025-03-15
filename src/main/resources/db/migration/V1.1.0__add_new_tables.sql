-- Удаление таблицы document_versions
DROP TABLE IF EXISTS document_versions;

-- Создание таблицы document_blocks
CREATE TABLE document_blocks (
                                 block_id SERIAL PRIMARY KEY,
                                 document_id INTEGER NOT NULL,
                                 title VARCHAR(100),
                                 order_index INT,
                                 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 FOREIGN KEY (document_id) REFERENCES documents(document_id) ON DELETE CASCADE
);

-- Создание таблицы document_block_versions
CREATE TABLE document_block_versions (
                                         block_version_id SERIAL PRIMARY KEY,
                                         block_id INTEGER NOT NULL,
                                         previous_block_version_id INTEGER NULL,
                                         author_user_id INTEGER NOT NULL,
                                         content TEXT NOT NULL,
                                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         FOREIGN KEY (block_id) REFERENCES document_blocks(block_id) ON DELETE CASCADE,
                                         FOREIGN KEY (author_user_id) REFERENCES users(user_id) ON DELETE SET NULL,
                                         FOREIGN KEY (previous_block_version_id) REFERENCES document_block_versions(block_version_id) ON DELETE SET NULL
);