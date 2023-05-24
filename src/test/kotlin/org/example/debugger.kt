package org.example

import org.junit.jupiter.api.Test

class DebuggerDemo {


    @Test
    fun traceCurrentStreamChain() {
        val strings = listOf("aaaa", "bbbbb", "cccc", "dddd").asSequence()
        val lines = strings.filter { it.length > 3 }.filter { !it.startsWith("a") }.map { User(it) }.toList()
        println(lines)
    }

}

