package net.jemzart.tool

import java.io.PrintWriter


fun main(args: Array<String>) {
    if (args.size != 1) {
        System.err.println("Usage: generate_ast <output directory>")
        System.exit(1)
    }
    val outputDir = args[0]
    defineAst(outputDir, "Expr", listOf(
        "Binary : Expr left, Token operator, Expr right",
        "Grouping : Expr expression",
        "Literal : Object value",
        "Unary : Token operator, Expr right")
    )
}

private fun defineAst(outputDir: String, baseName: String, types: List<String>) {
    val path = "$outputDir/$baseName.java"
    val writer = PrintWriter(path, "UTF-8")

    writer.println("package net.jemzart.klox")
    writer.println()
    writer.println("abstract class $baseName {")

    defineVisitor(writer, baseName, types)

    // The AST classes.
    for (type in types) {
        val className = type.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].trim { it <= ' ' }
        val fields = type.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].trim { it <= ' ' }
        defineType(writer, baseName, className, fields)
    }

    // The base accept() method.
    writer.println()
    writer.println("  abstract <R> R accept(Visitor<R> visitor);")

    writer.println("}")
    writer.close()
}

private fun defineType(writer: PrintWriter, baseName: String, className: String, fieldList: String) {
    writer.println("static class $className extends $baseName {")

    // Constructor.
    writer.println("    $className($fieldList) {")

    // Store parameters in fields.
    val fields = fieldList.split((", ").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    for (field in fields) {
        val name = field.split((" ").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
        writer.println("      this.$name = $name;")
    }

    writer.println("    }")

    // Visitor pattern.
    writer.println()
    writer.println("    <R> R accept(Visitor<R> visitor) {")
    writer.println("      return visitor.visit" +
            className + baseName + "(this);")
    writer.println("    }")

    // Fields.
    writer.println()
    for (field in fields) {
        writer.println("    final $field;")
    }

    writer.println("  }")
}

private fun defineVisitor(writer: PrintWriter, baseName: String, types: List<String>) {
    writer.println("  interface Visitor<R> {")

    for (type in types) {
        val typeName = type.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].trim { it <= ' ' }
        writer.println(
            "    R visit" + typeName + baseName + "(" +
                    typeName + " " + baseName.toLowerCase() + ");"
        )
    }

    writer.println("  }")
}