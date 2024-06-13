package capjoy

import kotlin.random.Random

fun createTempFile(): String {
    val l = Random.nextLong()
    return "/tmp/capjoy-tmp-$l"
}
