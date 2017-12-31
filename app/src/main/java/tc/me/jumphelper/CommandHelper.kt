package tc.me.jumphelper

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

/**
 * Created by classTC on 30/12/2017. CommandHelper
 */
object CommandHelper {

    private val CMD_SU = "su"
    private val CMD_SH = "sh"
    private val CMD_EXIT = "exit\n"
    private val CMD_LINE_END = "\n"

    data class CommandResult(val code: Int, val result: String, val error: String)

    fun exec(cmd: String, isRoot: Boolean): CommandResult {
        return exec(arrayListOf(cmd), isRoot)
    }

    fun exec(cmds: ArrayList<String>, isRoot: Boolean = true): CommandResult {
        try {
            val execProcess = Runtime.getRuntime().exec(if (isRoot) CMD_SU else CMD_SH)
            DataOutputStream(execProcess.outputStream).use {
                for (cmd in cmds) {
                    it.write(cmd.toByteArray())
                    it.write(CMD_LINE_END.toByteArray())
                    it.flush()
                }

                it.write(CMD_EXIT.toByteArray())
                it.flush()
            }

            val code = execProcess.waitFor()

            var execResult = ""
            BufferedReader(InputStreamReader(execProcess.inputStream)).use {
                it.readLines().forEach { execResult += it }
            }

            var execError = ""
            BufferedReader(InputStreamReader(execProcess.errorStream)).use {
                it.readLines().forEach { execError += it }
            }
            execProcess.destroy()
            return CommandResult(code, execResult, execError)
        } catch (e: Exception) {
            return CommandResult(-1, "", e.toString())
        }
    }
}