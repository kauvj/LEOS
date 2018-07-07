Insert into LEOS_USER (USER_LOGIN,USER_PER_ID,USER_LASTNAME,USER_FIRSTNAME,USER_DG,USER_EMAIL) values ('demo',1,'DEMO','demo','EE','demo.test@mail.com');
Insert into LEOS_USER (USER_LOGIN,USER_PER_ID,USER_LASTNAME,USER_FIRSTNAME,USER_DG,USER_EMAIL) values ('john',2,'DOE','john','DG','john.test@mail.com');
Insert into LEOS_USER (USER_LOGIN,USER_PER_ID,USER_LASTNAME,USER_FIRSTNAME,USER_DG,USER_EMAIL) values ('jane',3,'DOE','jane','DG','jane.test@mail.com');
Insert into LEOS_USER (USER_LOGIN,USER_PER_ID,USER_LASTNAME,USER_FIRSTNAME,USER_DG,USER_EMAIL) values ('luke',4,'SKYWALKER','luke','DG','luke.test@mail.com');
Insert into LEOS_USER (USER_LOGIN,USER_PER_ID,USER_LASTNAME,USER_FIRSTNAME,USER_DG,USER_EMAIL) values ('leia',5,'ORGANA','leia','DG','leia.test@mail.com');
Insert into LEOS_USER (USER_LOGIN,USER_PER_ID,USER_LASTNAME,USER_FIRSTNAME,USER_DG,USER_EMAIL) values ('han',6,'SOLO','han','DG','han.test@mail.com');
Insert into LEOS_USER (USER_LOGIN,USER_PER_ID,USER_LASTNAME,USER_FIRSTNAME,USER_DG,USER_EMAIL) values ('chewie',7,'CHEWBACCA','chewie','DG','chewie.test@mail.com');
Insert into LEOS_USER (USER_LOGIN,USER_PER_ID,USER_LASTNAME,USER_FIRSTNAME,USER_DG,USER_EMAIL) values ('c3po',8,'C-3PO','c3po','DG','c3po.test@mail.com');
Insert into LEOS_USER (USER_LOGIN,USER_PER_ID,USER_LASTNAME,USER_FIRSTNAME,USER_DG,USER_EMAIL) values ('r2d2',9,'R2-D2','r2d2','DG','r2d2.test@mail.com');
Insert into LEOS_USER (USER_LOGIN,USER_PER_ID,USER_LASTNAME,USER_FIRSTNAME,USER_DG,USER_EMAIL) values ('vader',10,'DARTH','vader','DG','vader.test@mail.com');
Insert into LEOS_USER (USER_LOGIN,USER_PER_ID,USER_LASTNAME,USER_FIRSTNAME,USER_DG,USER_EMAIL) values ('admin',99,'SUPER','ADMIN','DG','super.admin@mail.com');

INSERT INTO LEOS_ROLE (ROLE_NAME, ROLE_DESC) VALUES ('SUPPORT', 'Support role');
INSERT INTO LEOS_ROLE (ROLE_NAME, ROLE_DESC) VALUES ('ADMIN', 'Admin role');

INSERT INTO LEOS_USER_ROLE (USER_LOGIN, ROLE_NAME) VALUES ('jane', 'SUPPORT');
INSERT INTO LEOS_USER_ROLE (USER_LOGIN, ROLE_NAME) VALUES ('admin', 'ADMIN');

Commit;
