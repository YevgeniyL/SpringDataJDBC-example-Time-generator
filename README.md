# SpringDataJDBC example: Time-generator
Simple example java application with smallest framework "Spring Data JDBC"

### Include

 - spring-data-jdbc
 - HikariCP
 
### Build and configuration
Base application configuration (datasource, timeouts...) in file application.properties
```sh 
build with maven: clean assembly:assembly 
```
Simple start is will run Timestamp generation and write data to database
If run with key -p woll be show database data. Additional key -debug to show more info.

### Database structure
```sh 
create table generated_data
(
	Id bigint auto_increment
		primary key,
	created timestamp(6) null
):
```

