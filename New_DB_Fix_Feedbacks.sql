-- Bước 2: Tạo database với mã hóa hỗ trợ tiếng Việt
CREATE DATABASE IF NOT EXISTS KingPoolDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE KingPoolDB;

-- drop database kingpooldb;

-- Bước 3: Tạo các bảng với tối ưu hóa

-- Table Roles: Lưu trữ vai trò (Customer, Coach, Admin)
CREATE TABLE Roles (
    role_id INT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table Users: Lưu trữ thông tin khách hàng và nhân viên
CREATE TABLE Users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL, -- Thêm NOT NULL để bảo mật
    email VARCHAR(100) UNIQUE,
    phone_number VARCHAR(15),
    gender ENUM('M', 'F', 'Other'),
    date_of_birth DATE,
    hire_date DATE,
    role_id INT NOT NULL,
    address VARCHAR(255), -- Tăng từ 200 lên 255 để hỗ trợ địa chỉ dài hơn
    image VARCHAR(255), -- Phù hợp cho URL/đường dẫn
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    status ENUM('Active', 'Inactive') DEFAULT 'Active',
    FOREIGN KEY (role_id) REFERENCES Roles(role_id) ON DELETE RESTRICT,
    INDEX idx_username (username) -- Tăng tốc tìm kiếm theo username
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table Schedules: Quản lý khung giờ bán vé
CREATE TABLE Schedules (
    schedule_id INT PRIMARY KEY AUTO_INCREMENT,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    max_tickets INT NOT NULL,
    adult_price DECIMAL(8, 2) NOT NULL,
    child_price DECIMAL(8, 2) NOT NULL,
    status ENUM('Open', 'Closed', 'SoldOut') DEFAULT 'Open',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT CHK_Schedules_Time CHECK (end_time > start_time),
    INDEX idx_start_time (start_time) -- Tăng tốc tìm kiếm theo thời gian
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table Booking: Lưu trữ thông tin đặt vé trước
CREATE TABLE Booking (
    booking_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    schedule_id INT NOT NULL,
    booking_type ENUM('Ticket', 'SwimClass') NOT NULL,
    quantity_adult INT DEFAULT 0,
    quantity_child INT DEFAULT 0,
    total_price DECIMAL(10, 2) NOT NULL, -- Thêm NOT NULL để đảm bảo tính toán
    booking_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    status ENUM('Pending', 'Confirmed', 'Cancelled') DEFAULT 'Pending',
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (schedule_id) REFERENCES Schedules(schedule_id) ON DELETE CASCADE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table Tickets: Lưu trữ thông tin vé
CREATE TABLE Tickets (
    ticket_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    schedule_id INT NOT NULL,
    ticket_type ENUM('Adult', 'Child') NOT NULL,
    price DECIMAL(8, 2) NOT NULL,
    ticket_code VARCHAR(50) NOT NULL UNIQUE, -- Thêm NOT NULL để đảm bảo mã vé duy nhất
    purchase_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    status ENUM('Used', 'Unused') DEFAULT 'Unused',
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (schedule_id) REFERENCES Schedules(schedule_id) ON DELETE CASCADE,
    INDEX idx_ticket_code (ticket_code) -- Tăng tốc tra cứu mã vé
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table SwimPackages: Quản lý gói học bơi
CREATE TABLE SwimPackages (
    package_id INT PRIMARY KEY AUTO_INCREMENT,
    package_name VARCHAR(100) NOT NULL,
    description TEXT,
    total_sessions INT NOT NULL, -- Thêm NOT NULL để đảm bảo logic
    duration_days INT NOT NULL, -- Thêm NOT NULL
    price DECIMAL(8, 2) NOT NULL,
    coach_id INT,
    status ENUM('Active', 'Inactive') DEFAULT 'Active',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (coach_id) REFERENCES Users(user_id) ON DELETE SET NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table PackageSubscriptions: Lưu trữ đăng ký gói học bơi
CREATE TABLE PackageSubscriptions (
    subscription_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    package_id INT NOT NULL,
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    sessions_used INT DEFAULT 0,
    status ENUM('Active', 'Expired', 'Cancelled') DEFAULT 'Active',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (package_id) REFERENCES SwimPackages(package_id) ON DELETE CASCADE,
    CONSTRAINT CHK_Subscription_Dates CHECK (end_date >= start_date)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table PackageUsages: Lưu trữ lịch sử sử dụng gói học bơi
CREATE TABLE PackageUsages (
    usage_id INT PRIMARY KEY AUTO_INCREMENT,
    subscription_id INT NOT NULL,
    schedule_id INT NOT NULL,
    usage_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    note TEXT,
    FOREIGN KEY (subscription_id) REFERENCES PackageSubscriptions(subscription_id) ON DELETE CASCADE,
    FOREIGN KEY (schedule_id) REFERENCES Schedules(schedule_id) ON DELETE CASCADE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table Feedback: Lưu trữ phản hồi khách hàng
CREATE TABLE Feedback (
    feedback_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    content TEXT NOT NULL,
    rating INT NOT NULL, -- Thêm NOT NULL để đảm bảo đánh giá
    submitted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    response TEXT,
    responded_at DATETIME,
    status ENUM('Pending', 'Read', 'Responded') DEFAULT 'Pending',
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    CONSTRAINT CHK_Feedback_Rating CHECK (rating BETWEEN 1 AND 5)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table SupportRequests: Lưu trữ yêu cầu hỗ trợ
CREATE TABLE SupportRequests (
    request_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    support_type ENUM('Technical', 'Billing', 'Facility', 'Other') NOT NULL,
    description TEXT NOT NULL,
    image1_path VARCHAR(255),
    image2_path VARCHAR(255),
    priority ENUM('Low', 'Medium', 'High') DEFAULT 'Medium',
    submitted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    response TEXT,
    responded_at DATETIME,
    status ENUM('Pending', 'Read', 'Responded') DEFAULT 'Pending',
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table SwimClasses: Quản lý lớp học bơi
CREATE TABLE SwimClasses (
    class_id INT PRIMARY KEY AUTO_INCREMENT,
    schedule_id INT NOT NULL,
    coach_id INT NOT NULL,
    max_students INT NOT NULL,
    description TEXT,
    status ENUM('Open', 'Closed', 'Cancelled') DEFAULT 'Open',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (schedule_id) REFERENCES Schedules(schedule_id) ON DELETE CASCADE,
    FOREIGN KEY (coach_id) REFERENCES Users(user_id) ON DELETE CASCADE
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table SwimClassRegistrations: Lưu trữ đăng ký lớp học bơi
CREATE TABLE SwimClassRegistrations (
    registration_id INT PRIMARY KEY AUTO_INCREMENT,
    class_id INT NOT NULL,
    user_id INT NOT NULL,
    registration_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    status ENUM('Pending', 'Confirmed', 'Cancelled') DEFAULT 'Pending',
    FOREIGN KEY (class_id) REFERENCES SwimClasses(class_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    UNIQUE (class_id, user_id) -- Ngăn đăng ký trùng
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Bước 4: Chèn dữ liệu mẫu

-- Insert into Roles
INSERT INTO Roles (role_name) VALUES 
('CUSTOMER'),
('COACH'),
('ADMIN');

-- Insert into Users
INSERT INTO Users (name, username, password, email, phone_number, gender, date_of_birth, hire_date, role_id, address, image, status) VALUES 
('John Doe', 'johndoe', 'hashed_password1', 'john@example.com', '0123456789', 'M', '1990-05-15', NULL, 1, '123 Main St', 'john.jpg', 'Active'),
('Jane Smith', 'janesmith', 'hashed_password2', 'jane@example.com', '0987654321', 'F', '1985-08-20', '2023-01-10', 2, '456 Oak Ave', 'jane.jpg', 'Active'),
('Admin User', 'admin', 'hashed_password3', 'admin@example.com', '0112233445', 'Other', '1980-03-01', '2022-06-01', 3, '789 Pine Rd', 'admin.jpg', 'Active');

-- Insert into Schedules
INSERT INTO Schedules (start_time, end_time, max_tickets, adult_price, child_price, status) VALUES 
('2025-06-01 08:00:00', '2025-06-01 10:00:00', 50, 15.00, 10.00, 'Open'),
('2025-06-01 14:00:00', '2025-06-01 16:00:00', 40, 15.00, 10.00, 'Open'),
('2025-06-02 09:00:00', '2025-06-02 11:00:00', 30, 15.00, 10.00, 'Open');

-- Insert into Booking
INSERT INTO Booking (user_id, schedule_id, booking_type, quantity_adult, quantity_child, total_price, status) VALUES 
(1, 1, 'Ticket', 2, 1, 40.00, 'Confirmed'),
(2, 2, 'SwimClass', 1, 0, 15.00, 'Pending'),
(1, 3, 'Ticket', 1, 2, 35.00, 'Confirmed');

-- Insert into Tickets
INSERT INTO Tickets (user_id, schedule_id, ticket_type, price, ticket_code, status) VALUES 
(1, 1, 'Adult', 15.00, 'TCKT001', 'Unused'),
(1, 1, 'Child', 10.00, 'TCKT002', 'Unused'),
(2, 2, 'Adult', 15.00, 'TCKT003', 'Used');

-- Insert into SwimPackages
INSERT INTO SwimPackages (package_name, description, total_sessions, duration_days, price, coach_id, status) VALUES 
('Beginner Package', '10 sessions for beginners', 10, 30, 120.00, 2, 'Active'),
('Advanced Package', '15 sessions for advanced swimmers', 15, 45, 180.00, 2, 'Active'),
('Kids Package', '8 sessions for kids', 8, 20, 80.00, 2, 'Active');

-- Insert into PackageSubscriptions
INSERT INTO PackageSubscriptions (user_id, package_id, start_date, end_date, sessions_used, status) VALUES 
(1, 1, '2025-06-01 00:00:00', '2025-06-30 23:59:59', 2, 'Active'),
(2, 2, '2025-06-01 00:00:00', '2025-07-15 23:59:59', 5, 'Active'),
(1, 3, '2025-06-01 00:00:00', '2025-06-20 23:59:59', 0, 'Active');

-- Insert into PackageUsages
INSERT INTO PackageUsages (subscription_id, schedule_id, usage_date, note) VALUES 
(1, 1, '2025-06-02 08:00:00', 'Session 1 completed'),
(1, 2, '2025-06-03 14:00:00', 'Session 2 completed'),
(2, 2, '2025-06-04 14:00:00', 'Session 3 completed');

-- Insert into Feedback
INSERT INTO Feedback (user_id, content, rating, status) VALUES 
(1, 'Great facilities!', 5, 'Pending'),
(2, 'Need more coaches.', 3, 'Pending'),
(1, 'Friendly staff.', 4, 'Pending');

-- Insert into SupportRequests
INSERT INTO SupportRequests (user_id, support_type, description, priority, status) VALUES 
(1, 'Facility', 'Broken locker', 'High', 'Pending'),
(2, 'Billing', 'Overcharged for ticket', 'Medium', 'Pending'),
(1, 'Technical', 'App login issue', 'Low', 'Pending');

-- Insert into SwimClasses
INSERT INTO SwimClasses (schedule_id, coach_id, max_students, description, status) VALUES 
(1, 2, 10, 'Beginner swim class', 'Open'),
(2, 2, 8, 'Advanced swim class', 'Open'),
(3, 2, 12, 'Kids swim class', 'Open');

-- Insert into SwimClassRegistrations
INSERT INTO SwimClassRegistrations (class_id, user_id, status) VALUES 
(1, 1, 'Confirmed'),
(2, 2, 'Pending'),
(3, 1, 'Confirmed');

ALTER TABLE Feedback ADD COLUMN is_visible BOOLEAN DEFAULT TRUE;

UPDATE Feedback SET is_visible = TRUE WHERE feedback_id IN (1, 3);
UPDATE Feedback SET is_visible = FALSE WHERE feedback_id = 2;


UPDATE users SET role_id = (SELECT role_id FROM roles WHERE role_name = 'ADMIN') WHERE user_id = 3;

SELECT * FROM roles;

ALTER TABLE Feedback
ADD COLUMN is_read BOOLEAN DEFAULT FALSE;

-- Cập nhật trạng thái phản hồi cho các feedback hiện có
UPDATE Feedback
SET status = 'Responded'
WHERE response IS NOT NULL;

-- Cập nhật trạng thái đọc cho một số feedback mẫu
UPDATE Feedback
SET is_read = TRUE
WHERE feedback_id IN (1, 3);


ALTER TABLE Feedback
MODIFY COLUMN status ENUM('Pending', 'Read', 'Responded', 'Completed') DEFAULT 'Pending';

-- use kingpooldb;