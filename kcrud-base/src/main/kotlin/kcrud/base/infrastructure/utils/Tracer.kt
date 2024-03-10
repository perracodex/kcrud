/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.infrastructure.utils

import kcrud.base.infrastructure.env.EnvironmentType
import kcrud.base.settings.AppSettings
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

/**
 * A simple tracer wrapper to provide a consistent logging interface.
 */
@Suppress("unused")
class Tracer(private val logger: Logger) {

    fun trace(message: String) {
        logger.trace(message)
    }

    fun debug(message: String) {
        logger.debug(message)
    }

    fun info(message: String) {
        logger.info(message)
    }

    fun warning(message: String) {
        logger.warn(message)
    }

    fun error(message: String) {
        logger.error(message)
    }

    fun error(message: String? = "Unexpected Exception", throwable: Throwable) {
        logger.error(message, throwable)
    }

    /**
     * Logs a message at different severity levels based on the current configured [EnvironmentType].
     * Intended for highlighting configurations or operations that might be allowed
     * for development or testing environments but not for production.
     * It logs the message as an error in production, as a warning in testing, and
     * as information in development environments.
     * This helps in quickly identifying potential misconfigurations or unintended
     * execution of certain code paths in specific deployment environments.
     *
     * @param message The message to log indicating the context or operation that needs attention.
     */
    fun byEnvironment(message: String) {
        when (val environment = AppSettings.runtime.environment) {
            EnvironmentType.PROD -> error("ATTENTION: '$environment' environment >> $message")
            EnvironmentType.STAGING -> warning("ATTENTION: '$environment' environment >> $message")
            EnvironmentType.TEST -> info(message)
            EnvironmentType.DEV -> info(message)
        }
    }

    companion object {
        /** Toggle for full package name or simple name. */
        const val LOG_FULL_PACKAGE = true

        /**
         * Creates a new [Tracer] instance for a given class.
         * Intended for classes where the class context is applicable.
         *
         * @param T The class for which the logger is being created.
         * @return Tracer instance with a logger named after the class.
         */
        inline operator fun <reified T : Any> invoke(): Tracer {
            val loggerName: String = if (LOG_FULL_PACKAGE) {
                T::class.qualifiedName ?: T::class.simpleName ?: "UnknownClass"
            } else {
                T::class.simpleName ?: "UnknownClass"
            }
            return Tracer(logger = LoggerFactory.getLogger(loggerName))
        }

        /**
         * Creates a new [Tracer] instance for a given function.
         * Intended for top-level and extension functions where class context is not applicable.
         *
         * @param ref The reference to the function for which the logger is being created.
         * @return Tracer instance named after the function and its declaring class (if available).
         */
        fun <R> byFunction(ref: KFunction<R>): Tracer {
            val declaringClass: Class<*>? = ref.javaMethod?.declaringClass
            val className: String = when {
                (LOG_FULL_PACKAGE && (declaringClass != null)) -> declaringClass.name
                (declaringClass != null) -> declaringClass.simpleName
                else -> "UnknownClass"
            }
            return Tracer(logger = LoggerFactory.getLogger("$className.${ref.name}"))
        }
    }
}
