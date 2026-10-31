package com.example.qrapp

import com.example.qrapp.util.QrExporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QrUtilsTest {

    @Test
    fun testUrlValidation() {
        assertTrue(QrExporter.isValidUrl("https://example.com"))
        assertTrue(QrExporter.isValidUrl("http://sub.domain.org/path?q=1"))
        assertTrue(QrExporter.isValidUrl("google.com"))
        assertTrue(QrExporter.isValidUrl("github.com/divyam"))

        assertFalse(QrExporter.isValidUrl(""))
        assertFalse(QrExporter.isValidUrl("   "))
        assertFalse(QrExporter.isValidUrl("justaword"))
    }

    @Test
    fun testUrlNormalization() {
        assertEquals("https://google.com", QrExporter.normalizeUrl("google.com"))
        assertEquals("https://google.com", QrExporter.normalizeUrl("https://google.com"))
        assertEquals("http://legacy.site", QrExporter.normalizeUrl("http://legacy.site"))
        assertEquals("https://test.io/page", QrExporter.normalizeUrl("  test.io/page  "))
    }
}
