# https://ktor.io/docs/docker.html
# https://ktor.io/docs/docker-compose.html

# This Dockerfile variant does not include the fat-JAR build stage, only the final image stage.
# It is intended to be used with a pre-built fat JAR.
# If the image is not found, use the next command: docker image prune -a

FROM amazoncorretto:17
LABEL authors="perracolabs"
LABEL image.tag="kcrud"
LABEL name="kcrud-final-image"
EXPOSE 8080

COPY build/libs/kcrud-1.0.0-all.jar /kcrud-1.0.0-all.jar
COPY build/libs/keystore.p12 /keystore.p12

ENTRYPOINT ["java","-jar","kcrud-1.0.0-all.jar"]
#-------------------------------------------------------------------------------------------------

# ENVOIRMENT VARIABLES

#-------------------------------------------------------------------------------------------------
# Ktor

# Set to 'true' to enable development mode.
# This make Ktor to enables features like auto-reload, better tracing, etc.,
# See: https://ktor.io/docs/development-mode.html
ENV KCRUD_KTOR_DEVELOPMENT=false

ENV KCRUD_KTOR_DEPLOYMENT_PORT=8080
ENV KCRUD_KTOR_DEPLOYMENT_SSL_PORT=8443

# Set to 0.0.0.0 to listen on all interfaces.
ENV KCRUD_KTOR_DEPLOYMENT_HOST="0.0.0.0"
#-------------------------------------------------------------------------------------------------

#-------------------------------------------------------------------------------------------------
# Netty Server
#
# Set next settings to 0 for a faster shutdown / restart in development mode.

# Maximum time for a server to stop accepting new requests, (ms).
ENV shutdownGracePeriod=1000
#  Maximum time to wait until the server stops completely. Must be >= shutdownGracePeriod, (ms).
ENV shutdownTimeout=2000
#-------------------------------------------------------------------------------------------------

#-------------------------------------------------------------------------------------------------
# Runtime

# Unique machine identifier.
# Used to generate unique snowflake IDs for calls traceability.
ENV KCRUD_RUNTIME_MACHINE_ID=1

# The environment type the application is running on.
# Not to confuse with the 'development' mode flag.
# Choices: DEV, TEST, STATING, PROD.
ENV KCRUD_RUNTIME_ENVIRONMENT="PROD"

# DoubleReceive is a feature that allows to receive the same request more than once.
# This is useful for debugging and testing.
# See: https://ktor.io/docs/double-receive.html
ENV KCRUD_RUNTIME_DOUBLE_RECEIVE=true
#-------------------------------------------------------------------------------------------------

#-------------------------------------------------------------------------------------------------
# Database

# The database connection pool size to be used.  0 for no connection pooling.
ENV KCRUD_DATABASE_CONNECTION_POOL_SIZE=100

# The minimum number of idle connections to maintain in the pool.
ENV KCRUD_DATABASE_MINIMUM_POOL_IDLE=20

# The database connection pool timeout to be used, (ms).
ENV KCRUD_DATABASE_CONNECTION_POOL_TIMEOUT_MS=30000

# The database transactions isolation level to be used.
# Choices: TRANSACTION_READ_UNCOMMITTED, TRANSACTION_READ_COMMITTED,
#    	   TRANSACTION_REPEATABLE_READ, TRANSACTION_SERIALIZABLE
ENV KCRUD_DATABASE_ISOLATION_LEVEL="TRANSACTION_REPEATABLE_READ"

# How many retries will be made inside any transaction block if a SQLException happens.
# This can be overridden on a per-transaction level by specifying the 'repetitionAttempts'
# property in a transaction block.
ENV KCRUD_DATABASE_TRANSACTION_RETRY_ATTEMPTS=3

# The minimum delay between transaction retries if a SQLException happens, (ms).
# This can be overridden on a per-transaction level by setting the 'minRepetitionDelay'
# property in a transaction block.
ENV FKCRUD_DATABASE_TRANSACTION_RETRY_MIN_DELAY_MS=15

# Threshold to log queries which exceed the threshold with WARN level, (ms).
# This can be overridden on a per-transaction level by setting the 'warnLongQueriesDuration'
# property in a transaction block.
ENV KCRUD_DATABASE_WARN_LONG_QUERIES_DURATION_MS=3000

# The list of environments under which it is allowed to update the database schema.
# By default Production is not allowed to update the schema.
# Empty list means 'no' environments are allowed to update the schema.
# Tha list can be either a single string with comma delimited values, or a list of strings.
# Choices: DEV, TEST, STATING, PROD.
ENV KCRUD_DATABASE_WARN_LONG_QUERIES_THRESHOLD_MS="DEV,TEST,STAGING,PROD"

# The database file name.
ENV KCRUD_DATABASE_NAME="dbv1"

# The database location path.
ENV KCRUD_DATABASE_PATH="/database/"

# The database JDBC URL.
# In memory: "jdbc:h2:mem:regular;DB_CLOSE_DELAY=-1;"
# Persisent: "jdbc:h2:file:"${database.path}${database.name}
ENV KCRUD_DATABASE_JDBC_URL="jdbc:h2:file:/database/kcrud.db"

# The database JDBC driver.
ENV KCRUD_DATABASE_JDBC_DRIVER="org.h2.Driver"
#-------------------------------------------------------------------------------------------------

#-------------------------------------------------------------------------------------------------
# CORS

# Hosts should be in the format:
# 		"host|comma-delimited-schemes|optional comma-delimited-subdomains".
# Example:
# 		allowedHosts: ["example.com|http,https|api,admin", "potato.com|https|api", "somewhere.com|https|"]
#
# If empty list or any of the hosts is '*', then the default is to allow all hosts,
# in which case schemes and subdomains are ignored even if defined, in addition any other host.
ENV KCRUD_CORS_ALLOWED_HOSTS="*"
#-------------------------------------------------------------------------------------------------

