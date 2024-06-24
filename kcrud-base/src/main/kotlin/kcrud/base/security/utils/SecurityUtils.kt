/*
 * Copyright (c) 2024-Present Perracodex. Use of this source code is governed by an MIT license.
 */

package kcrud.base.security.utils

/**
 * Utility class for security related operations.
 */
object SecurityUtils {

    /**
     * Converts a hexadecimal string to a ByteArray of a specified length.
     * Pads with zeros or truncates as necessary, suitable for key generation in encryption.
     *
     * @param length The desired length of the key in bytes.
     * @return A ByteArray of the specified length.
     */
    fun String.toByteKey(length: Int): ByteArray {
        val requiredHexLength: Int = length * 2 // Each byte is represented by two hex characters.
        return (if (this.length < requiredHexLength) padEnd(requiredHexLength, padChar = '0') else take(requiredHexLength))
            .chunked(size = 2)
            .map { it.toInt(radix = 16).toByte() }
            .toByteArray()
    }

    /**
     * Extension string function escaping HTML tags in a string to prevent XSS (Cross-Site Scripting) attacks.
     *
     * This function replaces all instances of '<' with '&lt;' and '>' with '&gt;' in the input string.
     * This conversion ensures that any HTML tags present in the input are not rendered by the browser,
     * preventing potential XSS attacks where malicious scripts could be injected and executed.
     *
     * Note: This method is basic and might not cover all the cases for XSS prevention. For more
     * comprehensive security, consider using established libraries for input sanitization.
     *
     * Examples:
     * ```
     *      Input: "<script>alert('XSS')</script>"
     *      Output: "&lt;script&gt;alert('XSS')&lt;/script&gt;"
     *      Explanation: Converts script tags into harmless text.
     *
     *      Input: "<b>Hello, World!</b>"
     *      Output: "&lt;b&gt;Hello, World!&lt;/b&gt;"
     *      Explanation: Converts bold tags into text, preventing any HTML rendering.
     *
     *      Input: "Normal text without HTML"
     *      Output: "Normal text without HTML"
     *      Explanation: Text without HTML tags remains unchanged.
     *```
     * @return The sanitized string with HTML tags escaped.
     */
    @Suppress("unused")
    fun String.sanitizeHtmlInput(): String {
        return this.replace(oldValue = "<", newValue = "&lt;")
            .replace(oldValue = ">", newValue = "&gt;")
    }
}
