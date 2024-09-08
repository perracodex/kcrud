/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.database.schema.employment.types

import kcrud.base.database.columns.IEnumWithId

/**
 * The possible employment work modalities.
 *
 * @property id The unique identifier of the work modality.
 */
public enum class WorkModality(override val id: Int) : IEnumWithId {
    /** The employee works on-site premises. */
    ON_SITE(id = 100),

    /** The employee works remotely. */
    REMOTE(id = 101),

    /** The employee works in a hybrid modality, so both on-site and remotely. */
    HYBRID(id = 102);
}
