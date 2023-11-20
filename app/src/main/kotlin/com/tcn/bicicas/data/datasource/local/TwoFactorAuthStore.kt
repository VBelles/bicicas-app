package com.tcn.bicicas.data.datasource.local

import android.content.SharedPreferences
import androidx.core.content.edit
import com.tcn.bicicas.data.model.TwoFactorAuth
import org.jasypt.util.text.BasicTextEncryptor


class TwoFactorAuthStore(
    private val preferences: SharedPreferences,
    password: CharArray
) : LocalStore<TwoFactorAuth> {

    companion object {
        private const val USER_KEY = "twoFactorAuth.user"
        private const val SECRET_KEY = "twoFactorAuth.secret"
    }

    private val encryptor = BasicTextEncryptor().apply { setPasswordCharArray(password) }

    init {
        migrate()
    }

    override fun get(): TwoFactorAuth? = runCatching {
        val user = preferences.getString(USER_KEY, null)?.let(encryptor::decrypt)
        val secret = preferences.getString(SECRET_KEY, null)?.let(encryptor::decrypt)
        TwoFactorAuth(user ?: return null, secret ?: return null)
    }.getOrNull()

    override fun save(value: TwoFactorAuth) =
        preferences.edit {
            putString(USER_KEY, encryptor.encrypt(value.user))
            putString(SECRET_KEY, encryptor.encrypt(value.secret))
        }

    override fun clear() =
        preferences.edit {
            remove(USER_KEY)
            remove(SECRET_KEY)
        }

    /**
     * Remove this on next releases
     */
    private fun migrate() = runCatching {
        val user = preferences.getString("user", null) ?: return@runCatching
        val secret = preferences.getString("secret", null) ?: return@runCatching
        preferences.edit {
            remove("user")
            remove("secret")
        }
        save(TwoFactorAuth(user, secret))
    }


}
