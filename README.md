# Node Management Service

**Node Management Service** is a RESTful API designed for managing hierarchical data structures. It provides endpoints to add, delete, move nodes, and fetch the descendants of a node. This service allows you to organize and manipulate tree-like structures with CRUD (Create, Read, Update, Delete) operations.

## Technologies Used

- **Spring Boot** 3.x
- **JPA (Java Persistence API)** with **MySQL** for production database
- **H2 Database** for integration tests
- **Swagger** for interactive API documentation
- **Docker** for running MySQL via a `docker-compose.yaml` file

## Features

The service exposes the following operations via API:

- **Add a child node to a parent node** (`POST /api/nodes/{parentName}/children`): Adds a new child node under an existing parent node.
- **Delete a child node** (`DELETE /api/nodes/{parentName}/children/{childName}`): Deletes a child node from a parent node.
- **Move a node to a new parent** (`PUT /api/nodes/{nodeName}/parent/{parentName}`): Moves a node to a new parent node.
- **Get all descendants of a node** (`GET /api/nodes/{nodeName}/descendants`): Retrieves a list of all descendant nodes for a given node.

## Database Technologies

- **MySQL**: Used as the production database, with the connection configured in the `application.yaml` file. The MySQL instance is pre-configured with the `nodemanagementservicedb` database.
  
- **H2 Database**: Used for integration tests. It is configured to allow local testing with an in-memory database.

## Docker Compose

A `docker-compose.yaml` file is included in the project to easily spin up a MySQL instance. You can quickly start MySQL and the test database using Docker.

To launch the service, run:
docker-compose up

This command will start MySQL and automatically set up the `nodemanagementservicedb` database as defined in the `application.yaml` configuration.

## API Documentation

Interactive API documentation is available through **Swagger**. You can access the Swagger UI at the following URL:

[http://localhost:8080/swagger-ui/index.html#/](http://localhost:8080/swagger-ui/index.html#/)

Here, you can explore the endpoints, read operation descriptions, and test API calls directly from the interface.

## Installation and Setup

### Prerequisites

- **Java 17** (as specified in `pom.xml`)
- **Docker** (for running MySQL via `docker-compose`)
- **Maven** (for dependency management and project build)

### Steps to Run the Project

1. Clone the repository:

    ```bash
    git clone https://github.com/your-username/node-management-service.git
    ```

2. Navigate to the project directory:

    ```bash
    cd node-management-service
    ```

3. Build the project using Maven:

    ```bash
    mvn clean install
    ```

4. Run the service with:

    ```bash
    mvn spring-boot:run
    ```

   The service will be available at `http://localhost:8080`.

5. If you want to use Docker to run MySQL, execute:

    ```bash
    docker-compose up
    ```

    This command will start MySQL in a Docker container, configured as described in the `docker-compose.yaml` file.


## Dependencies

The main dependencies of the project are:

- **Spring Boot** (for creating the RESTful service)
- **Spring Data JPA** (for interacting with the MySQL database)
- **Swagger (Springdoc OpenAPI)** (for interactive API documentation)
- **Lombok** (for automatic handling of Java annotations like getters, setters, and constructors)
- **MySQL Connector** (for connecting to MySQL)
- **H2 Database** (for integration testing)
