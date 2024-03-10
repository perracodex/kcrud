/*
 * Copyright (c) 2023-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.graphql.expedia

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.generator.federation.directives.ContactDirective
import com.expediagroup.graphql.server.Schema
import kcrud.base.graphql.expedia.annotation.ExpediaAPI

@ContactDirective(
    name = "KCrud Schema",
    url = "https://github.com/perracodex/Kcrud",
    description = "Expedia schema example with Ktor."
)
@GraphQLDescription("Kcrud schema description")
@ExpediaAPI
internal class KcrudSchema : Schema
