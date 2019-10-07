package net.jemzart.klox

internal class Environment {
    private val values = mutableMapOf<String, Any?>()
    fun define(name: String, value: Any?) {
        values[name] = value
    }
    operator fun get(name: Token): Any? {
        if (values.containsKey(name.lexeme)) {
            return values[name.lexeme]
        }

        throw RuntimeError(
            name,
            "Undefined variable '" + name.lexeme + "'."
        )
    }
} 