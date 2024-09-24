/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.database.utils

import kcrud.core.env.SessionContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Schema
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Executes a transaction with a [SessionContext].
 *
 * See: [Transactions](https://github.com/JetBrains/Exposed/wiki/Transactions)
 *
 * See: [Schema Tests](https://github.com/JetBrains/Exposed/blob/main/exposed-tests/src/test/kotlin/org/jetbrains/exposed/sql/tests/shared/SchemaTests.kt)
 *
 * @param sessionContext The [SessionContext] instance to be used for the transaction.
 * @param db Optional database instance to be used for the transaction.
 * @param statement The block of code to execute within the transaction.
 * @return Returns the result of the block execution.
 */
public fun <T> transaction(
    sessionContext: SessionContext,
    db: Database? = null,
    statement: Transaction.() -> T
): T {
    return transaction(db = db) scope@{
        val auditor = AuditInterceptor(sessionContext = sessionContext)
        registerInterceptor(interceptor = auditor)

        try {
            if (sessionContext.schema.isNullOrBlank()) {
                // Directly proceed with the transaction if no schema is specified.
                return@scope statement()
            }

            val currentSchema: String = TransactionManager.current().connection.schema
            val changeSchema: Boolean = (sessionContext.schema != currentSchema)
            val originalSchema: String? = currentSchema.takeIf { changeSchema }

            // Change the schema only if it's different from the current one.
            if (changeSchema) {
                SchemaUtils.setSchema(schema = Schema(name = sessionContext.schema))
            }

            try {
                return@scope statement()
            } finally {
                // Restore the original schema if it was changed.
                originalSchema?.let { schemaName ->
                    SchemaUtils.setSchema(schema = Schema(name = schemaName))
                }
            }
        } finally {
            unregisterInterceptor(interceptor = auditor)
        }
    }
}
