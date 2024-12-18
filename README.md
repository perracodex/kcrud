# [Krud](https://github.com/perracodex/Krud)

A fully functional CRUD REST server implemented using [Ktor](https://ktor.io/),

**Krud** serves as a comprehensive example of a Ktor server, showcasing a variety of features and best practices.

The code intentionally contains redundancies and some inconsistencies to illustrate different approaches and strategies to achieve
similar results. For example, different authentication methods (JWT, OAuth, and Basic, despite Basic being discouraged);
different approaches to exception handling and validations across layers. The project also examines data persistence,
illustrating custom columns, field level encryption, etc.

---

### Characteristics:

* [Multi-project](.wiki/01.project-structure.md) setup.
* [Exposed](https://github.com/JetBrains/Exposed) database framework.
* [Database Connection Pooling](krud-core/database/src/main/kotlin/krud/database/service/DatabasePooling.kt) with [HikariCP ](https://github.com/brettwooldridge/HikariCP).
* [Encryption](krud-core/database/src/main/kotlin/krud/database/schema/employment/EmploymentTable.kt) at field level example.
* [Koin](./krud-server/src/main/kotlin/krud/server/plugins/Koin.kt) dependency injection.
* [In-memory hashed passwords](./krud-core/access/src/main/kotlin/krud/access/credential/CredentialService.kt) storage lookup, with enhanced security.
* [Pagination](https://github.com/perracodex/exposed-pagination) and filtering examples.
* [JSON serialization](https://ktor.io/docs/serialization.html) with [Kotlinx](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/serialization-guide.md).
* [RBAC (Role Based Access Control)](./krud-core/access/src/main/kotlin/krud/access/domain/rbac) example, including a basic role [dashboard](./krud-core/access/src/main/kotlin/krud/access/domain/rbac/view).
* [JWT-authentication](./krud-core/access/src/main/kotlin/krud/access/plugins/AuthJwt.kt) example.
* [Basic-authentication](./krud-core/access/src/main/kotlin/krud/access/plugins/AuthBasic.kt) example.
* [OAuth-authentication](./krud-core/access/src/main/kotlin/krud/access/plugins/AuthOAuth.kt) example.
* [Connection Rate limit](krud-core/base/src/main/kotlin/krud/base/plugins/RateLimits.kt) examples.
* [HTML DSL](https://ktor.io/docs/server-html-dsl.html) example.
* [H2](https://github.com/h2database/h2database) embedded database, both in-memory and file-based.
* [HOCON](krud-core/base/src/main/resources/config) configuration example, including [parsing](krud-core/base/src/main/kotlin/krud/base/settings) for strongly typed settings.
* [OpenAPI](./krud-core/base/src/main/kotlin/krud/base/plugins/ApiSchema.kt) integration, including Swagger-UI and Redoc.
* [Routing](./krud-server/src/main/kotlin/krud/server/plugins/Routes.kt) organization examples.
* [Call Logging](https://ktor.io/docs/server-call-logging.html) and [Call ID](https://ktor.io/docs/server-call-id.html) examples for events traceability.
* [Snowflake](krud-core/base/src/main/kotlin/krud/base/security/snowflake) unique IDs for logging purposes, suitable for distributed systems.
* [Micrometer Metrics](krud-core/base/src/main/kotlin/krud/base/plugins/MicrometerMetrics.kt) with Prometheus integration. Configuration steps for Prometheus and Grafana are [included](.wiki/10.micrometer-metrics.md).
* [Flyway](https://github.com/flyway/flyway) database migration example.
* [Contextual Transactions](krud-core/database/src/main/kotlin/krud/database/util/Transaction.kt), allowing to execute concrete transactions per database connection and/or schema.
* [Custom exceptions](krud-core/base/src/main/kotlin/krud/base/error) including composite error responses.
* [Custom serializers](krud-core/base/src/main/kotlin/krud/base/serializer) examples.
* [Custom validators](krud-core/base/src/main/kotlin/krud/base/error/validator) and [custom table column](krud-core/database/src/main/kotlin/krud/database/column) constraints.
* [Fat Jar building](.wiki/03.fat-jar) and execution example.
* [Docker containerization](.wiki/04.docker) example.

---

For convenience, it is included a *[Postman Collection](./.postman/krud.postman_collection.json)* with all the available REST endpoints.

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

---

### License

This project is licensed under an MIT License - see the [LICENSE](LICENSE) file for details.
