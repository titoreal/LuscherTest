package com.titin.testpersonalidad.data.repository

import android.util.Log
import com.titin.testpersonalidad.data.api.ApiService
import com.titin.testpersonalidad.model.TestResult
import com.titin.testpersonalidad.utils.Constants
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class TestRepository @Inject constructor(private val apiService: ApiService) {
    // Mapa para la conversión de letras a números - más eficiente que un when
    private val letterToNumberMap = mapOf(
        "A" to "0", "B" to "1", "C" to "2", "D" to "3",
        "E" to "4", "F" to "5", "G" to "6", "H" to "7"
    )

    // Función para convertir letras a números
    private fun letterToNumber(letter: String): String {
        return letterToNumberMap[letter] ?: "0"
    }

    // Función para parsear la respuesta JSON
    private fun parseJsonResponse(response: String): String {
        return try {
            when {
                response.trim().startsWith("[") -> {
                    val jsonArray = JSONArray(response)
                    if (jsonArray.length() > 0) {
                        jsonArray.getJSONObject(0).optString("value", response)
                    } else {
                        response
                    }
                }
                else -> {
                    val jsonObject = JSONObject(response)
                    jsonObject.optString("value", response)
                }
            }
        } catch (e: Exception) {
            Log.e("JsonParse", "Error parsing JSON response: ${e.message}")
            response
        }
    }

    // Usar corrutinas para manejar la asincronía
    suspend fun getTestResult(orderSelection: List<String>): Result<TestResult> {
        return try {
            if (orderSelection.size < 4) {
                return Result.failure(Exception("No hay suficientes selecciones"))
            }

            val num0 = letterToNumber(orderSelection[0])
            val num1 = letterToNumber(orderSelection[1])
            val num2 = letterToNumber(orderSelection[2])
            val num3 = letterToNumber(orderSelection[3])

            val keyStrP = "p$num0$num1"
            val keyStrX = "x$num2$num3"

            val personalityResponse = suspendCoroutine<String> { continuation ->
                apiService.fetchData(
                    Constants.URL_P, keyStrP,
                    { responseP -> continuation.resume(parseJsonResponse(responseP)) },
                    { errorP -> continuation.resumeWithException(Exception(errorP)) }
                )
            }

            val stateResponse = suspendCoroutine<String> { continuation ->
                apiService.fetchData(
                    Constants.URL_X, keyStrX,
                    { responseX -> continuation.resume(parseJsonResponse(responseX)) },
                    { errorX -> continuation.resumeWithException(Exception(errorX)) }
                )
            }

            Result.success(TestResult(personalityResponse, stateResponse))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}