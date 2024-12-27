package com.example.carscandemo.api_manager

import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

interface RestClient {
    companion object {
        val apiService by lazy { create() }
        fun create(): HttpClient {
            val client = HttpClient(Android) {
                install(Logging) {
                    logger = object : Logger {
                        override fun log(message: String) {
                            Log.d("KTOR Response", message)
                        }
                    }
                    level = LogLevel.ALL
                }

                install(ContentNegotiation) {
                    json(Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        prettyPrint = true
                    })
                }
                install(HttpTimeout) {
                    requestTimeoutMillis = 15000L
                    connectTimeoutMillis = 15000L
                    socketTimeoutMillis = 15000L
                }
                // Apply to all requests
                defaultRequest {
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                }

            }
            return client
        }

    }
}