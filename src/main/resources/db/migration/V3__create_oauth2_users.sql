-- Create oauth2_users table for storing GitHub authenticated users
CREATE TABLE oauth2_users (
    id BIGSERIAL PRIMARY KEY,
    github_id VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255),
    name VARCHAR(255),
    avatar_url VARCHAR(500),
    roles VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    last_login TIMESTAMP NOT NULL
);

-- Create indexes for commonly queried columns
CREATE INDEX idx_oauth2_users_github_id ON oauth2_users(github_id);
CREATE INDEX idx_oauth2_users_username ON oauth2_users(username);
CREATE INDEX idx_oauth2_users_last_login ON oauth2_users(last_login);
