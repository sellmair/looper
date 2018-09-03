package io.sellmair.looper.internal

import android.os.Handler
import android.os.Looper
import android.util.Log
import io.sellmair.looper.LooperThread
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

internal class StartupSafeLooperThread internal constructor() : Thread(), LooperThread {


    /**
     * Used to synchronize the startup phase.
     * Messages that cannot be executed (because not started yet)
     * well be queued and synchronized with this monitor
     */
    private val startupMonitor = Any()

    /**
     * Used to synchronize waiting threads in [handler] or in [looper]
     */
    private val startupAwaitLock = ReentrantLock()

    /**
     * Used to notify waiting threads in [handler] or in [looper]
     * that handler and looper are created
     */
    private val startupCondition = startupAwaitLock.newCondition()


    /**
     * Indicates whether any ongoing startup should be canceled.
     *  Does not affect any running thread
     */
    private val startupCanceled = AtomicBoolean(false)

    /**
     * All messages that cannot be queued to the looper, because the thread is not started.
     * Those messages will be executed once the thread finished starting
     */
    private val startupPending = mutableListOf<Runnable>()

    /**
     * The looper of the thread.
     * Will be available once the thread booted
     */
    override var looper: Looper? by atomic()
        private set

    /**
     * A handler for this thread
     * Will be available once the thread booted
     */
    override var handler: Handler? by atomic()
        private set


    override fun awaitStartup() {
        /*
        Startup was finished if a handler was set
         */
        if (handler != null) {
            return
        }

        /*
        If startup was canceled: Cannot await startup anymor
         */
        if (startupCanceled.get()) throw IllegalStateException("" +
            "Startup was canceled")


        /*
        Enter the startup await lock
         */
        startupAwaitLock.withLock {

            /*
            Re-Check if handler is already set
             */
            val lockedHandler = this.handler
            if (lockedHandler != null) return

            /*
            Wait for the startup to finish
             */
            startupCondition.await()
        }
    }

    override fun handler(): Handler {
        val handler = this.handler
        if (handler != null) return handler
        this.awaitStartup()
        return this.handler ?: throw IllegalStateException("" +
            "LooperThread started, but handler is still missing")
    }

    override fun looper(): Looper {
        val looper = this.looper
        if (looper != null) return looper
        this.awaitStartup()

        return this.looper ?: throw IllegalStateException("" +
            "Looper Thread started, but looper is still missing")
    }

    override fun run() {
        Looper.prepare()


        /*
        It is possible, that messages are sent to this thread before the thread
        is already started. Those messages will be queued and synchronized with this
        monitor
         */
        synchronized(startupMonitor) {
            if (startupCanceled.get()) {
                Log.i("LooperThread", "$name: Startup canceled")
            }


            val handler = Handler(Looper.myLooper())

            /*
            Execute all pending messages
             */
            for (pending in startupPending) {
                handler.post(pending)
            }

            /*
            Remove all pending messages.
            This is mandatory for not causing memory leaks!
             */
            startupPending.clear()

            /*
            Assign looper and handler AFTER pending messages where processed.
            Assigning them before could lead to messages being send to the handler
            while posting pending messages (which then would violate the order)
             */
            this.looper = Looper.myLooper()
            this.handler = handler
        }

        startupAwaitLock.withLock {
            startupCondition.signalAll()
        }

        /*
        Run the actual loop
         */
        Looper.loop()
    }

    @Suppress("NAME_SHADOWING")
    override fun execute(command: Runnable?) {
        if (command == null) return

        /*
        Fix the handler to support smart casts
         */
        val handler = this.handler

        /*
        If a handler exists: Thread is booted.
        Message is queued. Nothing left to do
         */
        if (handler != null) {
            handler.post(command)
            return
        }

        /*
        Enter the startup monitor to ensure proper synchronization
         */
        synchronized(startupMonitor) {

            /*
            Check again if the thread is booted (by checking if a handler exists.
            (Double checked locking)
            If so: enqueue the message and be happy!
             */
            val handler = this.handler
            if (handler != null) {
                handler.post(command)
                return
            }

            /*
            If not yet booted: Add the message to the pending messages and wait for
            startup of the thread.
             */
            startupPending.add(command)
        }
    }

    /**
     * Will stop the thread if it is currently running.
     * Will prevent the thread to startup if not yet booted
     * @see Looper.quit
     */
    override fun quit() {
        synchronized(startupMonitor) {
            startupCanceled.set(true)
            looper?.quit()
        }
    }

    /**
     * Will stop the thread if it is currently running.
     * Will prevent the thread to startup if not yet booted
     * @see Looper.quitSafely
     */
    override fun quitSafely() {
        synchronized(startupMonitor) {
            startupCanceled.set(true)
            looper?.quitSafely()
        }
    }
}