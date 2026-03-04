INSERT INTO authority(name)
VALUES ('DATA_READ'),
       ('DATA_UPDATE'),
       ('DATA_CREATE'),
       ('DATA_DELETE');

INSERT INTO app_user(username, password_hash, role)
VALUES ('customer1', '$2b$10$9cbYtwtQfuxJxWjVtnkDAe868AUxcxr0NQfDRX9hzTeoFHzLq3VAm', 'CUSTOMER'),
       ('business1', '$2b$10$HWKubf5XI8n16c3Z26Fms..haLdOWeyiCKSiaIIH4.71f6BhMr9Z.', 'BUSINESS'),
       ('admin1', '$2b$10$tgXYPOGDExg4LpXSR3Oex.e2l/e7MZvHWFVH.H8TR9zbssCArn9be', 'ADMIN');

INSERT INTO user_authority(user_id, authority_id)
SELECT u.id, a.id
FROM app_user u
         JOIN authority a ON a.name = 'DATA_READ'
WHERE u.username = 'customer1';

INSERT INTO user_authority(user_id, authority_id)
SELECT u.id, a.id
FROM app_user u
         JOIN authority a ON a.name IN ('DATA_READ', 'DATA_UPDATE')
WHERE u.username = 'business1';

INSERT INTO user_authority(user_id, authority_id)
SELECT u.id, a.id
FROM app_user u
         JOIN authority a ON a.name IN ('DATA_READ', 'DATA_UPDATE', 'DATA_CREATE', 'DATA_DELETE')
WHERE u.username = 'admin1';

INSERT INTO item(name, description)
VALUES ('Laptop', 'Business ultrabook'),
       ('Phone', 'Demo smartphone item'),
       ('Desk', 'Office desk item');
