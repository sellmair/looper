package io.sellmair.looper.internal

import java.util.concurrent.atomic.AtomicReference

internal fun <T> atomic() = AtomicReference<T>()