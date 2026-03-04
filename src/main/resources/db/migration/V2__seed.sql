INSERT INTO role(name)
VALUES ('CUSTOMER'),
       ('BUSINESS'),
       ('ADMIN');

INSERT INTO role_authority(role_id, authority)
SELECT r.id, 'DATA_READ'
FROM role r
WHERE r.name = 'CUSTOMER';

INSERT INTO role_authority(role_id, authority)
SELECT r.id, a.authority
FROM role r
         CROSS JOIN (SELECT 'DATA_READ' AS authority UNION ALL SELECT 'DATA_UPDATE') a
WHERE r.name = 'BUSINESS';

INSERT INTO role_authority(role_id, authority)
SELECT r.id, a.authority
FROM role r
         CROSS JOIN (SELECT 'DATA_READ' AS authority
                     UNION ALL SELECT 'DATA_UPDATE'
                     UNION ALL SELECT 'DATA_CREATE'
                     UNION ALL SELECT 'DATA_DELETE') a
WHERE r.name = 'ADMIN';

INSERT INTO app_user(username, password_hash)
VALUES ('customer1', '$2b$10$9cbYtwtQfuxJxWjVtnkDAe868AUxcxr0NQfDRX9hzTeoFHzLq3VAm'),
       ('business1', '$2b$10$HWKubf5XI8n16c3Z26Fms..haLdOWeyiCKSiaIIH4.71f6BhMr9Z.'),
       ('admin1', '$2b$10$tgXYPOGDExg4LpXSR3Oex.e2l/e7MZvHWFVH.H8TR9zbssCArn9be');

INSERT INTO user_role(user_id, role_id)
SELECT u.id, r.id
FROM app_user u
         JOIN role r ON (
    (u.username = 'customer1' AND r.name = 'CUSTOMER') OR
    (u.username = 'business1' AND r.name = 'BUSINESS') OR
    (u.username = 'admin1' AND r.name = 'ADMIN')
    );

INSERT INTO item(name, description)
VALUES ('Laptop', 'Business ultrabook'),
       ('Phone', 'Demo smartphone item'),
       ('Desk', 'Office desk item');