#-------------------------------------------------------------------------------------------------
# SSL
#
# https://ktor.io/docs/ssl.html
# https://ktor.io/docs/ssl.html#configure-ssl-ktor

ENV KCRUD_KTOR_SECURITY_SSL_KEY_STORE="./keystore.p12"
ENV KCRUD_KTOR_SECURITY_SSL_KEY_STORE_TYPE="PKCS12"
ENV KCRUD_KTOR_SECURITY_SSL_KEY_ALIAS="kcrud"
ENV KCRUD_KTOR_SECURITY_SSL_KEY_STORE_PASSWORD="kcrud"
ENV KCRUD_KTOR_SECURITY_SSL_PRIVATE_KEY_PASSWORD="kcrud"
#-------------------------------------------------------------------------------------------------

#-------------------------------------------------------------------------------------------------
# Application Security

# Whether to enable the Basic and JWT Authentication.
ENV KCRUD_SECURITY_ENABLED=true

# When true, it enables the 'HttpsRedirect' to redirect all HTTP requests to the HTTPS counterpart
# before processing any call, and the 'HSTS' plugin to add the required HTTP Strict Transport Security
# headers to all the requests.
ENV KCRUD_SECURITY_SECURE_CONNECTIONS=false

# RBAC requires JWT to be enabled and a valid JWT token in the Authorization header.
ENV KCRUD_SECURITY_RBAC_ENABLED=true
#-------------------------------------------------------------------------------------------------

#-------------------------------------------------------------------------------------------------
# JWT
ENV KCRUD_SECURITY_JWT_PROVIDER_NAME="kcrud-jwt-auth"
ENV KCRUD_SECURITY_JWT_TOKEN_LIFETIME_SEC=86400
ENV KCRUD_SECURITY_JWT_AUDIENCE="kcrud"
ENV KCRUD_SECURITY_JWT_REALM="kcrud"
ENV KCRUD_SECURITY_JWT_ISSUER="localhost"
ENV KCRUD_SECURITY_JWT_SECRET_KEY="9e6e26399b28fc5f5ad1e4431f8a387a60bf94b89716805a376319fcdca35ca8"
#-------------------------------------------------------------------------------------------------

#-------------------------------------------------------------------------------------------------
# Basic Auth
ENV KCRUD_SECURITY_BASIC_AUTH_PROVIDER_NAME="kcrud-basic-auth"
ENV KCRUD_SECURITY_BASIC_AUTH_REALM="kcrud"
#-------------------------------------------------------------------------------------------------

#-------------------------------------------------------------------------------------------------
# Encryption
#
# WARNING: The encryption settings below are critical for data privacy and integrity.
# Changing any of these values post-encryption will result in the inability to decrypt previously encrypted data.
# It is imperative to establish these values PRIOR to any data input and maintain them consistently to ensure data accessibility.
# In the event of attribute compromise, you must decrypt all affected data using the former settings before re-encrypting with
# updated values to prevent data loss. These settings must be handled with extreme caution and have restricted access.

# Encryption Algorithm Configuration.
# "AES_256_PBE_CBC" and "AES_256_PBE_GCM" are recommended for their balance of security and performance.
# Choices: AES_256_PBE_CBC, AES_256_PBE_GCM, BLOW_FISH, TRIPLE_DES
ENV KCRUD_SECURITY_ENCRYPTION_ALGORITHM="AES_256_PBE_CBC"

# Salt for Encryption.
# Utilized by "AES_256_PBE_CBC" and "AES_256_PBE_GCM" algorithms.
# Essential for password-based encryption to thwart dictionary attacks.
# Must be securely generated and unique per encryption operation for optimal security.
ENV KCRUD_SECURITY_ENCRYPTION_SALT="5c0744940b5c369b"

# Encryption Key.
# The secret key for data encryption. For AES-256-based encryption, this should be 32 bytes (256 bits).
# Ensure this key is strong and securely stored; exposure compromises all encrypted data.
ENV KCRUD_SECURITY_ENCRYPTION_KEY="kcrud-secret-key-example"

# HMAC Signature for Data Integrity.
# Used to verify the integrity and authenticity of the data. Compromise can lead to undetected tampering.
# Should be unique and securely stored, similar to the encryption key.
ENV KCRUD_SECURITY_ENCRYPTION_SIGN="kcrud-secret-sign-example"
#-------------------------------------------------------------------------------------------------

#-------------------------------------------------------------------------------------------------
# Constraints
#
# Constraints to be use for endpoints rate limiting.

# Maximum number of requests allowed for the Public API.
# Default is 10_000 requests per second.
ENV KCRUD_SECURITY_CONSTRAINTS_PUBLIC_API_LIMIT=10000
ENV KCRUD_SECURITY_CONSTRAINTS_PUBLIC_API_REFILL_MS=1000

# Maximum number of requests allowed for a New Authentication Token generation.
# Default is 1000 requests every 10 seconds.
ENV KCRUD_SECURITY_CONSTRAINTS_NEW_TOKEN_LIMIT=1000
ENV KCRUD_SECURITY_CONSTRAINTS_NEW_TOKEN_REFILL_MS=10000
#-------------------------------------------------------------------------------------------------

#-------------------------------------------------------------------------------------------------
# Docs

# The list of environments under which the documentation is enabled.
# If the environment is not in the list, the documentation will not be available.
# Empty list means 'no' documentation is available in any of the environments.
# Tha list can be either a single string with comma delimited values, or a list of strings.
# Choices: DEV, TEST, STATING, PROD.
ENV KCRUD_DOCS_ENVIRONMENTS="DEV,TEST"
#-------------------------------------------------------------------------------------------------