package com.pizza5ps.app.data

/** A signed-in customer. */
data class Account(
    val name: String,
    val email: String
) {
    /** First name only, for greetings where the full name would be too long. */
    val firstName: String get() = name.trim().substringBefore(' ').ifBlank { name }

    /** Up to two initials, for the avatar. */
    val initials: String
        get() = name.trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "?" }
}

/** What went wrong, so the form can say so precisely. */
sealed interface AuthResult {
    data class Success(val account: Account) : AuthResult
    data class Failure(val message: String) : AuthResult
}

/**
 * In-memory account store.
 *
 * **This is a demo credential store, not authentication.** Accounts live in a map for
 * the lifetime of the process and passwords are held in plain text, so nothing here
 * survives a restart and nothing here is secure. The app has no backend by design;
 * this exists so the sign-in flow, the greeting and the saved addresses have something
 * real to read from. Any production version would replace this wholesale with a real
 * identity provider and never hold a password itself.
 */
object AccountRepository {

    private data class Credential(val password: String, val account: Account)

    // Keyed by lower-cased email so sign-in is not case-sensitive on the address.
    private val accounts = mutableMapOf<String, Credential>()

    /** A ready-made account, so the app can be demonstrated without signing up first. */
    private val demoEmail = "maheep.student@college.edu"

    init {
        accounts[demoEmail] = Credential(
            password = "pizza123",
            account = Account(name = "Maheep Singh", email = demoEmail)
        )
    }

    /** Credentials shown on the sign-in screen so a demo needs no prior setup. */
    val demoCredentials: Pair<String, String> = demoEmail to "pizza123"

    fun signUp(name: String, email: String, password: String): AuthResult {
        val trimmedName = name.trim()
        val key = email.trim().lowercase()

        if (trimmedName.length < 2) {
            return AuthResult.Failure("Please enter your name")
        }
        if (!key.isValidEmail()) {
            return AuthResult.Failure("That doesn't look like an email address")
        }
        if (password.length < 6) {
            return AuthResult.Failure("Password needs at least 6 characters")
        }
        if (accounts.containsKey(key)) {
            return AuthResult.Failure("An account already exists for that email")
        }

        val account = Account(name = trimmedName, email = key)
        accounts[key] = Credential(password, account)
        return AuthResult.Success(account)
    }

    fun signIn(email: String, password: String): AuthResult {
        val key = email.trim().lowercase()
        val credential = accounts[key]
            ?: return AuthResult.Failure("No account found for that email")

        // A demo store, but still worth not confirming which half was wrong.
        if (credential.password != password) {
            return AuthResult.Failure("Email or password is incorrect")
        }
        return AuthResult.Success(credential.account)
    }
}

private fun String.isValidEmail(): Boolean {
    val at = indexOf('@')
    val dot = lastIndexOf('.')
    return at > 0 && dot > at + 1 && dot < length - 1 && !contains(' ')
}
