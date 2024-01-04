
# Currency Conversion Service

## Description
The `currency-conversion-service` is a Spring Boot Reactive application built with Gradle. This service is responsible for handling queries and calculations needed for currency conversion. It interfaces with external exchange rate services and provides real-time currency conversion features.

## Prerequisites
Make sure you have the following tools installed and running on your system before you proceed with the setup:

- JDK 21
- Google Cloud SDK
- Gradle 8.5
- MongoDB running on port 27017 with username `anavarrete` and password `Navarrete11221`
- Redis database running on port 6379

## Compilation Guide
To compile the project, follow these steps:

1. Open your terminal or command prompt.
2. Navigate to the root directory of the project.
3. Execute the following commands in sequence:

```shell
gradle clean  # This command will clean your project by removing the build directory.
gradle build  # This will compile your project and create the build artifacts.
```

## Running the Application
To run the application, select the appropriate properties file for your environment:

- `application-dev.yml` for local development environment.
- `application-qa.yml` for QA environment.
- `application-prod.yml` for production environment.

Execute the `CurrencyConversionManagementApplication` by running the following command with the relevant profile:

```shell
java -Dspring.profiles.active=[profile] -jar build/libs/currency-conversion-service-0.0.1-SNAPSHOT.jar
```

Replace `[profile]` with `dev`, `qa`, or `prod` based on the environment you wish to run.

## Endpoint Documentation
Documentation for the API endpoints is provided through Swagger UI and is accessible at the `{url_service}/swagger-ui.html` URL. For example, the local development documentation can be accessed at http://localhost:9092/swagger-ui.html.

## Additional Information
As seen in the project structure, the application is organized into several layers including main Java package and resources for configurations. Ensure that the property files are configured correctly for your environment before launching the application. Additionally, the presence of the Dockerfile suggests that this service is ready to be containerized, which is suitable for cloud deployment.
