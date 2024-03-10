/*
 * Copyright (c) 2024-Present Perraco Labs. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.settings.config.sections

import kcrud.base.settings.config.parser.IConfigSection

/**
 * Contains settings related to CORS.
 *
 * Hosts should be in the format:
 *      "host|comma-delimited-schemes|optional comma-delimited-subdomains".
 * Example:
 * ```
 * 	 	"example.com|http,https|api,admin",
 * 	 	"potato.com|https|api",
 * 	 	"somewhere.com|https|"
 * ```
 *
 * If empty list or any of the hosts is '*', then the default is to allow all hosts,
 * in which case schemes and subdomains are ignored even if defined, in addition of
 * any other hosts.
 *
 * @property allowedHosts The list of allowed hosts used in CORS.
 */
data class CorsSettings(
    val allowedHosts: List<String>
) : IConfigSection {
    data class HostConfig(
        val host: String,
        val schemes: List<String>,
        val subDomains: List<String>
    )

    /**
     * Returns true if the allowed hosts list is empty
     * or any of the hosts is/or starts with a wildcard.
     */
    fun allowAllHosts(): Boolean {
        return allowedHosts.isEmpty() or
                (allowedHosts.any { it.startsWith(prefix = "*") })
    }

    companion object {

        /**
         * Parses a host configuration from a string.
         */
        fun parse(spec: String): HostConfig {
            var host = ""
            var schemes: List<String> = emptyList()
            var subDomains: List<String> = emptyList()

            spec.split('|').forEachIndexed { index, part ->
                when (index) {
                    0 -> host = part.trim()
                    1 -> schemes = part.split(',').filterNot { it.isBlank() }.map { it.trim() }
                    2 -> subDomains = part.split(',').filterNot { it.isBlank() }.map { it.trim() }
                }
            }

            return HostConfig(
                host = host,
                schemes = schemes,
                subDomains = subDomains
            )
        }
    }
}
