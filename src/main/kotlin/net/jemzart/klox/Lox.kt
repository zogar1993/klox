package net.jemzart.klox

import java.nio.charset.Charset
import java.nio.file.Paths
import java.nio.file.Files
import java.io.BufferedReader
import java.io.InputStreamReader

var hadError = false
fun main(args: Array<String>) {
	if (args.size > 1) {
		println("Usage: klox [script]")
		System.exit(64)
	} else if (args.size == 1) {
		runFile(args[0])
	} else {
		runPrompt()
	}
}

private fun runFile(path: String) {
	val bytes = Files.readAllBytes(Paths.get(path))
	run(String(bytes, Charset.defaultCharset()))
	if (hadError) System.exit(65)
}

private fun runPrompt() {
	val input = InputStreamReader(System.`in`)
	val reader = BufferedReader(input)

	while (true) {
		print("> ")
		run(reader.readLine())
		hadError = false
	}
}

private fun run(source: String) {
	val scanner = Scanner(source)
	val tokens = scanner.scanTokens()

	val parser = Parser(tokens)
	val expression = parser.parse()

	// Stop if there was a syntax error.
	if (hadError) return

	println(AstPrinter().print(expression!!))
}

fun error(line: Int, message: String) {
	report(line, "", message)
}

private fun report(line: Int, where: String, message: String) {
	System.err.println("[line $line] Error$where: $message")
	hadError = true
}

fun error(token: Token, message: String) {
	if (token.type === TokenType.EOF) {
		report(token.line, " at end", message)
	} else {
		report(token.line, " at '" + token.lexeme + "'", message)
	}
}