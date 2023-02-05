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

INSERT INTO USERS (name, surname, profile_picture, telephone_number, email, address, password, is_blocked, is_active, user_type)
VALUES ('Vukasin', 'Bodanovic', 'DEFAULT.jpg', '+12312321314', 'vukasin.bogdanovic610@gmail.com', 'Bulevar Oslobodjenja 74', '$2a$12$T1i/9on6Eq.PW6FlDo1HUOqV9GNmJ1Sp24LbE0J5OrLg.f06BcapG', false, true, 'PASSENGER');

INSERT INTO USER_ROLE (user_id, role_id) VALUES (2, 1);
INSERT INTO USER_ROLE (user_id, role_id) VALUES (1, 2);
INSERT INTO USER_ROLE (user_id, role_id) VALUES (3, 2);
INSERT INTO USER_ROLE (user_id, role_id) VALUES (4, 2);
INSERT INTO USER_ROLE (user_id, role_id) VALUES (5, 2);
INSERT INTO USER_ROLE (user_id, role_id) VALUES (6, 3);
INSERT INTO USER_ROLE (user_id, role_id) VALUES (7, 3);

INSERT INTO USER_ACTIVATIONS (date_created, life_span, user_id,code,valid)
VALUES ('2023-1-18 20:35:33.172279', TIMESTAMP '2022-09-05 12:47:00.000000' - TIMESTAMP '2022-09-04 12:47:00.000000', '5','1000',true);
INSERT INTO USER_ACTIVATIONS (date_created, life_span, user_id,code,valid)
VALUES ('2023-1-10 23:35:33.172279', TIMESTAMP '2022-09-05 12:47:00.000000' - TIMESTAMP '2022-09-04 12:47:00.000000', '4','2000',true);

INSERT INTO Password_Reset_Codes (date_created, life_span, user_id,code,valid)
VALUES ('2023-1-18 23:35:33.172279', TIMESTAMP '2022-09-05 12:47:00.000000' - TIMESTAMP '2022-09-04 12:47:00.000000', '3','3000',true);
INSERT INTO Password_Reset_Codes (date_created, life_span, user_id,code,valid)
VALUES ('2023-1-10 23:35:33.172279', TIMESTAMP '2022-09-05 12:47:00.000000' - TIMESTAMP '2022-09-04 12:47:00.000000', '1','4000',true);

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
INSERT INTO LOCATIONS (address, latitude, longitude) VALUES ('Matije Hudji 50', '44.997770', '19.573220');
INSERT INTO LOCATIONS (address, latitude, longitude) VALUES ('Radnicka 54', '44.979348', '19.582567');
INSERT INTO LOCATIONS (address, latitude, longitude) VALUES ('Antuna Urbana 54', '45.245711', '19.816702');

UPDATE vehicles SET location_id=4 WHERE id=2;
UPDATE vehicles SET location_id=1 WHERE id=1;

INSERT INTO RIDES (includes_babies,estimated_time,is_panic_pressed,includes_pets,start_time,status,price,driver_id,rejection_id,vehicle_type_id)
VALUES ('0','10','0','1','2023-01-08 19:15:33.172279','FINISHED','100.0','6',null,'1');
INSERT INTO RIDES (includes_babies,end_time,estimated_time,is_panic_pressed,includes_pets,start_time,status,price,driver_id,rejection_id,vehicle_type_id)
VALUES ('1','2022-12-06 23:35:33.172279','15','0','1','2022-12-06 23:35:33.172279','FINISHED','13.0','6',null,'1');
INSERT INTO RIDES (includes_babies,end_time,estimated_time,is_panic_pressed,includes_pets,start_time,status,price,driver_id,rejection_id,vehicle_type_id)
VALUES ('0','2022-12-07 23:35:33.172279','15','0','1','2022-12-07 23:35:33.172279','FINISHED','90.0','6',null,'1');
INSERT INTO RIDES (includes_babies,end_time,estimated_time,is_panic_pressed,includes_pets,start_time,status,price,driver_id,rejection_id,vehicle_type_id)
VALUES ('1','2022-12-08 23:35:33.172279','15','0','1','2022-12-08 23:35:33.172279','FINISHED','50.0','6',null,'1');
INSERT INTO RIDES (includes_babies,end_time,estimated_time,is_panic_pressed,includes_pets,start_time,status,price,driver_id,rejection_id,vehicle_type_id)
VALUES ('0','2022-12-26 23:35:33.172279','15','0','1','2022-12-26 23:35:33.172279','FINISHED','10.0','6',null,'1');
INSERT INTO RIDES (includes_babies,end_time,estimated_time,is_panic_pressed,includes_pets,start_time,status,price,driver_id,rejection_id,vehicle_type_id)
VALUES ('0','2022-12-10 23:35:33.172279','15','0','1','2022-12-10 23:35:33.172279','FINISHED','150.0','6',null,'1');

