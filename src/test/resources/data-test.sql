INSERT INTO ROLES (name) VALUES('ROLE_ADMIN');
INSERT INTO ROLES (name) VALUES('ROLE_PASSENGER');
INSERT INTO ROLES (name) VALUES('ROLE_DRIVER');

INSERT INTO USERS (name, surname, profile_picture, telephone_number, email, address, password, is_blocked, is_active, user_type)
VALUES ('Pera', 'Perić', '803537000.jpg', '+12312321314', 'pera.peric@email.com', 'Bulevar Oslobodjenja 74', '$2a$12$T1i/9on6Eq.PW6FlDo1HUOqV9GNmJ1Sp24LbE0J5OrLg.f06BcapG', false, true, 'PASSENGER');
INSERT INTO USERS (name, surname, profile_picture, telephone_number, email, address, password, is_blocked, is_active, user_type)
VALUES ('Marko', 'Markovic', 'DEFAULT.jpg', '+12312321314', 'marko.markovic@email.com', 'Bulevar Oslobodjenja 75', '$2a$12$T1i/9on6Eq.PW6FlDo1HUOqV9GNmJ1Sp24LbE0J5OrLg.f06BcapG', false, true, 'ADMIN');
INSERT INTO USERS (name, surname, profile_picture, telephone_number, email, address, password, is_blocked, is_active, user_type)
VALUES ('Darko', 'Darkovic', 'DEFAULT.jpg', '+12312321314', 'darko.darkovic@email.com', 'Bulevar Oslobodjenja 76', '$2a$12$T1i/9on6Eq.PW6FlDo1HUOqV9GNmJ1Sp24LbE0J5OrLg.f06BcapG', false, true, 'PASSENGER');
INSERT INTO USERS (name, surname, profile_picture, telephone_number, email, address, password, is_blocked, is_active, user_type)
VALUES ('Petar', 'Petrovic', 'DEFAULT.jpg', '+12312321314', 'petar.petrovic@email.com', 'Bulevar Oslobodjenja 77', '$2a$12$T1i/9on6Eq.PW6FlDo1HUOqV9GNmJ1Sp24LbE0J5OrLg.f06BcapG', false, false, 'PASSENGER');
INSERT INTO USERS (name, surname, profile_picture, telephone_number, email, address, password, is_blocked, is_active, user_type)
VALUES ('Perica', 'Petkovic', 'DEFAULT.jpg', '+12312321314', 'parica.petkovic@email.com', 'Bulevar Oslobodjenja 78', '$2a$12$T1i/9on6Eq.PW6FlDo1HUOqV9GNmJ1Sp24LbE0J5OrLg.f06BcapG', false, false, 'PASSENGER');
INSERT INTO USERS (name, surname, profile_picture, telephone_number, email, address, password, is_blocked, is_active, is_online, user_type)
VALUES ('Perislav', 'Perić', '1808251220.jpg', '+12312321314', 'perislav.peric@email.com', 'Bulevar Oslobodjenja 74', '$2a$12$T1i/9on6Eq.PW6FlDo1HUOqV9GNmJ1Sp24LbE0J5OrLg.f06BcapG', false, true, true, 'DRIVER');
INSERT INTO USERS (name, surname, profile_picture, telephone_number, email, address, password, is_blocked, is_active, is_online, user_type)
VALUES ('Nikola', 'Nikolic', 'DEFAULT.jpg', '+12312321314', 'nikola.nikolic@email.com', 'Bulevar Oslobodjenja 74', '$2a$12$T1i/9on6Eq.PW6FlDo1HUOqV9GNmJ1Sp24LbE0J5OrLg.f06BcapG', false, true, true, 'DRIVER');

