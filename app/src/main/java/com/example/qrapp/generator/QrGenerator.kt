package com.example.qrapp.generator

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.example.qrapp.model.QrCustomizationConfig
import com.example.qrapp.model.QrDotShape
import com.example.qrapp.model.QrEyeStyle
import com.example.qrapp.model.QrLogoType
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object QrGenerator {

    fun generateQrBitmap(
        content: String,
        config: QrCustomizationConfig,
        targetSize: Int = 1024
    ): Bitmap {
        val hints = mutableMapOf<EncodeHintType, Any>()
        hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
        // If logo is enabled, use Level H (30% error correction), otherwise Level Q (25%)
        hints[EncodeHintType.ERROR_CORRECTION] = if (config.logoType != QrLogoType.None) {
            ErrorCorrectionLevel.H
        } else {
            ErrorCorrectionLevel.Q
        }
        hints[EncodeHintType.MARGIN] = 1

        val bitMatrix = try {
            QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 0, 0, hints)
        } catch (e: Exception) {
            // Fallback default matrix
            QRCodeWriter().encode("https://example.com", BarcodeFormat.QR_CODE, 0, 0, hints)
        }

        val matrixWidth = bitMatrix.width
        val matrixHeight = bitMatrix.height

        val bitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw background
        canvas.drawColor(config.backgroundColor)

        val padding = targetSize * 0.05f
        val drawableArea = targetSize - (padding * 2)
        val moduleSize = drawableArea / matrixWidth

        val fgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = config.foregroundColor
            style = Paint.Style.FILL
        }

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = config.backgroundColor
            style = Paint.Style.FILL
        }

        val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = config.eyeColor ?: config.foregroundColor
            style = Paint.Style.FILL
        }

        // Finder patterns location:
        // Top-left: (1..7, 1..7) modules when margin is 1
        val margin = 1
        val tlEyeX = margin
        val tlEyeY = margin

        val trEyeX = matrixWidth - margin - 7
        val trEyeY = margin

        val blEyeX = margin
        val blEyeY = matrixHeight - margin - 7

        fun isInFinderPattern(x: Int, y: Int): Boolean {
            val inTL = x in tlEyeX until (tlEyeX + 7) && y in tlEyeY until (tlEyeY + 7)
            val inTR = x in trEyeX until (trEyeX + 7) && y in trEyeY until (trEyeY + 7)
            val inBL = x in blEyeX until (blEyeX + 7) && y in blEyeY until (blEyeY + 7)
            return inTL || inTR || inBL
        }

        // Calculate center logo exclusion zone if logo is active
        val centerModuleX = matrixWidth / 2f
        val centerModuleY = matrixHeight / 2f
        val logoRadiusModules = if (config.logoType != QrLogoType.None) matrixWidth * 0.14f else 0f

        fun isInCenterLogoZone(x: Int, y: Int): Boolean {
            if (config.logoType == QrLogoType.None) return false
            val dx = (x + 0.5f) - centerModuleX
            val dy = (y + 0.5f) - centerModuleY
            return (dx * dx + dy * dy) <= (logoRadiusModules * logoRadiusModules)
        }

        // 1. Draw Finder Patterns with custom eye style
        drawFinderPattern(canvas, tlEyeX, tlEyeY, moduleSize, padding, config.eyeStyle, eyePaint, bgPaint)
        drawFinderPattern(canvas, trEyeX, trEyeY, moduleSize, padding, config.eyeStyle, eyePaint, bgPaint)
        drawFinderPattern(canvas, blEyeX, blEyeY, moduleSize, padding, config.eyeStyle, eyePaint, bgPaint)

        // 2. Draw normal data modules
        for (y in 0 until matrixHeight) {
            for (x in 0 until matrixWidth) {
                if (isInFinderPattern(x, y)) continue
                if (isInCenterLogoZone(x, y)) continue

                if (bitMatrix.get(x, y)) {
                    val left = padding + x * moduleSize
                    val top = padding + y * moduleSize
                    val right = left + moduleSize
                    val bottom = top + moduleSize

                    when (config.dotShape) {
                        QrDotShape.Square -> {
                            canvas.drawRect(left, top, right, bottom, fgPaint)
                        }
                        QrDotShape.Circle -> {
                            val cx = left + moduleSize / 2f
                            val cy = top + moduleSize / 2f
                            val r = moduleSize * 0.42f
                            canvas.drawCircle(cx, cy, r, fgPaint)
                        }
                        QrDotShape.RoundedSquare -> {
                            val rect = RectF(
                                left + moduleSize * 0.05f,
                                top + moduleSize * 0.05f,
                                right - moduleSize * 0.05f,
                                bottom - moduleSize * 0.05f
                            )
                            val corner = moduleSize * 0.35f
                            canvas.drawRoundRect(rect, corner, corner, fgPaint)
                        }
                    }
                }
            }
        }

        // 3. Draw Center Logo Badge if enabled
        if (config.logoType != QrLogoType.None) {
            val centerPixelX = targetSize / 2f
            val centerPixelY = targetSize / 2f
            val badgeRadius = logoRadiusModules * moduleSize * 1.05f

            // Badge Background
            val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = config.logoBadgeColor
                style = Paint.Style.FILL
            }
            canvas.drawCircle(centerPixelX, centerPixelY, badgeRadius, badgePaint)

            // Badge Border
            val badgeStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = config.foregroundColor
                style = Paint.Style.STROKE
                strokeWidth = moduleSize * 0.5f
            }
            canvas.drawCircle(centerPixelX, centerPixelY, badgeRadius, badgeStrokePaint)

            // Draw Icon inside badge
            val iconSize = badgeRadius * 1.15f
            drawLogoIcon(
                canvas,
                config.logoType,
                centerPixelX,
                centerPixelY,
                iconSize,
                config.foregroundColor
            )
        }

        return bitmap
    }

    private fun drawFinderPattern(
        canvas: Canvas,
        moduleX: Int,
        moduleY: Int,
        moduleSize: Float,
        padding: Float,
        style: QrEyeStyle,
        eyePaint: Paint,
        bgPaint: Paint
    ) {
        val left = padding + moduleX * moduleSize
        val top = padding + moduleY * moduleSize
        val size = 7 * moduleSize
        val right = left + size
        val bottom = top + size

        when (style) {
            QrEyeStyle.Square -> {
                // Outer 7x7
                canvas.drawRect(left, top, right, bottom, eyePaint)
                // Middle cutout 5x5
                canvas.drawRect(
                    left + moduleSize,
                    top + moduleSize,
                    right - moduleSize,
                    bottom - moduleSize,
                    bgPaint
                )
                // Center 3x3
                canvas.drawRect(
                    left + 2 * moduleSize,
                    top + 2 * moduleSize,
                    right - 2 * moduleSize,
                    bottom - 2 * moduleSize,
                    eyePaint
                )
            }
            QrEyeStyle.Rounded -> {
                val outerCorner = 2.0f * moduleSize
                val innerCorner = 1.2f * moduleSize
                val centerCorner = 1.0f * moduleSize

                val outerRect = RectF(left, top, right, bottom)
                canvas.drawRoundRect(outerRect, outerCorner, outerCorner, eyePaint)

                val midRect = RectF(
                    left + moduleSize,
                    top + moduleSize,
                    right - moduleSize,
                    bottom - moduleSize
                )
                canvas.drawRoundRect(midRect, innerCorner, innerCorner, bgPaint)

                val centerRect = RectF(
                    left + 2 * moduleSize,
                    top + 2 * moduleSize,
                    right - 2 * moduleSize,
                    bottom - 2 * moduleSize
                )
                canvas.drawRoundRect(centerRect, centerCorner, centerCorner, eyePaint)
            }
            QrEyeStyle.Circle -> {
                val cx = left + size / 2f
                val cy = top + size / 2f

                // Outer circle
                canvas.drawCircle(cx, cy, 3.5f * moduleSize, eyePaint)
                // Middle cutout circle
                canvas.drawCircle(cx, cy, 2.5f * moduleSize, bgPaint)
                // Inner solid circle
                canvas.drawCircle(cx, cy, 1.5f * moduleSize, eyePaint)
            }
        }
    }

    private fun drawLogoIcon(
        canvas: Canvas,
        logoType: QrLogoType,
        cx: Float,
        cy: Float,
        size: Float,
        tintColor: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = tintColor
            style = Paint.Style.STROKE
            strokeWidth = size * 0.12f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = tintColor
            style = Paint.Style.FILL
        }

        val r = size / 2f

        when (logoType) {
            QrLogoType.Web -> {
                // Globe outline
                canvas.drawCircle(cx, cy, r * 0.85f, paint)
                // Equator
                canvas.drawLine(cx - r * 0.85f, cy, cx + r * 0.85f, cy, paint)
                // Central vertical ellipse
                val oval = RectF(cx - r * 0.45f, cy - r * 0.85f, cx + r * 0.45f, cy + r * 0.85f)
                canvas.drawOval(oval, paint)
            }
            QrLogoType.Heart -> {
                val path = Path()
                val topY = cy - r * 0.6f
                val bottomY = cy + r * 0.75f
                path.moveTo(cx, cy - r * 0.1f)
                path.cubicTo(
                    cx - r * 0.9f, topY - r * 0.3f,
                    cx - r * 0.9f, cy + r * 0.2f,
                    cx, bottomY
                )
                path.cubicTo(
                    cx + r * 0.9f, cy + r * 0.2f,
                    cx + r * 0.9f, topY - r * 0.3f,
                    cx, cy - r * 0.1f
                )
                path.close()
                canvas.drawPath(path, fillPaint)
            }
            QrLogoType.Star -> {
                val path = Path()
                val points = 5
                val outerR = r * 0.85f
                val innerR = outerR * 0.45f
                var angle = -Math.PI / 2
                val angleStep = Math.PI / points

                for (i in 0 until (points * 2)) {
                    val currR = if (i % 2 == 0) outerR else innerR
                    val px = (cx + currR * Math.cos(angle)).toFloat()
                    val py = (cy + currR * Math.sin(angle)).toFloat()
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                    angle += angleStep
                }
                path.close()
                canvas.drawPath(path, fillPaint)
            }
            QrLogoType.Share -> {
                val nodeR = r * 0.2f
                val p1x = cx - r * 0.45f
                val p1y = cy
                val p2x = cx + r * 0.45f
                val p2y = cy - r * 0.45f
                val p3x = cx + r * 0.45f
                val p3y = cy + r * 0.45f

                canvas.drawLine(p1x, p1y, p2x, p2y, paint)
                canvas.drawLine(p1x, p1y, p3x, p3y, paint)

                canvas.drawCircle(p1x, p1y, nodeR, fillPaint)
                canvas.drawCircle(p2x, p2y, nodeR, fillPaint)
                canvas.drawCircle(p3x, p3y, nodeR, fillPaint)
            }
            QrLogoType.Link -> {
                // Two interlocking chain links
                val linkW = r * 0.7f
                val linkH = r * 0.4f
                val linkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = tintColor
                    style = Paint.Style.STROKE
                    strokeWidth = size * 0.15f
                    strokeCap = Paint.Cap.ROUND
                }
                val rect1 = RectF(cx - linkW * 0.8f, cy - linkH / 2f, cx + linkW * 0.1f, cy + linkH / 2f)
                val rect2 = RectF(cx - linkW * 0.1f, cy - linkH / 2f, cx + linkW * 0.8f, cy + linkH / 2f)
                canvas.drawRoundRect(rect1, linkH / 2f, linkH / 2f, linkPaint)
                canvas.drawRoundRect(rect2, linkH / 2f, linkH / 2f, linkPaint)
            }
            QrLogoType.None -> {
                // No logo
            }
        }
    }
}
