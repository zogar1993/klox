package net.jemzart.klox

import kotlin.reflect.KFunction
import jdk.nashorn.internal.objects.NativeJSON.stringify
import kotlin.reflect.jvm.internal.impl.descriptors.ModuleDescriptor.DefaultImpls.accept
import com.sun.org.apache.xpath.internal.operations.Variable








class Interpreter : Expr.Visitor<Any?>, Stmt.Visitor<Unit> {
    private val environment = Environment()

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        evaluate(stmt.expression);
    }

    override fun visitPrintStmt(stmt: Stmt.Print) {
        val value = evaluate(stmt.expression)
        println(stringify(value))
    }

    override fun visitVarStmt(stmt: Stmt.Var) {
        var value: Any? = null
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer)
        }

        environment.define(stmt.name.lexeme, value)
    }

    override fun visitVariableExpr(expr: Expr.Variable): Any? {
        return environment[expr.name]
    }

    fun interpret(statements: List<Stmt>) {
        try {
            for (statement in statements) {
                execute(statement)
            }
        } catch (error: RuntimeError) {
            runtimeError(error)
        }
    }

    override fun visitBinaryExpr(expr: Expr.Binary): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        fun isEqual(a: Any?, b: Any?): Boolean {
            // nil is only equal to nil.
            // done for Java reasons, but not necesary in Kotlin,
            // still preserved for better portraying lox behaviour
            if (a == null && b == null) return true
            if (a == null) return false
            return a == b
        }
        return when (expr.operator.type) {
            TokenType.GREATER -> {
                checkNumberOperands(expr.operator, left, right)
                left as Double > right as Double
            }
            TokenType.GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                left as Double >= right as Double
            }
            TokenType.LESS -> {
                    checkNumberOperands(expr.operator, left, right)
                    (left as Double) < right as Double
                }
            TokenType.LESS_EQUAL -> {
                    checkNumberOperands(expr.operator, left, right)
                    left as Double <= right as Double
                }
            TokenType.MINUS -> {
                    checkNumberOperands(expr.operator, left, right)
                    left as Double - right as Double
                }
            TokenType.PLUS -> {
                if (left is Double && right is Double)
                    return left + right
                if (left is String && right is String)
                    return left + right
                throw RuntimeError(expr.operator, "Operands must be two numbers or two strings.")
            }
            TokenType.SLASH -> left as Double / right as Double
            TokenType.STAR -> left as Double * right as Double
            TokenType.BANG_EQUAL -> !isEqual(left, right);
            TokenType.EQUAL_EQUAL -> isEqual(left, right);
            else -> null //Unreachable.
        }
    }

    private fun evaluate(expr: Expr) = expr.accept(this)

    override fun visitGroupingExpr(expr: Expr.Grouping): Any? {
        return evaluate(expr.expression)
    }

    private fun execute(stmt: Stmt) {
        stmt.accept(this)
    }

    override fun visitLiteralExpr(expr: Expr.Literal): Any? {
        return expr.value
    }

    private fun isTruthy(obj: Any?): Boolean {
        if (obj == null) return false
        return if (obj is Boolean) obj else true
    }

    private fun checkNumberOperand(operator: Token, operand: Any?) {
        if (operand !is Double) throw RuntimeError(operator, "Operand must be a number.")
    }
    override fun visitUnaryExpr(expr: Expr.Unary): Any? {
        val right = evaluate(expr.right)

        return when (expr.operator.type) {
            TokenType.MINUS -> {
                checkNumberOperand(expr.operator, right)
                -(right as Double)
            }
            TokenType.BANG -> !isTruthy(right)
            else -> null //Unreachable.
        }
    }

    private fun checkNumberOperands(operator: Token, left: Any?, right: Any?) {
        if (left is Double && right is Double) return

        throw RuntimeError(operator, "Operands must be numbers.")
    }

    private fun stringify(obj: Any?): String {
        if (obj == null) return "nil"

        // Hack. Work around Java adding ".0" to integer-valued doubles.
        if (obj is Double) {
            var text = obj.toString()
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length - 2)
            }
            return text
        }

        return obj.toString()
    }
}