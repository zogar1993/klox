package net.jemzart.klox

fun Char.isAlpha(): Boolean {
    return this in 'a'..'z' || this in 'A'..'Z' || this == '_'
}

fun Char.isAlphaNumeric(): Boolean {
    return this.isAlpha() || this.isNumeric()
}

fun Char.isNumeric(): Boolean {
    return this in '0'..'9'
}