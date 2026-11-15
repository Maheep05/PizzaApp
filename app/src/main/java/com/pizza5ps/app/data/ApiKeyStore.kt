package com.pizza5ps.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

/**
 * Stores the customer's own Anthropic API key, on their device only.
 *
 * **No key ships with the app.** An API key baked into an APK is readable by anyone
 * who receives it — `unzip` and `strings` are enough — and spending on it bills
 * whoever owns it. Since this app is handed around as an APK, a bundled key would be
 * a published key. So the chatbot's cloud mode is opt-in and runs on a key the user
 * pastes in themselves.
 *
 * The key is held in [EncryptedSharedPreferences], wrapped by a master key in the
 * Android keystore, so it is encrypted at rest and does not leak through device
 * backups. It never leaves the device except in the `x-api-key` header of requests
 * to Anthropic.
 */
class ApiKeyStore(context: Context) {

    private val prefs: SharedPreferences
    /** False when the keystore was unavailable and the key sits in plain prefs. */
    val encryptionAvailable: Boolean

    init {
        val encrypted = runCatching {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            EncryptedSharedPreferences.create(
                ENCRYPTED_FILE,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }.getOrNull()

        // The keystore can be unavailable on an unusual or tampered device. Falling
        // back keeps the app usable, and the flag lets the UI say the key is not
        // encrypted rather than implying a guarantee it is not providing.
        prefs = encrypted ?: context.getSharedPreferences(PLAIN_FILE, Context.MODE_PRIVATE)
        encryptionAvailable = encrypted != null
    }

    fun read(): String? = prefs.getString(KEY_API_KEY, null)?.takeIf { it.isNotBlank() }

    fun write(apiKey: String) {
        prefs.edit().putString(KEY_API_KEY, apiKey.trim()).apply()
    }

    fun clear() {
        prefs.edit().remove(KEY_API_KEY).apply()
    }

    private companion object {
        const val ENCRYPTED_FILE = "pizza5ps_secure_prefs"
        const val PLAIN_FILE = "pizza5ps_prefs"
        const val KEY_API_KEY = "anthropic_api_key"
    }
}

/**
 * Shape check only — it cannot tell a valid key from a revoked one.
 *
 * Catching an obvious paste error here saves a network round trip and a confusing
 * 401; anything beyond the prefix is left to the API to judge.
 */
fun String.looksLikeAnthropicKey(): Boolean =
    trim().startsWith("sk-ant-") && trim().length >= 20
