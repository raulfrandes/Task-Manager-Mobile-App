package com.example.androidclient.todo.data.remote

import android.util.Log
import com.example.androidclient.core.TAG
import com.example.androidclient.core.data.remote.Api
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class TaskWsClient(private val okHttpClient: OkHttpClient) {
    private lateinit var webSocket: WebSocket

    suspend fun openSocket(
        onEvent: (taskEvent: TaskEvent?) -> Unit,
        onClosed: () -> Unit,
        onFailure: () -> Unit
    ) {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "openSocket")
            val request = Request.Builder().url(Api.WS_URL).build()
            webSocket = okHttpClient.newWebSocket(
                request,
                TaskWebSocketListener(onEvent = onEvent, onClosed = onClosed, onFailure = onFailure)
            )
            okHttpClient.dispatcher.executorService.shutdown()
        }
    }

    fun closeSocket() {
        Log.d(TAG, "closeSocket")
        webSocket.close(1000, "")
    }

    inner class TaskWebSocketListener(
        private val onEvent: (taskEvent: TaskEvent?) -> Unit,
        private val onClosed: () -> Unit,
        private val onFailure: () -> Unit
    ) : WebSocketListener() {
        private val moshi = Moshi.Builder().build()
        private val taskEventJsonAdapter: JsonAdapter<TaskEvent> =
            moshi.adapter(TaskEvent::class.java)

        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "onOpen")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d(TAG, "onMessage string $text")
            val taskEvent = taskEventJsonAdapter.fromJson(text)
            Log.d(TAG, "onMessage taskEvent $taskEvent")
            onEvent(taskEvent)
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            Log.d(TAG, "onMessage bytes $bytes")
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {}

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "onClosed bytes $code $reason")
            onClosed()
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.d(TAG, "onFailure $t")
            onFailure()
        }
    }

    fun authorize(token: String) {
        val auth = """
            {
              "type": "authorization",
              "token": "$token"
            }
        """.trimIndent()
        Log.d(TAG, "auth $auth")
        webSocket.send(auth)
    }
}