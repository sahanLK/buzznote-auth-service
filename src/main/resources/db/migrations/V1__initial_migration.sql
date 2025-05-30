CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    membership VARCHAR(255) CHECK (membership IN ('FREE', 'SILVER', 'GOLD', 'PLATINUM')),
    password VARCHAR(255)
);
