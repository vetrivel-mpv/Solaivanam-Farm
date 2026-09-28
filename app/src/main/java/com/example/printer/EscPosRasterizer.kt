package com.example.printer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import com.example.data.local.OrderEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object EscPosRasterizer {
    const val PRINTER_DOT_WIDTH = 384 // Standard 58mm thermal printer dot width (48mm @ 203 DPI)

    /**
     * Renders a complete bilingual (English + Tamil) receipt onto a 384-pixel wide Canvas Bitmap.
     * This achieves flawless Tamil typography without requiring Tamil font ROM on the POS printer!
     */
    fun renderReceiptBitmap(order: OrderEntity): Bitmap {
        val width = PRINTER_DOT_WIDTH

        // First pass: measure height dynamically
        val measurePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            textSize = 18f
        }

        // Base height calculation:
        // Header ~ 160px, Order info ~ 140px, Items ~ 45px per item, Total block ~ 140px, Footer ~ 100px, Padding ~ 60px
        val estimatedHeight = 600 + (order.items.size * 52)

        val bitmap = Bitmap.createBitmap(width, estimatedHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textSize = 19f
        }

        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textSize = 21f
        }

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textSize = 27f
            textAlign = Paint.Align.CENTER
        }

        val subTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textSize = 17f
            textAlign = Paint.Align.CENTER
        }

        val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            textSize = 15f
            textAlign = Paint.Align.CENTER
        }

        val linePaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }

        val dashedLinePaint = Paint().apply {
            color = Color.GRAY
            strokeWidth = 1.5f
            style = Paint.Style.STROKE
            pathEffect = DashPathEffect(floatArrayOf(6f, 4f), 0f)
        }

        var y = 34f
        val centerX = width / 2f
        val leftX = 14f
        val rightX = width - 14f

        // --- 1. HEADER ---
        canvas.drawText("🌱 சோலைவனம் 🌱", centerX, y, titlePaint)
        y += 28f
        canvas.drawText("SOLAIVANAM ORGANIC FARM", centerX, y, subTitlePaint)
        y += 22f
        canvas.drawText("இயற்கை வேளாண்மை • நேரடிப் பண்ணை வரவு", centerX, y, smallPaint)
        y += 20f
        canvas.drawText("Farm Fresh Organic Store • Chennai", centerX, y, smallPaint)
        y += 18f
        canvas.drawText("Phone: +91 98401 23456 | FSSAI: 22421000019", centerX, y, smallPaint)
        y += 20f

        // Divider
        canvas.drawLine(leftX, y, rightX, y, linePaint)
        y += 22f

        // --- 2. ORDER DETAILS ---
        val dateFormat = SimpleDateFormat("dd/MM/yyyy  hh:mm a", Locale.getDefault())
        val dateStr = dateFormat.format(Date(order.orderTimestamp))

        canvas.drawText("INVOICE: ${order.invoiceNo}", leftX, y, boldPaint)
        y += 22f
        canvas.drawText("DATE: $dateStr", leftX, y, smallPaint.apply { textAlign = Paint.Align.LEFT })
        y += 22f
        canvas.drawText("குடியிருப்பு / Apt: ${order.apartment}", leftX, y, boldPaint.apply { textSize = 18f })
        y += 22f
        canvas.drawText("வாடிக்கையாளர்: ${order.customerName}", leftX, y, textPaint)
        y += 20f
        canvas.drawText("Door / Flat: ${order.flatNo} | Ph: ${order.customerPhone}", leftX, y, textPaint)
        y += 20f
        if (order.notes.isNotBlank()) {
            canvas.drawText("குறிப்பு: ${order.notes}", leftX, y, smallPaint)
            y += 20f
        }

        // Table Header
        canvas.drawLine(leftX, y, rightX, y, dashedLinePaint)
        y += 20f
        canvas.drawText("பொருள் (Produce)", leftX, y, boldPaint.apply { textSize = 16f })
        val priceHeader = "அளவு x விலை     மொத்தம்"
        val priceHWidth = boldPaint.measureText(priceHeader)
        canvas.drawText(priceHeader, rightX - priceHWidth, y, boldPaint)
        y += 12f
        canvas.drawLine(leftX, y, rightX, y, dashedLinePaint)
        y += 22f

        // --- 3. ITEMS LIST ---
        order.items.forEachIndexed { index, item ->
            // Line 1: Tamil Name + English Name
            val itemTitle = "${index + 1}. ${item.nameTa} (${item.nameEn})"
            canvas.drawText(itemTitle, leftX, y, boldPaint.apply { textSize = 17f })
            y += 20f

            // Line 2: Qty x UnitPrice | Total
            val qtyStr = "   ${item.quantity} ${item.unit} x ₹${"%.2f".format(item.unitPrice)}"
            val amountStr = "₹${"%.2f".format(item.totalAmount)}"
            canvas.drawText(qtyStr, leftX, y, textPaint.apply { textSize = 16f })
            val amtWidth = boldPaint.measureText(amountStr)
            canvas.drawText(amountStr, rightX - amtWidth, y, boldPaint.apply { textSize = 17f })
            y += 24f
        }

        // --- 4. TOTALS & SUMMARY ---
        canvas.drawLine(leftX, y, rightX, y, dashedLinePaint)
        y += 22f

        val subtotalLabel = "பொருட்கள் மொத்தம் (Subtotal):"
        val subtotalVal = "₹${"%.2f".format(order.subtotal)}"
        canvas.drawText(subtotalLabel, leftX, y, textPaint.apply { textSize = 16f })
        canvas.drawText(subtotalVal, rightX - textPaint.measureText(subtotalVal), y, textPaint)
        y += 22f

        val deliveryLabel = "டெலிவரி கட்டணம் (Delivery):"
        val deliveryVal = if (order.deliveryFee == 0.0) "இலவசம் (FREE)" else "₹${"%.2f".format(order.deliveryFee)}"
        canvas.drawText(deliveryLabel, leftX, y, textPaint)
        canvas.drawText(deliveryVal, rightX - textPaint.measureText(deliveryVal), y, textPaint)
        y += 14f

        canvas.drawLine(leftX, y, rightX, y, linePaint)
        y += 24f

        val totalLabel = "மொத்த தொகை (TOTAL):"
        val totalVal = "₹${"%.2f".format(order.totalAmount)}"
        val largeBold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textSize = 21f
        }
        canvas.drawText(totalLabel, leftX, y, largeBold)
        canvas.drawText(totalVal, rightX - largeBold.measureText(totalVal), y, largeBold)
        y += 24f

        val payText = "கட்டண நிலை: ${order.paymentMode}"
        canvas.drawText(payText, leftX, y, textPaint.apply { textSize = 15f })
        y += 20f

        canvas.drawLine(leftX, y, rightX, y, linePaint)
        y += 24f

        // --- 5. FOOTER & TAMIL SLOGAN ---
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textSize = 16f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("🌱 இயற்கை விவசாயத்தை ஆதரிப்போம்! 🌱", centerX, y, footerPaint)
        y += 20f
        canvas.drawText("நஞ்சில்லா உணவே நம் ஆரோக்கியம்", centerX, y, smallPaint.apply { textAlign = Paint.Align.CENTER })
        y += 20f
        canvas.drawText("Thank you for choosing Solaivanam Organic!", centerX, y, smallPaint)
        y += 24f

        // Trim bitmap to exact content height
        val finalHeight = y.toInt() + 20
        return Bitmap.createBitmap(bitmap, 0, 0, width, finalHeight)
    }

    /**
     * Converts a rendered Bitmap into standard ESC/POS GS v 0 1-bit raster commands.
     * This is the universal protocol for 58mm / 80mm thermal receipt printers.
     */
    fun bitmapToEscPosRasterBytes(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val widthBytes = (width + 7) / 8 // 384 / 8 = 48 bytes per line

        // Total raster data size
        val rasterDataSize = widthBytes * height

        // ESC/POS GS v 0 header:
        // 0x1D, 0x76, 0x30, 0x00, xL, xH, yL, yH
        val xL = (widthBytes and 0xFF).toByte()
        val xH = ((widthBytes shr 8) and 0xFF).toByte()
        val yL = (height and 0xFF).toByte()
        val yH = ((height shr 8) and 0xFF).toByte()

        // Init printer + align center + raster command + feed lines
        val initCmd = byteArrayOf(
            0x1B, 0x40, // ESC @ (Initialize printer)
            0x1B, 0x61, 0x01 // ESC a 1 (Center alignment)
        )

        val headerCmd = byteArrayOf(
            0x1D, 0x76, 0x30, 0x00, // GS v 0 0 (Raster bit image normal mode)
            xL, xH,
            yL, yH
        )

        val rasterBytes = ByteArray(rasterDataSize)
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        var byteIndex = 0
        for (y in 0 until height) {
            for (xb in 0 until widthBytes) {
                var currentByte = 0
                for (bit in 0 until 8) {
                    val x = xb * 8 + bit
                    if (x < width) {
                        val pixel = pixels[y * width + x]
                        // Luminance calculation
                        val r = (pixel shr 16) and 0xFF
                        val g = (pixel shr 8) and 0xFF
                        val b = pixel and 0xFF
                        val gray = (r * 299 + g * 587 + b * 114) / 1000
                        // In thermal printing: 1 = black (burn dot), 0 = white (skip)
                        if (gray < 160) {
                            currentByte = currentByte or (1 shl (7 - bit))
                        }
                    }
                }
                rasterBytes[byteIndex++] = currentByte.toByte()
            }
        }

        val feedAndCutCmd = byteArrayOf(
            0x1B, 0x64, 0x04, // ESC d 4 (Feed 4 lines)
            0x1D, 0x56, 0x42, 0x00 // GS V B 0 (Partial cut if supported)
        )

        val totalSize = initCmd.size + headerCmd.size + rasterBytes.size + feedAndCutCmd.size
        val output = ByteArray(totalSize)
        var offset = 0

        System.arraycopy(initCmd, 0, output, offset, initCmd.size)
        offset += initCmd.size
        System.arraycopy(headerCmd, 0, output, offset, headerCmd.size)
        offset += headerCmd.size
        System.arraycopy(rasterBytes, 0, output, offset, rasterBytes.size)
        offset += rasterBytes.size
        System.arraycopy(feedAndCutCmd, 0, output, offset, feedAndCutCmd.size)

        return output
    }
}
