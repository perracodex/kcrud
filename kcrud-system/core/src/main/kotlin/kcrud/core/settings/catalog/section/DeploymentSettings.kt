/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.settings.catalog.section

import io.github.perracodex.ktor.config.IConfigCatalogSection
import kotlinx.serialization.Serializable

/**
 * Contains settings related to how the application is deployed.
 *
 * @property port The network port the server listens on.
 * @property sslPort The network port the server listens on for secure connections.
 * @property host The network address the server is bound to.
 */
@Serializable
public data class DeploymentSettings(
    val port: Int,
    val sslPort: Int,
    val host: String,
) : IConfigCatalogSection
