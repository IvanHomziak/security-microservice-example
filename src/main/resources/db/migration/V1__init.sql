CREATE TABLE app_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(200) NOT NULL,
    role VARCHAR(30) NOT NULL
);

CREATE TABLE authority (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE user_authority (
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    authority_id BIGINT NOT NULL REFERENCES authority(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, authority_id)
);

CREATE TABLE item (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description TEXT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
