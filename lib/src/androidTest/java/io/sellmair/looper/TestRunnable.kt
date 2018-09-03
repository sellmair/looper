package io.sellmair.looper

import junit.framework.TestCase.fail
import org.junit.Assert

class TestRunnable : Runnable {

    private val executions = mutableListOf<Long>()

    override fun run(): Unit = synchronized(this) {
        executions.add(System.nanoTime())
    }

    fun executionCount() = synchronized(this) {
        executions.count()
    }

    fun wasExecuted() = synchronized(this) {
        executionCount() > 0
    }

    fun assertExecutedBefore(other: TestRunnable) = synchronized(this) {
        if (this.executions.isEmpty()) {
            fail("this: Not executed")
        }

        if (this.executions.size > 1) {
            fail("this: Executed multiple times")
        }

        if (other.executions.isEmpty()) {
            fail("other: Not executed")
        }

        if (other.executions.size > 1) {
            fail("other: Executed multiple times")
        }

        Assert.assertTrue("Not executed before", this.executions.first() < other.executions.first())
    }
}



