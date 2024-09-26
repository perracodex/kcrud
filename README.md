# [Kcrud](https://github.com/perracodex/Kcrud)

A [Ktor](https://ktor.io/) REST server.

### Characteristics:

* [Multi-project](.wiki/01.project-structure.md) setup.
* [Exposed](https://github.com/JetBrains/Exposed) database framework.
* [Database Connection Pooling](kcrud-core/src/main/kotlin/kcrud/core/database/service/DatabasePooling.kt) with [HikariCP ](https://github.com/brettwooldridge/HikariCP).
* [Encryption](kcrud-core/src/main/kotlin/kcrud/core/database/schema/employment/EmploymentTable.kt) at field level example.
* [Koin](./kcrud-server/src/main/kotlin/kcrud/server/plugins/Koin.kt) dependency injection.
* [Quartz Scheduler](kcrud-core/src/main/kotlin/kcrud/core/scheduler) integration. A popular and [flexible](https://github.com/quartz-scheduler/quartz/blob/main/docs/introduction.adoc) job scheduling library.
* [In-memory hashed passwords](./kcrud-access/src/main/kotlin/kcrud/access/credential/CredentialService.kt) storage lookup, with enhanced security.
* [Pagination](https://github.com/perracodex/exposed-pagination) and filtering examples.
* [JSON serialization](https://ktor.io/docs/serialization.html) with [Kotlinx](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/serialization-guide.md).
* [RBAC (Role Based Access Control)](./kcrud-access/src/main/kotlin/kcrud/access/rbac) example, including a basic role [dashboard](./kcrud-access/src/main/kotlin/kcrud/access/rbac/view).
* [JWT-authentication](./kcrud-access/src/main/kotlin/kcrud/access/plugins/AuthJwt.kt) example.
* [Basic-authentication](./kcrud-access/src/main/kotlin/kcrud/access/plugins/AuthBasic.kt) example.
* [OAuth-authentication](./kcrud-access/src/main/kotlin/kcrud/access/plugins/AuthOAuth.kt) example.
* [Connection Rate limit](kcrud-core/src/main/kotlin/kcrud/core/plugins/RateLimits.kt) examples.
* [HTML DSL](https://ktor.io/docs/server-html-dsl.html) example.
* [H2](https://github.com/h2database/h2database) embedded database, both in-memory and file-based.
* [HOCON](kcrud-core/src/main/resources/config) configuration example, including [parsing](kcrud-core/src/main/kotlin/kcrud/core/settings) for strongly typed settings.
* [Swagger-UI](https://ktor.io/docs/server-swagger-ui.html#configure-swagger), [Redoc](https://swagger.io/blog/api-development/redoc-openapi-powered-documentation/), and [OpenAPI](https://ktor.io/docs/openapi.html) integration.
* [Routing](./kcrud-server/src/main/kotlin/kcrud/server/plugins/Routes.kt) organization examples.
* [Call Logging](https://ktor.io/docs/server-call-logging.html) and [Call ID](https://ktor.io/docs/server-call-id.html) examples for events traceability.
* [Snowflake](kcrud-core/src/main/kotlin/kcrud/core/security/snowflake) unique IDs for logging purposes, suitable for distributed systems.
* [Micrometer Metrics](kcrud-core/src/main/kotlin/kcrud/core/plugins/MicrometerMetrics.kt) with Prometheus integration. Configuration steps for Prometheus and Grafana are [included](.wiki/10.micrometer-metrics).
* [Flyway](https://github.com/flyway/flyway) database migration example.
* [Contextual Transactions](kcrud-core/src/main/kotlin/kcrud/core/database/utils/Transaction.kt), allowing to execute concrete transactions per database connection and/or schema.
* [Custom exceptions](kcrud-core/src/main/kotlin/kcrud/core/errors) including composite error responses.
* [Custom serializers](kcrud-core/src/main/kotlin/kcrud/core/persistence/serializers) examples.
* [Custom validators](kcrud-core/src/main/kotlin/kcrud/core/errors/validators) and [custom table column](kcrud-core/src/main/kotlin/kcrud/core/database/columns) constraints.
* [Fat Jar building](.wiki/03.fat-jar) and execution example.
* [Docker containerization](.wiki/04.docker) example.

---

For convenience, it is included a *[Postman Collection](./.postman/kcrud.postman_collection.json)* with all the available REST endpoints.

---

### Preface

[Kcrud](https://github.com/perracodex/Kcrud) serves as a comprehensive example of a Ktor server, showcasing a variety of features and best practices.

The code intentionally contains redundancies and inconsistencies to illustrate different approaches and strategies to achieve
similar results. For example, different authentication methods (JWT, OAuth, and Basic, despite Basic being discouraged);
different approaches to exception handling and validations across layers. The project also examines data persistence,
illustrating custom columns with validation, field level encryption, different ways to store enums in a database, etc.

---

### Wiki

* ### [Project Structure](./.wiki/01.project-structure.md)

* ### [Domain Component Design Overview](./.wiki/02.domain-component-design.md)

* ### [Building and Executing a Fat JAR](./.wiki/03.fat-jar.md)

* ### [Docker Containerization](./.wiki/04.docker.md)

* ### [RBAC (Role Based Access Control)](./.wiki/05.rbac.md)

* ### [Pagination](./.wiki/06.pagination.md)

* ### [Interactive Paginated Demo](./.wiki/07.demo.md)

* ### [REST endpoints](./.wiki/08.rest.md)

* ### [Handling Security](./.wiki/09.security.md)

* ### [Micrometer Metrics](./.wiki/10.micrometer-metrics.md)
