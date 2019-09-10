package net.jemzart.klox

import net.jemzart.klox.TokenType.*

internal class Scanner(private val source: String) {
	private var start = 0
	private var current = 0
	private var line = 1
	fun scanTokens(): List<Token> {
		while (!isAtEnd()) {
			// We are at the beginning of the next lexeme.
			start = current
			scanToken()
		}

		tokens.add(Token(EOF, "", null, line))
		return tokens
	}
	
	private fun isAtEnd(): Boolean {
		return current >= source.length
	}

	private fun scanToken() {
		val c = advance()
		when (c) {
			'(' -> addToken(LEFT_PAREN)
			')' -> addToken(RIGHT_PAREN)
			'{' -> addToken(LEFT_BRACE)
			'}' -> addToken(RIGHT_BRACE)
			',' -> addToken(COMMA)
			'.' -> addToken(DOT)
			'-' -> addToken(MINUS)
			'+' -> addToken(PLUS)
			';' -> addToken(SEMICOLON)
			'*' -> addToken(STAR)
			'!' -> addToken(if (match('=')) BANG_EQUAL else BANG)
			'=' -> addToken(if (match('=')) EQUAL_EQUAL else EQUAL)
			'<' -> addToken(if (match('=')) LESS_EQUAL else LESS)
			'>' -> addToken(if (match('=')) GREATER_EQUAL else GREATER)
			'"' -> string()
			'/' ->
				if (match('/')) {
					// A comment goes until the end of the line.
					while (peek() != '\n' && !isAtEnd()) advance()
				} else {
					addToken(SLASH)
				}
			' ', '\r', '\t' -> Unit
			'\n' -> line++
			else -> {
				if (c.isNumeric()) {
					number()
				} else if (c.isAlpha()) {
					identifier()
				} else {
					error(line, "Unexpected character.")
				}
			}
		}
	}
	private fun identifier() {
		while (peek().isAlphaNumeric()) advance()
		// See if the identifier is a reserved word.
		val text = source.substring(start, current)

		var type = keywords[text]
		if (type == null) type = IDENTIFIER
		addToken(type)
	}
	private fun peekNext(): Char {
		return if (current + 1 >= source.length) '\u0000' else source[current + 1]
	}

	private fun number() {
		while (peek().isNumeric()) advance()

		// Look for a fractional part.
		if (peek() == '.' && peekNext().isNumeric()) {
			// Consume the "."
			advance()

			while (peek().isNumeric()) advance()
		}

		addToken(NUMBER, source.substring(start, current).toDouble())
	}

	private fun peek(): Char {
		return if (isAtEnd()) '\u0000' else source[current]
	}

	private fun match(expected: Char): Boolean {
		if (isAtEnd()) return false
		if (source[current] != expected) return false

		current++
		return true
	}

	private fun advance(): Char {
		current++
		return source[current - 1]
	}

	private fun addToken(type: TokenType) {
		addToken(type, null)
	}

	private fun addToken(type: TokenType, literal: Any?) {
		val text = source.substring(start, current)
		tokens.add(Token(type, text, literal, line))
	}

	private fun string() {
		while (peek() != '"' && !isAtEnd()) {
			if (peek() == '\n') line++
			advance()
		}

		// Unterminated string.
		if (isAtEnd()) {
			error(line, "Unterminated string.")
			return
		}

		// The closing ".
		advance()

		// Trim the surrounding quotes.
		val value = source.substring(start + 1, current - 1)
		addToken(STRING, value)
	}

	private val tokens = mutableListOf<Token>()

	private var keywords = mapOf(
		"and" to AND,
		"class" to CLASS,
		"else" to ELSE,
		"false" to FALSE,
		"for" to FOR,
		"fun" to FUN,
		"if" to IF,
		"nil" to NIL,
		"or" to OR,
		"print" to PRINT,
		"return" to RETURN,
		"super" to SUPER,
		"this" to THIS,
		"true" to TRUE,
		"var" to VAR,
		"while" to WHILE
	)
}  