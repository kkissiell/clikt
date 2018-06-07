package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.testing.splitArgv
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.Test

class ContextTest {
    @Test
    fun `find functions single context`() {
        class C : CliktCommand() {
            val o1 by findObject<String>()
            val o2 by findObject { "foo" }
            val o3 by findObject<String>()
            val o4 by findObject<Int>()

            override fun run() {
                context.findRoot() shouldBe context
            }
        }

        val c = C().apply { parse(emptyArray()) }

        c.o1 shouldBe null
        c.o2 shouldBe "foo"
        c.o3 shouldBe "foo"
        c.o4 shouldBe null
    }

    @Test
    fun `find functions parent context`() {
        class Foo

        val foo = Foo()

        class C : CliktCommand(invokeWithoutSubcommand = true) {
            val o1 by findObject<Foo>()
            val o2 by findObject { foo }
            val o3 by findObject<Foo>()
            val o4 by findObject<Int>()

            override fun run() {
                context.findRoot() shouldBe context
            }
        }

        val child = C()
        val parent = C().subcommands(child).apply { parse(emptyArray()) }
        parent.o1 shouldBe child.o1
        parent.o1 shouldBe null
        parent.o2 shouldBe child.o2
        parent.o2 shouldBe foo
        parent.o3 shouldBe child.o3
        parent.o3 shouldBe foo
        parent.o4 shouldBe child.o4
        parent.o4 shouldBe null
    }

    @Test
    fun `default help option names`() {
        class C : NoRunCliktCommand()

        shouldThrow<PrintHelpMessage> { C().parse(splitArgv("--help")) }
        shouldThrow<PrintHelpMessage> { C().parse(splitArgv("-h")) }
        shouldThrow<PrintHelpMessage> {
            C().context { helpOptionNames = setOf("-x") }.parse(splitArgv("-x"))
        }
        shouldThrow<NoSuchOption> {
            C().context { helpOptionNames = setOf("--x") }.parse(splitArgv("--help"))
        }
    }
}
