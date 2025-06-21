
CREATE TABLE IF NOT EXISTS cart (
    cart_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,  -- References the username from user table
    medicine_id INT NOT NULL,       -- References the medicine_id from medicine table
    quantity INT NOT NULL DEFAULT 1,
    date_added TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (medicine_id) REFERENCES medicine(medicine_id)
);

