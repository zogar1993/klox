package net.jemzart.klox

class RuntimeError(val token: Token, message: String) : RuntimeException(message)