INSERT INTO PATHS (starting_point, destination) VALUES ('1', '2');
INSERT INTO PATHS (starting_point, destination) VALUES ('2', '3');
INSERT INTO PATHS (starting_point, destination) VALUES ('1', '3');
INSERT INTO PATHS (starting_point, destination) VALUES ('2', '3');
INSERT INTO PATHS (starting_point, destination) VALUES ('3', '1');
INSERT INTO PATHS (starting_point, destination) VALUES ('1', '3');
INSERT INTO PATHS (starting_point, destination) VALUES ('2', '1');

INSERT INTO RIDES_LOCATIONS (ride_id, locations_id) VALUES  (1, 1);
INSERT INTO RIDES_LOCATIONS (ride_id, locations_id) VALUES  (2, 2);
INSERT INTO RIDES_LOCATIONS (ride_id, locations_id) VALUES  (3, 3);
INSERT INTO RIDES_LOCATIONS (ride_id, locations_id) VALUES  (4, 4);
INSERT INTO RIDES_LOCATIONS (ride_id, locations_id) VALUES  (5, 5);
INSERT INTO RIDES_LOCATIONS (ride_id, locations_id) VALUES  (6, 6);

INSERT INTO PASSENGER_RIDES (passenger_id,ride_id)
VALUES ('1','1');
INSERT INTO PASSENGER_RIDES (passenger_id,ride_id)
VALUES ('3','1');
INSERT INTO PASSENGER_RIDES (passenger_id,ride_id)
VALUES ('1','2');
INSERT INTO PASSENGER_RIDES (passenger_id,ride_id)
VALUES ('1','3');
INSERT INTO PASSENGER_RIDES (passenger_id,ride_id)
VALUES ('1','4');
INSERT INTO PASSENGER_RIDES (passenger_id,ride_id)
VALUES ('1','5');
INSERT INTO PASSENGER_RIDES (passenger_id,ride_id)
VALUES ('1','6');

INSERT INTO PANICS (reason, time_pressed, ride_id, user_id) VALUES ('testtest', '2022-12-05 23:35:33.172279', '1', '1');
INSERT INTO PANICS (reason, time_pressed, ride_id, user_id) VALUES ('testtesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttest', '2022-12-05 23:35:33.172279', '1', '1');
INSERT INTO PANICS (reason, time_pressed, ride_id, user_id) VALUES ('testtest', '2022-12-05 23:34:33.172279', '1', '1');

INSERT INTO work_times (end_time, start_time, driver_id) VALUES ('2022-12-05 23:35:33.172279', '2022-12-05 23:35:33.172279', '2');

INSERT INTO RIDE_REVIEWS(comment,rating,type,passenger,ride) VALUES ('Vozilo je u losem stanju, popravite ga.', 3, 'DRIVER',1,1);
INSERT INTO RIDE_REVIEWS(comment,rating,type,passenger,ride) VALUES ('Sve super, svaka cast za cistocu vozila.', 5, 'VEHICLE',1,1);
INSERT INTO RIDE_REVIEWS(comment,rating,type,passenger,ride) VALUES ('Vozac je veoma korektan.', 4, 'DRIVER',3,1);
INSERT INTO RIDE_REVIEWS(comment,rating,type,passenger,ride) VALUES ('Vozilo se cuje kao traktor, neprijatni zvukovi. Nije mi dobro.', 4, 'VEHICLE',3,1);

INSERT INTO FAVORITE_PATHS(favorite_name,includes_babies,includes_pets,vehicle_type_id,owner) VALUES ('Home To Work',false,false,1,1);
INSERT INTO FAVORITE_PATHS(favorite_name,includes_babies,includes_pets,vehicle_type_id,owner) VALUES ('School',true,false,3,1);
INSERT INTO FAVORITE_PATHS(favorite_name,includes_babies,includes_pets,vehicle_type_id,owner) VALUES ('Focus Shisha Bar',true,true,1,1);
INSERT INTO PASSENGER_FAVORITE_PATHS(favorite_path_id,passenger_id) VALUES ('1','1');
INSERT INTO PASSENGER_FAVORITE_PATHS(favorite_path_id,passenger_id) VALUES ('1','2');
INSERT INTO PASSENGER_FAVORITE_PATHS(favorite_path_id,passenger_id) VALUES ('1','3');

INSERT INTO PATHS (starting_point, destination, fav_path_id) VALUES ('1', '3', '1');
INSERT INTO PATHS (starting_point, destination, fav_path_id) VALUES ('2', '3', '2');
INSERT INTO PATHS (starting_point, destination, fav_path_id) VALUES ('3', '1', '3');

INSERT INTO MESSAGES (message_content, message_type, sending_time, receiver_id, ride_id, sender_id) VALUES ('awdafsetserser', 'RIDE', '2022-12-05 23:35:33.172279', '1', '1', '6');
INSERT INTO MESSAGES (message_content, message_type, sending_time, receiver_id, ride_id, sender_id) VALUES ('awdaaerser', 'RIDE', '2022-12-06 01:40:33.172279', '6', '1', '1');
INSERT INTO MESSAGES (message_content, message_type, sending_time, receiver_id, ride_id, sender_id) VALUES ('sdfsdfsfsdfsf', 'RIDE', '2022-12-07 23:35:33.172279', '3', '1', '6');