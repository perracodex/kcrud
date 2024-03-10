/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.database.service

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Schema
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Executes a transaction with an optional schema switch.
 * The previous schema is restored after the transaction is completed.
 *
 * See: [Transactions](https://github.com/JetBrains/Exposed/wiki/Transactions)
 *
 * See: [Schema Tests](https://github.com/JetBrains/Exposed/blob/main/exposed-tests/src/test/kotlin/org/jetbrains/exposed/sql/tests/shared/SchemaTests.kt)
 *
 * @param db Optional database instance to be used for the transaction.
 * @param schema Optional name of the schema to be set for the transaction.
 * @param statement The block of code to execute within the transaction.
 * @return Returns the result of the block execution.
 */
fun <T> transactionWithSchema(db: Database? = null, schema: String? = null, statement: Transaction.() -> T): T {
    // Directly proceed with the transaction if no schema is specified.
    if (schema.isNullOrBlank()) {
        return transaction(statement = statement)
    }

    // Proceed with schema adjustment only if a schema is provided.
    return transaction(db = db) {
        val currentSchema: String = TransactionManager.current().connection.schema
        val changeSchema: Boolean = (schema != currentSchema)
        val originalSchema: String? = currentSchema.takeIf { changeSchema }

        // Change the schema only if it's different from the current one.
        if (changeSchema) {
            SchemaUtils.setSchema(schema = Schema(name = schema))
        }

        try {
            statement()
        } finally {
            // Restore the original schema if it was changed.
            originalSchema?.let { schemaName ->
                SchemaUtils.setSchema(schema = Schema(name = schemaName))
            }
        }
    }
}
