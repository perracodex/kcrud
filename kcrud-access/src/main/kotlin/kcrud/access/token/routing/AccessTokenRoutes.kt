/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.access.token.routing

import io.ktor.server.routing.*
import kcrud.access.token.annotation.TokenAPI
import kcrud.access.token.routing.operate.createTokenRoute
import kcrud.access.token.routing.operate.refreshTokenRoute

/**
 * Defines routes for handling access tokens within the authentication system,
 * using JWT and Basic Authentication as specified in Ktor's documentation.
 *
 * • `create`: Generates a new JWT token using Basic Authentication.
 *   This endpoint is rate-limited to prevent abuse and requires valid Basic Authentication credentials.
 *
 * • `refresh`: Allows a client to refresh their existing JWT token. This endpoint does not require
 *   Basic Authentication but does require a valid JWT token in the 'Authorization' header.
 *   Depending on the state of the provided token (valid, expired, or invalid), it either returns
 *   the same token, generates a new one, or denies access.
 *
 * See: [Ktor JWT Authentication Documentation](https://ktor.io/docs/server-jwt.html)
 *
 * See: [Basic Authentication Documentation](https://ktor.io/docs/server-basic-auth.html)
 */
@OptIn(TokenAPI::class)
public fun Route.accessTokenRoutes() {
    createTokenRoute()
    refreshTokenRoute()
}
