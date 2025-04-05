-- Enable the pgvector extension to support vector operations in PostgreSQL
-- This adds vector data type and similarity search functions
CREATE EXTENSION IF NOT EXISTS vector;

-- Create a table to store items with vector embeddings
-- Each item has an ID, document_name, text, and a 1536-dimensional vector
-- The vector dimension (1536) matches common embedding models like OpenAI's
CREATE TABLE IF NOT EXISTS items
(
    id            SERIAL PRIMARY KEY,
    document_name VARCHAR(64),
    text          TEXT NOT NULL,
    embedding     VECTOR(1536)
);

-- Create an index for fast vector similarity search using Euclidean distance (L2 norm)
-- Uses IVF-Flat algorithm for approximate nearest neighbor search
-- The 'vector_l2_ops' operator class optimizes for the '<->' operator (Euclidean distance)
-- The 'lists' parameter controls the number of clusters (tradeoff between speed and accuracy)
CREATE INDEX idx_items_embedding_l2 ON items USING ivfflat (embedding vector_l2_ops) WITH (lists = 100);
-- Create an index for semantic search using Cosine similarity
-- Uses IVF-Flat algorithm for approximate nearest neighbor search
-- The 'vector_cosine_ops' operator class optimizes for the '<=>' operator (Cosine distance)
-- This index is important for semantic search where angle between vectors matters more than distance
CREATE INDEX idx_items_embedding_cosine ON items USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);

-- Grant database access permissions to the postgres user
-- This allows the default user to perform all operations on the database
GRANT ALL PRIVILEGES ON DATABASE vectordb TO postgres;
-- Grant table access permissions to the postgres user
-- This allows operations like SELECT, INSERT, UPDATE on all tables
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
-- Grant sequence access permissions to the postgres user
-- This allows the user to use auto-incrementing IDs in the tables
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO postgres;


-- Example of how to insert data with embeddings (commented for reference)
-- In production, embeddings would be generated from text using AI models
-- INSERT INTO items (document_name, text, embedding) VALUES ('microwave.txt', 'This is a test data', '[0.1, 0.2, 0.3, ...]');