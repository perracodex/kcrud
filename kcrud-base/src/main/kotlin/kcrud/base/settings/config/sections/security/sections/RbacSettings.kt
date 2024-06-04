/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.settings.config.sections.security.sections

import kcrud.base.settings.config.parser.IConfigSection
import kotlinx.serialization.Serializable

/**
 * RBAC settings.
 *
 * @property isEnabled Flag to enable/disable RBAC authentication.
 */
@Serializable
data class RbacSettings(
    val isEnabled: Boolean
) : IConfigSection
