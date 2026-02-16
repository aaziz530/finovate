-- Run this in phpMyAdmin or MySQL to add 2 test users for Finovate
-- Database: finovate | Table: user

-- User 1: alice@finovate.tn / alice123
-- User 2: bob@finovate.tn / bob456

-- Passwords are SHA-256 hashed (same as your app)
INSERT INTO `user` (email, password, firstname, lastname, role, points, createdAt, solde, numeroCarte, birthdate)
VALUES
  ('alice@finovate.tn', '4e40e8ffe0ee32fa53e139147ed559229a5930f89c2204706fc174beb36210b3', 'Alice', 'Martin', 'USER', 0, NOW(), 0, NULL, '1990-05-15'),
  ('bob@finovate.tn', 'ed4d9437294706c60027d39427f6f5850870625544bb77722aac19f97495b2b7', 'Bob', 'Wilson', 'USER', 0, NOW(), 0, NULL, '1988-11-20');
