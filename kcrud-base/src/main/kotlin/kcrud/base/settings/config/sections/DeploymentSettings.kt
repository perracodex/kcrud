/*
 * Copyright (c) 2024-Present Perracodex. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.settings.config.sections

import kcrud.base.settings.config.parser.IConfigSection
import kotlinx.serialization.Serializable

/**
 * Contains settings related to how the application is deployed.
 *
 * @property port The network port the server listens on.
 * @property sslPort The network port the server listens on for secure connections.
 * @property host The network address the server is bound to.
 */
@Serializable
data class DeploymentSettings(
    val port: Int,
    val sslPort: Int,
    val host: String,
) : IConfigSection
