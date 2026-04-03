package com.example.accidentdetection.domain

data class DetectionSample(
    val accelerationMagnitude: Float,
    val rotationMagnitude: Float,
    val timestampMillis: Long,
)
