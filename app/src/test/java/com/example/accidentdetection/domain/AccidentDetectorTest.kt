package com.example.accidentdetection.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccidentDetectorTest {

    @Test
    fun `detects likely crash when impact rotation and sudden stop happen together`() {
        val detector = AccidentDetector()
        val start = 1_000L

        detector.process(
            DetectionSample(
                accelerationMagnitude = 27f,
                rotationMagnitude = 0.8f,
                timestampMillis = start,
            ),
        )
        detector.process(
            DetectionSample(
                accelerationMagnitude = 22f,
                rotationMagnitude = 4.3f,
                timestampMillis = start + 200,
            ),
        )
        val result = detector.process(
            DetectionSample(
                accelerationMagnitude = 4f,
                rotationMagnitude = 4.1f,
                timestampMillis = start + 400,
            ),
        )

        assertTrue(result.detected)
    }

    @Test
    fun `does not trigger on hard braking without rotation`() {
        val detector = AccidentDetector()
        val start = 2_000L

        detector.process(
            DetectionSample(
                accelerationMagnitude = 28f,
                rotationMagnitude = 0.2f,
                timestampMillis = start,
            ),
        )
        val result = detector.process(
            DetectionSample(
                accelerationMagnitude = 4f,
                rotationMagnitude = 0.3f,
                timestampMillis = start + 300,
            ),
        )

        assertFalse(result.detected)
    }

    @Test
    fun `respects cooldown to reduce repeat alerts`() {
        val detector = AccidentDetector()
        val start = 3_000L

        val first = sequence(detector, start)
        val second = sequence(detector, start + 2_000)

        assertTrue(first.detected)
        assertFalse(second.detected)
    }

    private fun sequence(detector: AccidentDetector, start: Long): DetectionResult {
        detector.process(DetectionSample(30f, 0.5f, start))
        detector.process(DetectionSample(21f, 4.5f, start + 100))
        return detector.process(DetectionSample(3f, 4.2f, start + 250))
    }
}
