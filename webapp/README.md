# webapp

## Instruction

For assignment1, this is a health check api to check if the database is connected


## Prerequisites

To run and build this project successfully, install the following software first

-[Java 17 +](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)

-[Maven 3.6.3+](https://maven.apache.org/docs/3.9.1/release-notes.html)

-[PostgreSQL 14.3](https://www.postgresql.org/download/)

-[homebrew](https://docs.brew.sh/Installation)


## Clone this project

Use SSH to clone this project

```
    git clone github-repo-link
```

## PostgreSQL

To conect to PostgreSQL using brew

```
    brew services start postgresql
```

To disconnect from PostgreSQL using brew

```
    brew services stop postgresql
```

To restart PostgreSQL

```
    brew services restart postgresql
```


## Build

Since we leave the data source infomation empty, we should use `-DskipTests` to skip test when building, to avoid any error

```
    mvn clean package -DskipTests
```


## Run

To run the project, you can either put your db connection information in the `resources-application.properties` or have your own `.properties` file

If chose to put db information in `resources-application.properties`, you just need to do the build process and execute

```
    java -jar "/path/to/your-file.jar"
```

execute the following comment to run the project

```
    java -jar "/path/to/your-file.jar" --spring.config.location="/path/to/your-file.properties"
```
/Users/yingtong/Documents/24 Fall/CloudComputingAssignment/webapp/target/CloudComputing-0.0.1-SNAPSHOT.jar
