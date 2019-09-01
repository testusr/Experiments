https://www.baeldung.com/spring-boot-start

#### h2 in memory console:
http://localhost:8080/h2-console

#### to generate the schema from hibernate entities against an existing db
mvn liquibase:diff

#### run the application agains h2 in memory db
mvn spring-boot:run

### migrate the hsqldb
mvn liquibase:update

### rollback to verions 1.0
mvn liquibase:rollback -Dliquibase.rollbackTag=v1.0