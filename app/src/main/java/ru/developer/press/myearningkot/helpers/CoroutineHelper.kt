package ru.developer.press.myearningkot.helpers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*


private fun coroutine(dispatcher: CoroutineDispatcher, run: suspend () -> Unit) =
    CoroutineScope(dispatcher).launch { run.invoke() }

fun runOnDefault(run: suspend () -> Unit) = coroutine(Dispatchers.Default) { run.invoke() }

fun runOnIO(run: suspend () -> Unit) = coroutine(Dispatchers.IO) { run.invoke() }

fun runOnMaim(run: suspend () -> Unit) = coroutine(Dispatchers.Main) { run.invoke() }

fun ViewModel.runOnViewModel(run: suspend () -> Unit) = viewModelScope.launch { run.invoke() }

suspend fun <T> main(block: suspend () -> T): T = withContext(Dispatchers.Main) { block.invoke() }

suspend fun <T> io(block: suspend () -> T): T = withContext(Dispatchers.IO) { block.invoke() }
