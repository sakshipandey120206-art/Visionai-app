package com.example.model

import java.util.UUID

data class QAMessage(
    val id: String = UUID.randomUUID().toString(),
    val question: String,
    val answer: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isCvEnriched: Boolean = false,
    val isError: Boolean = false
)
