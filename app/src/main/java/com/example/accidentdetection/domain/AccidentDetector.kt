package com.example.accidentdetection.domain

import kotlin.math.abs

data class DetectionResult(
    val detected: Boolean,
    val reason: String? = null,
)

class AccidentDetector(
    private val impactThreshold: Float = 25f,
    private val rotationThreshold: Float = 3.5f,
    private val suddenStopThreshold: Float = 6f,
    private val jerkThreshold: Float = 12f,
    private val detectionWindowMillis: Long = 1800L,
    private val cooldownMillis: Long = 15_000L,
) {
    private var lastImpactTimestamp = 0L
    private var lastRotationTimestamp = 0L
    private var lastDetectionTimestamp = -cooldownMillis
    private var previousAcceleration = 9.8f

    fun process(sample: DetectionSample): DetectionResult {
        val jerk = abs(sample.accelerationMagnitude - previousAcceleration)
        previousAcceleration = sample.accelerationMagnitude

        if (sample.accelerationMagnitude >= impactThreshold) {
            lastImpactTimestamp = sample.timestampMillis
        }
        if (sample.rotationMagnitude >= rotationThreshold) {
            lastRotationTimestamp = sample.timestampMillis
        }

        val recentImpact = sample.timestampMillis - lastImpactTimestamp <= detectionWindowMillis
        val recentRotation = sample.timestampMillis - lastRotationTimestamp <= detectionWindowMillis
        val suddenStop = sample.accelerationMagnitude <= suddenStopThreshold
        val highJerk = jerk >= jerkThreshold
        val cooldownExpired = sample.timestampMillis - lastDetectionTimestamp > cooldownMillis

        return if (recentImpact && recentRotation && suddenStop && highJerk && cooldownExpired) {
            lastDetectionTimestamp = sample.timestampMillis
            DetectionResult(
                detected = true,
                reason = "High impact, strong rotation, and sudden stop detected",
            )
        } else {
            DetectionResult(detected = false)
        }
    }
}
