package io.sellmair.looper

import android.os.Handler
import android.os.Looper
import io.sellmair.looper.internal.StartupSafeLooperThread
import java.util.concurrent.Executor


interface QuitableExecutor : Executor {
    fun quit()
    fun quitSafely()
}


/**
 * A thread that internally owns a [Looper].
 * It implements [Executor].
 * All messages send to this thread (using [Executor.execute] will be queued
 * and executed once the thread is finished starting.
 */
interface LooperThread : QuitableExecutor {
    /**
     * Will stop the thread if it is currently running.
     * Will prevent the thread to startup if not yet booted
     * @see Looper.quit
     */
    override fun quit()

    /**
     * Will stop the thread if it is currently running.
     * Will prevent the thread to startup if not yet booted
     * @see Looper.quitSafely
     */
    override fun quitSafely()

    /**
     * A handler able to post messages to this thread.
     * This will be available once the thread started
     *
     * @see handler if you want to wait for startup if necessary.
     */
    val handler: Handler?

    /**
     * The looper associated with this thread.
     * This will be available once the thread started
     *
     * @see looper if you want to wait for startup if necessary
     */
    val looper: Looper?


    /**
     * Does nothing if the thread is already fully booted.
     * Waits for the thread to become fully booted if necessary
     */
    fun awaitStartup()


    /**
     * A handler able to post messages to this thread.
     * Will wait for thread startup if necessary
     */
    fun handler(): Handler

    /**
     * The looper associated with this thread.
     * Will wait for thread startup if necessary
     */
    fun looper(): Looper


    companion object {
        /**
         * Creates a new LooperThread and starts the thread.
         */
        fun start(): LooperThread = StartupSafeLooperThread().also(Thread::start)
    }
}


