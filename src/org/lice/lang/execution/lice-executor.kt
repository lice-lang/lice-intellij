package org.lice.lang.execution

import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import org.lice.lang.JAVA_PATH

class LiceCommandLineState(
		private val configuration: LiceRunConfiguration,
		env: ExecutionEnvironment) : CommandLineState(env) {
	override fun startProcess() = OSProcessHandler(GeneralCommandLine(
			listOf(
					JAVA_PATH,
					"-jar",
					configuration.jarLocation,
					configuration.targetFile
			) + configuration
					.vmParameters
					.split(" ")
					.filter(String::isNotBlank)))
			.apply { startNotify() }
}
