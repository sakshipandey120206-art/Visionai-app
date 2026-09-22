package com.example.model

data class BackendStatus(
    val isConnected: Boolean = false,
    val isChecking: Boolean = false,
    val baseUrl: String = "http://10.0.2.2:8000",
    val lastChecked: Long = 0,
    val libraries: Map<String, String> = emptyMap(),
    val device: String = "Unknown",
    val errorMessage: String? = null
)
