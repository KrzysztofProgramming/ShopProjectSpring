ALTER TABLE IF EXISTS order_products_ids ADD CONSTRAINT fk_products FOREIGN KEY (product_id) REFERENCES book_products_table (book_id);
ALTER TABLE IF EXISTS cart_products_ids ADD CONSTRAINT fk_products FOREIGN KEY (product_id) REFERENCES book_products_table (book_id) ON DELETE CASCADE;

ALTER TABLE authors_table ADD COLUMN textsearchable_index_col tsvector GENERATED ALWAYS AS (to_tsvector('english', coalesce(name, ''))) STORED;
ALTER TABLE types_table ADD COLUMN textsearchable_index_col tsvector GENERATED ALWAYS AS (to_tsvector('english', coalesce(name, ''))) STORED;
ALTER TABLE book_products_table ADD COLUMN textsearchable_index_col tsvector GENERATED ALWAYS AS (to_tsvector('english', coalesce(name, '') || ' ' || coalesce(description, ''))) STORED;

CREATE INDEX textsearch_types_idx ON types_table USING GIN (textsearchable_index_col);
CREATE INDEX textsearch_authors_idx ON authors_table USING GIN (textsearchable_index_col);
CREATE INDEX textsearch_books_idx ON book_products_table USING GIN (textsearchable_index_col);