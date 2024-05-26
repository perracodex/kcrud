/*
 * Copyright (c) 2024-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.env.health.checks

import kcrud.base.env.health.annotation.HealthCheckAPI
import kcrud.base.security.snowflake.SnowflakeData
import kcrud.base.security.snowflake.SnowflakeFactory
import kotlinx.serialization.Serializable

/**
 * A health check that generates a snowflake id and parses it.
 *
 * @property errors A list of errors that occurred during the check.
 * @property testId A generated snowflake id at the time of the check.
 * @property testResult The parsed snowflake data from the testId.
 * @property timestampEpoch The timestamp epoch used to generate the snowflake id.
 * @property nanoTimeStart The nano time start used to generate the snowflake id.
 */
@HealthCheckAPI
@Serializable
data class SnowflakeCheck(
    val errors: MutableList<String> = mutableListOf(),
    var testId: String? = null,
    var testResult: SnowflakeData? = null,
    val timestampEpoch: Long = SnowflakeFactory.timestampEpoch,
    val nanoTimeStart: Long = SnowflakeFactory.nanoTimeStart,
) {
    init {
        // Generating testId and handling potential exceptions.
        testId = try {
            SnowflakeFactory.nextId()
        } catch (ex: Exception) {
            null
        }

        if (testId == null) {
            errors.add(
                "${SnowflakeCheck::class.simpleName}. Error generating snowflake. " +
                        "timestampEpoch: $timestampEpoch, nanoTimeStart: $nanoTimeStart."
            )
        }

        // Parsing testResult and handling potential exceptions.
        testResult = try {
            SnowflakeFactory.parse(id = testId!!)
        } catch (ex: Exception) {
            null
        }

        if (testResult == null) {
            errors.add(
                "${SnowflakeCheck::class.simpleName}. Unable to parse testId '$testId'. " +
                        "timestampEpoch: $timestampEpoch, nanoTimeStart: $nanoTimeStart."
            )
        }
    }
}