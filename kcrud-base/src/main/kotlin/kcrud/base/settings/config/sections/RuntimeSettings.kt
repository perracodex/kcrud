/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.settings.config.sections

import kcrud.base.env.EnvironmentType
import kcrud.base.settings.config.parser.IConfigSection
import kotlinx.serialization.Serializable

/**
 * Contains settings related to server runtime.
 *
 * @property machineId The unique machine ID. Used for generating unique IDs for call traceability.
 * @property environment The environment type. Not to be confused with the development mode flag.
 * @property doubleReceive Whether to enable the DoubleReceive plugin.
 * @property workingDir The working directory where files are stored.
 */
@Serializable
data class RuntimeSettings(
    val machineId: Int,
    val environment: EnvironmentType,
    val doubleReceive: Boolean,
    val workingDir: String
) : IConfigSection
