package org.lice.lang

fun main(args: Array<String>) {
	try {
		throw RuntimeException("233")
	} catch (e: RuntimeException) {
		throw e
	} catch (e: Exception) {
		println("caught!")
	}
}
