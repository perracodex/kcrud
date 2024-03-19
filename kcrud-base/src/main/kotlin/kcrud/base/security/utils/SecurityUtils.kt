/*
 * Copyright (c) 2023-Present Perraco. All rights reserved.
 * This work is licensed under the terms of the MIT license.
 * For a copy, see <https://opensource.org/licenses/MIT>
 */

package kcrud.base.security.utils

/**
 * Utility class for security related operations.
 */
object SecurityUtils {

    /**
     * Extension string function converting to a 16-byte initialization vector (IV) for AES encryption.
     *
     * If the input string is shorter than 16 characters, it is padded with '0' to reach 16 characters.
     * If it is longer than 16 characters, it is truncated to the first 16 characters.
     * The resulting string is then converted to a ByteArray using UTF-8 encoding.
     *
     * Note: For security purposes, an IV should ideally be random and not reused with the same key.
     * Using a fixed or predictable IV can weaken the security of the encryption.
     *
     * @return The 16-byte initialization vector (IV) for AES encryption.
     */
    fun String.to16ByteIV(): ByteArray {
        return (if (length < 16) padEnd(length = 16, padChar = '0') else take(n = 16)).encodeToByteArray()
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
