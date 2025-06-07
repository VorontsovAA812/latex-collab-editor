-- Удаляем столбец branchName, если он уже существует
ALTER TABLE user_documents
    DROP COLUMN IF EXISTS branchName;

-- Добавляем столбец branchName заново с типом TEXT
ALTER TABLE user_documents
    ADD COLUMN branchName TEXT;
