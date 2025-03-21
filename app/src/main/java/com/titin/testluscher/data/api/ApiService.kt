package com.titin.testluscher.data.api

import android.content.Context
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import javax.inject.Inject

class ApiService @Inject constructor(private val context: Context) {
    private val queue by lazy { Volley.newRequestQueue(context) }

    fun fetchData(url: String, key: String, onResult: (String) -> Unit, onError: (String) -> Unit) {
        val stringRequest = StringRequest(
            Request.Method.GET, "$url?key=$key",
            { response -> onResult(response) },
            { error -> onError(error.message ?: "Unknown error") }
        )
        queue.add(stringRequest)
    }
}