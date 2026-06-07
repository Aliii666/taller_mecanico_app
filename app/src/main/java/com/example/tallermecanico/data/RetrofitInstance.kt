package com.example.tallermecanico.data

import com.example.tallermecanico.model.PaginatedResponse
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

/**
 * Deserializador inteligente para PaginatedResponse.
 * Si el servidor devuelve un listado plano [...], lo encapsula automáticamente.
 * Si el servidor devuelve un objeto paginado de Django {"results": [...]}, lo procesa normalmente.
 */
class PaginatedResponseDeserializer : JsonDeserializer<PaginatedResponse<*>> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): PaginatedResponse<*> {
        if (json.isJsonArray) {
            val itemType = (typeOfT as ParameterizedType).actualTypeArguments[0]
            val listType = TypeToken.getParameterized(List::class.java, itemType).type
            val list: List<Any> = context.deserialize(json, listType)
            return PaginatedResponse(
                count = list.size,
                next = null,
                previous = null,
                results = list
            )
        } else if (json.isJsonObject) {
            val jsonObject = json.asJsonObject
            val count = jsonObject.get("count")?.let { if (it.isJsonNull) 0 else it.asInt } ?: 0
            val next = jsonObject.get("next")?.let { if (it.isJsonNull) null else it.asString }
            val previous = jsonObject.get("previous")?.let { if (it.isJsonNull) null else it.asString }
            val resultsElement = jsonObject.get("results")
            
            val itemType = (typeOfT as ParameterizedType).actualTypeArguments[0]
            val listType = TypeToken.getParameterized(List::class.java, itemType).type
            val results: List<Any> = if (resultsElement != null && !resultsElement.isJsonNull && resultsElement.isJsonArray) {
                context.deserialize(resultsElement, listType)
            } else {
                emptyList()
            }
            return PaginatedResponse(
                count = count,
                next = next,
                previous = previous,
                results = results
            )
        }
        throw JsonParseException("Tipo de JSON inesperado: $json")
    }
}

object RetrofitInstance {

    internal const val BASE_URL = "https://mazsorra-taller.uaeftt-ute.site/"

    private var _sessionManager: SessionManager? = null

    fun init(sessionManager: SessionManager) {
        _sessionManager = sessionManager
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient by lazy {
        requireNotNull(_sessionManager) {
            "RetrofitInstance.init(sessionManager) no fue llamado. Hazlo en TallerApp.onCreate()."
        }
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(_sessionManager!!))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val gson = GsonBuilder()
        .registerTypeAdapter(PaginatedResponse::class.java, PaginatedResponseDeserializer())
        .create()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}
