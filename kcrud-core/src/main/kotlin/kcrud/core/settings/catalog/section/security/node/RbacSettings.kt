/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.core.settings.catalog.section.security.node

import io.github.perracodex.ktor.config.IConfigCatalogSection
import kotlinx.serialization.Serializable

/**
 * RBAC settings.
 *
 * @property isEnabled Flag to enable/disable RBAC authentication.
 */
@Serializable
public data class RbacSettings(
    val isEnabled: Boolean
) : IConfigCatalogSection
