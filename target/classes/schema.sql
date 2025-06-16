-- Create photo table
CREATE TABLE IF NOT EXISTS photo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    url VARCHAR(255) NOT NULL
);

-- Create qrcode table
CREATE TABLE IF NOT EXISTS qrcode (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    image_url VARCHAR(255)
);

-- Create object table
CREATE TABLE IF NOT EXISTS object (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    photo_id BIGINT,
    object_name VARCHAR(255),
    category VARCHAR(255),
    FOREIGN KEY (photo_id) REFERENCES photo(id)
);

-- Create object_qrcode relation table (many-to-many)
CREATE TABLE IF NOT EXISTS object_qrcode (
    object_id BIGINT NOT NULL,
    qrcode_id BIGINT NOT NULL,
    PRIMARY KEY (object_id, qrcode_id),
    FOREIGN KEY (object_id) REFERENCES object(id) ON DELETE CASCADE,
    FOREIGN KEY (qrcode_id) REFERENCES qrcode(id) ON DELETE CASCADE
); 