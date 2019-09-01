insert into AUTHORS (ID, NAME) values (1001, 'sql_file');
GO
insert into books (id, title, author) values (123,'sql_and_liquibase', 1001);
GO