INSERT INTO USER_ROLE (user_id, role_id) VALUES (2, 1);
INSERT INTO USER_ROLE (user_id, role_id) VALUES (1, 2);
INSERT INTO USER_ROLE (user_id, role_id) VALUES (3, 2);
INSERT INTO USER_ROLE (user_id, role_id) VALUES (4, 2);
INSERT INTO USER_ROLE (user_id, role_id) VALUES (5, 2);
INSERT INTO USER_ROLE (user_id, role_id) VALUES (6, 3);
INSERT INTO USER_ROLE (user_id, role_id) VALUES (7, 3);

INSERT INTO Vehicles(are_babies_allowed,are_pets_allowed,licence_plate_number,model,number_of_seats,vehicle_type,driver_id)
VALUES ('1','0','SM074HZ','Skoda Octavia','5','1','6');
INSERT INTO Vehicles(are_babies_allowed,are_pets_allowed,licence_plate_number,model,number_of_seats,vehicle_type,driver_id)
VALUES ('1','1','SM075HZ','Skoda Octavia','5','0','7');

UPDATE USERS SET vehicle_id=1 WHERE ID=6;
UPDATE USERS SET vehicle_id=2 WHERE ID=7;

INSERT INTO VEHICLE_TYPE_PRICES (vehicle_type,price_per_km, image_path)
VALUES ('STANDARD','2.49', 'car_model_01.png');
INSERT INTO VEHICLE_TYPE_PRICES (vehicle_type,price_per_km, image_path)
VALUES ('VAN','4.49', 'car_model_02.png');
INSERT INTO VEHICLE_TYPE_PRICES (vehicle_type,price_per_km, image_path)
VALUES ('LUX','6.99', 'car_model_03.png');

INSERT INTO LOCATIONS (address, latitude, longitude) VALUES ('Djure Danicica 82', '44.975980', '19.583750');
INSERT INTO LOCATIONS (address, latitude, longitude) VALUES ('Antuna Urbana 54', '45.245711', '19.816702');

UPDATE vehicles SET location_id=1 WHERE id=1;
UPDATE vehicles SET location_id=2 WHERE id=2;

INSERT INTO RIDES (includes_babies,end_time,estimated_time,is_panic_pressed,includes_pets,start_time,status,price,driver_id,rejection_id,vehicle_type_id)
VALUES ('1','2022-12-06 23:35:33.172279','15','0','1','2022-12-06 23:35:33.172279','ACTIVE','13.0','7',null,'1');

INSERT INTO RIDES (includes_babies,end_time,estimated_time,is_panic_pressed,includes_pets,start_time,status,price,driver_id,rejection_id,vehicle_type_id)
VALUES ('1','2022-12-06 23:35:33.172279','15','0','1','2022-12-06 23:35:33.172279','PENDING','13.0','7',null,'1');
INSERT INTO RIDES (includes_babies,end_time,estimated_time,is_panic_pressed,includes_pets,start_time,status,price,driver_id,rejection_id,vehicle_type_id)
VALUES ('1','2022-12-06 23:35:33.172279','15','0','1','2022-12-06 23:35:33.172279','PENDING','13.0','7',null,'1');
INSERT INTO RIDES (includes_babies,end_time,estimated_time,is_panic_pressed,includes_pets,start_time,status,price,driver_id,rejection_id,vehicle_type_id)
VALUES ('1','2022-12-06 23:35:33.172279','15','0','1','2022-12-06 23:35:33.172279','PENDING','13.0','6',null,'1');

INSERT INTO PATHS (starting_point, destination) VALUES (1, 2);
INSERT INTO RIDES_LOCATIONS (ride_id, locations_id) VALUES  (1, 1);
INSERT INTO PASSENGER_RIDES (passenger_id,ride_id) VALUES (3, 1);
INSERT INTO PASSENGER_RIDES (passenger_id,ride_id) VALUES (3, 2);

INSERT INTO FAVORITE_PATHS(favorite_name,includes_babies,includes_pets,vehicle_type_id,owner) VALUES ('Focus Shisha Bar',true,true,1,1);
INSERT INTO PASSENGER_FAVORITE_PATHS(favorite_path_id,passenger_id) VALUES ('1','1');