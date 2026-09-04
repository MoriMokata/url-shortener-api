CREATE TABLE short_urls (
    id BIGSERIAL PRIMARY KEY,
    short_code VARCHAR(16) NOT NULL,
    original_url TEXT NOT NULL,
    owner_id BIGINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_short_urls_short_code UNIQUE (short_code),
    CONSTRAINT fk_short_urls_owner FOREIGN KEY (owner_id) REFERENCES users (id)
);

CREATE INDEX idx_short_urls_owner_id ON short_urls (owner_id);
