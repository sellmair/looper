# Looper 
A simple Looper-Thread implementation for Android

![GitHub top language](https://img.shields.io/github/languages/top/sellmair/looper.svg)
[![Build Status](https://travis-ci.org/sellmair/looper.svg?branch=develop)](https://travis-ci.org/sellmair/looper)
![Bintray](https://img.shields.io/bintray/v/sellmair/sellmair/looper.svg)


## Usage

##### gradle
```groovy
dependencies { 
    implement "io.sellmair:looper:1.0.0-RC.0"
}
```

##### Starting the thread

```kotlin
val thread = Looper.start()
```

##### Wait for startup
```kotlin
// Current thread will wait until looper thread is fully booted
thread.awaitStartup()
```

##### Executing 
```kotlin

// Dispatches a task to the looper thread
// If thread is not started: Will be queued and 
// executed once the thread started.
thread.execute {
     print("I will be executed by the looper thread")
}

```

##### Getting a Handler
```kotlin
// Will be null if the thread is not yet started
val handler = thread.handler

// Never null: Will wait for thread to start up if necessary
// Is cheap if thread is already started
val handler = thread.handler()
```

##### Stopping the thread
```kotlin
thread.quit() // will discard all pending messages
thread.quitSafely() // will process all currently pending messages first
```