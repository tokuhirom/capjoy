package capjoy.command

import kotlin.test.Test

class VersionCommandTest {
    @Test
    fun testVersion() {
        val cmd = VersionCommand()
        cmd.run()
    }
}
