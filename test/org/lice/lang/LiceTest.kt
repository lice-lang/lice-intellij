package org.lice.lang

import org.junit.Test
import org.lice.Lice
import org.lice.core.SymbolList
import java.nio.file.Files
import java.nio.file.Paths

class StarPlatinum {
	@Test
	fun oraOraOraOra() {
		//language=Lice
		Lice.run("((233))").let(::println)
		Lice.run("((\"666\"))").let(::println)
	}

	@Test
	fun generateStdlibStub() {
		(SymbolList.preludeSymbols.map { "(defexpr $it ()) ;; this is a stub, used for indexing" }
				+
				SymbolList.preludeVariables.map { "(-> $it ()) ;; this is a stub, used for indexing" })
				.sorted()
				.joinToString("\n")
				.let {
					println(it)
					val destination = Paths.get("./lice-stdlib.lice")
					if (!Files.exists(destination)) Files.createFile(destination)
					Files.write(destination, it.toByteArray())
				}
	}
}
