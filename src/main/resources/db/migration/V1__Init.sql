CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(128) UNIQUE NOT NULL,
    VRC_id VARCHAR(40) NULL,
    email VARCHAR(128) UNIQUE NOT NULL,
    password VARCHAR(60) NOT NULL
);

CREATE TABLE packages (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(256) UNIQUE NOT NULL,
    author BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    display_name VARCHAR(1024) NOT NULL,
    description TEXT NOT NULL
);

CREATE TABLE package_blobs (
    id BIGSERIAL PRIMARY KEY,
    zip_file BYTEA NOT NULL
);

CREATE TABLE package_versions (
    id BIGSERIAL PRIMARY KEY,
    package bigint REFERENCES packages(id) ON DELETE CASCADE,
    version_major INT,
    version_minor INT,
    version_revision INT,
    blob BIGINT NOT NULL REFERENCES package_blobs(id) ON DELETE CASCADE,
    UNIQUE(package, version_major, version_minor, version_revision)
);

CREATE TABLE package_dependencies (
    dependent BIGINT REFERENCES package_versions(id) ON DELETE CASCADE,
    dependency VARCHAR(256),
    dependency_version VARCHAR(64) DEFAULT 'x',
    PRIMARY KEY (dependent, dependency)
);