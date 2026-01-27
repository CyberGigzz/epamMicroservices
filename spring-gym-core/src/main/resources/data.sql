-- Seed Training Types
INSERT INTO training_types (id, training_type_name) VALUES (1, 'Cardio');
INSERT INTO training_types (id, training_type_name) VALUES (2, 'Strength');
INSERT INTO training_types (id, training_type_name) VALUES (3, 'Yoga');
INSERT INTO training_types (id, training_type_name) VALUES (4, 'Pilates');
INSERT INTO training_types (id, training_type_name) VALUES (5, 'CrossFit');

-- Seed Users (password is BCrypt encoded "password123")
-- Trainers
INSERT INTO users (id, first_name, last_name, username, password, is_active)
VALUES (1, 'John', 'Smith', 'john.smith', '$2a$10$lUpyjuWKywh8kgSndVCnH.8.B/WCj7iwwKsML8mdeMRq1dXifhaNi', true);
INSERT INTO users (id, first_name, last_name, username, password, is_active)
VALUES (2, 'Jane', 'Doe', 'jane.doe', '$2a$10$lUpyjuWKywh8kgSndVCnH.8.B/WCj7iwwKsML8mdeMRq1dXifhaNi', true);

-- Trainees
INSERT INTO users (id, first_name, last_name, username, password, is_active)
VALUES (3, 'Mike', 'Johnson', 'mike.johnson', '$2a$10$lUpyjuWKywh8kgSndVCnH.8.B/WCj7iwwKsML8mdeMRq1dXifhaNi', true);
INSERT INTO users (id, first_name, last_name, username, password, is_active)
VALUES (4, 'Sarah', 'Wilson', 'sarah.wilson', '$2a$10$lUpyjuWKywh8kgSndVCnH.8.B/WCj7iwwKsML8mdeMRq1dXifhaNi', true);

-- Seed Trainers (references users table)
INSERT INTO trainers (id, specialization_id) VALUES (1, 2);  -- John Smith - Strength
INSERT INTO trainers (id, specialization_id) VALUES (2, 3);  -- Jane Doe - Yoga

-- Seed Trainees (references users table)
INSERT INTO trainees (id, date_of_birth, address) VALUES (3, '1995-05-15', '123 Main St, New York');
INSERT INTO trainees (id, date_of_birth, address) VALUES (4, '1998-08-22', '456 Oak Ave, Los Angeles');

-- Link trainees to trainers
INSERT INTO trainee_trainer (trainee_id, trainer_id) VALUES (3, 1);  -- Mike with John
INSERT INTO trainee_trainer (trainee_id, trainer_id) VALUES (3, 2);  -- Mike with Jane
INSERT INTO trainee_trainer (trainee_id, trainer_id) VALUES (4, 1);  -- Sarah with John

-- Reset sequences to avoid ID conflicts with new inserts
ALTER TABLE users ALTER COLUMN id RESTART WITH 100;
ALTER TABLE training_types ALTER COLUMN id RESTART WITH 100;
