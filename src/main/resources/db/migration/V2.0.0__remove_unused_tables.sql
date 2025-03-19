DROP TABLE IF EXISTS document_block_versions;
DROP TABLE IF EXISTS document_blocks;
DROP TABLE IF EXISTS document_versions;
-- Добавляем поле content в documents
ALTER TABLE documents
    ADD COLUMN content TEXT NOT NULL DEFAULT '';

-- Опционально: Добавляем last_modified_at
ALTER TABLE documents
    ADD COLUMN last_modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
