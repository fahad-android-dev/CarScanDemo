package com.example.carscandemo.api_manager


import android.util.Log

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

object ApiRepository {
    inline fun <reified RESPONSE> apiGet(methodExt :String): Flow<RESPONSE> {
        return flow<RESPONSE> {
            val response = RestClient.apiService.get {
                url(WebServices.getDomainUrl() +methodExt)
            }
            emit(response.body())
        }.catch { e ->
            emit(emptyFlow<RESPONSE>().first())
            Log.e(methodExt, "Error fetching data: ${e.message}")
        }.flowOn(Dispatchers.IO)
    }

   inline fun <reified REQUEST , reified RESPONSE> apiPost (methodExt :String, model: REQUEST): Flow<RESPONSE> {
        return flow<RESPONSE> {
            val response = RestClient.apiService.post {
                url(WebServices.getDomainUrl() + methodExt)
                setBody(model)
            }
            emit(response.body())
        }.catch { e ->
            emit(emptyFlow<RESPONSE>().first())
            Log.e(methodExt, "Error fetching data: ${e.message}")
        }.flowOn(Dispatchers.IO)
    }

    inline fun <reified REQUEST , reified RESPONSE> apiPostMultiPart (methodExt :String, model: REQUEST): Flow<RESPONSE> {
        return flow<RESPONSE> {
            val response = RestClient.apiService.post {
                accept(ContentType.MultiPart.FormData)
                contentType(ContentType.MultiPart.FormData)
                url(WebServices.getDomainUrl() + methodExt)
                setBody(model)
            }
            emit(response.body())
        }.catch { e ->
            emit(emptyFlow<RESPONSE>().first())
            Log.e(methodExt, "Error fetching data: ${e.message}")
        }.flowOn(Dispatchers.IO)
    }


    /** use this function only when calling api from different host */
    inline fun <reified RESPONSE> apiGetByUrl(url :String): Flow<RESPONSE> {
        return flow<RESPONSE> {
            val response = RestClient.apiService.get {
                url(url)
            }
            emit(response.body())
        }.catch { e ->
            emit(emptyFlow<RESPONSE>().first())
            Log.e(url, "Error fetching data: ${e.message}")
        }.flowOn(Dispatchers.IO)
    }

    /** use this function only when calling api from different host */
    inline fun <reified RESPONSE , reified REQUEST> apiPostByUrl(url :String , model: REQUEST): Flow<RESPONSE> {
        return flow<RESPONSE> {
            val response = RestClient.apiService.post {
                url(url)
                setBody(model)
            }
            emit(response.body())
        }.catch { e ->
            emit(emptyFlow<RESPONSE>().first())
            Log.e(url, "Error fetching data: ${e.message}")
        }.flowOn(Dispatchers.IO)
    }

}
