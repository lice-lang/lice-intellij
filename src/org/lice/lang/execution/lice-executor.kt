package org.lice.lang.execution

import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import org.lice.lang.*
import java.io.File

class LiceCommandLineState(
		private val configuration: LiceRunConfiguration,
		env: ExecutionEnvironment) : CommandLineState(env) {
	override fun startProcess() = OSProcessHandler(GeneralCommandLine(listOf(
			JAVA_PATH,
			"-classpath",
			arrayOf(
					configuration.jarLocation,
					KOTLIN_RUNTIME_PATH,
					KOTLIN_REFLECT_PATH
			).joinToString(File.pathSeparator),
			LICE_MAIN_DEFAULT,
			configuration.targetFile
	) + configuration
			.vmParameters
			.split(" ")
			.filter(String::isNotBlank)).apply {
		withWorkDirectory(configuration.workingDirectory)
	}).also {
		ProcessTerminatedListener.attach(it)
		it.startNotify()
	}
}
