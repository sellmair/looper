package io.sellmair.looper.internal

import android.support.test.runner.AndroidJUnit4
import io.sellmair.looper.TestRunnable
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.concurrent.thread

@RunWith(AndroidJUnit4::class)
class StartupSafeLooperThreadTest {

    @Test
    fun messagesSent_beforeStartup_areExecuted() {
        val thread = StartupSafeLooperThread()
        val testRunnable1 = TestRunnable()
        val testRunnable2 = TestRunnable()
        val testRunnable3 = TestRunnable()
        val testRunnable4 = TestRunnable()
        thread.execute(testRunnable1)
        thread.execute(testRunnable2)
        thread.execute(testRunnable3)
        thread.execute(testRunnable4)
        thread.start()
        thread.awaitStartup()
        thread.quitSafely()
        thread.join()

        testRunnable1.assertExecutedBefore(testRunnable2)
        testRunnable2.assertExecutedBefore(testRunnable3)
        testRunnable3.assertExecutedBefore(testRunnable4)
    }

    @Test
    fun messageSent_beforeStartup_and_afterStartup_areExecuted() {
        val thread = StartupSafeLooperThread()
        val testRunnable1 = TestRunnable()
        val testRunnable2 = TestRunnable()
        val testRunnable3 = TestRunnable()
        val testRunnable4 = TestRunnable()
        val testRunnable5 = TestRunnable()
        val testRunnable6 = TestRunnable()
        val testRunnable7 = TestRunnable()
        val testRunnable8 = TestRunnable()
        thread.execute(testRunnable1)
        thread.execute(testRunnable2)
        thread.execute(testRunnable3)
        thread.execute(testRunnable4)
        thread.start()
        thread.execute(testRunnable5)
        thread.execute(testRunnable6)
        thread.awaitStartup()
        thread.execute(testRunnable7)
        thread.execute(testRunnable8)
        thread.quitSafely()
        thread.join()

        testRunnable1.assertExecutedBefore(testRunnable2)
        testRunnable2.assertExecutedBefore(testRunnable3)
        testRunnable3.assertExecutedBefore(testRunnable4)
        testRunnable4.assertExecutedBefore(testRunnable5)
        testRunnable5.assertExecutedBefore(testRunnable6)
        testRunnable6.assertExecutedBefore(testRunnable7)
        testRunnable7.assertExecutedBefore(testRunnable8)
    }

    @Test
    fun looper() {
        val thread = StartupSafeLooperThread()
        thread.start()
        val looper = thread.looper()
        thread.awaitStartup()
        Assert.assertEquals(looper, thread.looper)
        thread.quit()
    }

    @Test
    fun looper_beforeStart() {
        val looperThread = StartupSafeLooperThread()

        val assertThread = thread {
            val looper = looperThread.looper()
            Assert.assertEquals(looper, looperThread.looper)
        }

        thread {
            Thread.sleep(200)
            looperThread.start()
        }

        assertThread.join()
    }


}

