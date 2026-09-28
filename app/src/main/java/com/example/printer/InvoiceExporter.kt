package com.example.printer

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.local.OrderEntity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object InvoiceExporter {

    /**
     * Shares invoice details and optional thermal receipt image via Android Share Sheet (WhatsApp, etc.)
     */
    fun shareOrder(context: Context, order: OrderEntity, bitmap: Bitmap? = null) {
        val summaryBuilder = StringBuilder().apply {
            append("🌱 *SOLAIVANAM • சோலைவனம்* 🌱\n")
            append("இயற்கை பண்ணை நேரடி வரவு (Farm-Fresh Organic)\n\n")
            append("🧾 *Invoice:* ${order.invoiceNo}\n")
            append("👤 *Customer:* ${order.customerName} (${order.customerPhone})\n")
            append("🏢 *Community:* ${order.apartment}, Flat ${order.flatNo}\n\n")
            append("📋 *Order Items / பொருட்கள்:*\n")
            order.items.forEachIndexed { i, item ->
                append("${i + 1}. ${item.nameTa} (${item.nameEn}) - ${item.quantity} ${item.unit} x ₹${"%.2f".format(item.unitPrice)} = ₹${"%.2f".format(item.totalAmount)}\n")
            }
            append("\n-------------------------------\n")
            append("Subtotal: ₹${"%.2f".format(order.subtotal)}\n")
            if (order.deliveryFee > 0) {
                append("Delivery: ₹${"%.2f".format(order.deliveryFee)}\n")
            } else {
                append("Delivery: FREE (Community bulk waiver)\n")
            }
            append("💰 *TOTAL: ₹${"%.2f".format(order.totalAmount)}*\n")
            append("Status: ${order.status.name} | Pay: ${order.paymentMode}\n\n")
            append("இயற்கையைக் காப்போம்! நன்றி!\n")
            append("SOLAIVANAM Farm, Chengalpattu")
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Solaivanam Invoice #${order.invoiceNo}")
            putExtra(Intent.EXTRA_TEXT, summaryBuilder.toString())
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share Invoice via"))
    }

    /**
     * Uses Android PrintManager to trigger native Print / Save to PDF dialog for the invoice.
     */
    fun printPdfInvoice(context: Context, order: OrderEntity) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        if (printManager == null) {
            Toast.makeText(context, "Print service unavailable on this device", Toast.LENGTH_SHORT).show()
            return
        }

        val jobName = "Solaivanam_Invoice_${order.invoiceNo}"
        val receiptBitmap = EscPosRasterizer.renderReceiptBitmap(order)

        printManager.print(
            jobName,
            object : PrintDocumentAdapter() {
                private var pdfDocument: PdfDocument? = null

                override fun onLayout(
                    oldAttributes: PrintAttributes?,
                    newAttributes: PrintAttributes,
                    cancellationSignal: CancellationSignal?,
                    callback: LayoutResultCallback?,
                    extras: Bundle?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback?.onLayoutCancelled()
                        return
                    }

                    val info = PrintDocumentInfo.Builder("$jobName.pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(1)
                        .build()

                    callback?.onLayoutFinished(info, true)
                }

                override fun onWrite(
                    pages: Array<out PageRange>?,
                    destination: ParcelFileDescriptor?,
                    cancellationSignal: CancellationSignal?,
                    callback: WriteResultCallback?
                ) {
                    if (destination == null) {
                        callback?.onWriteFailed("Missing destination")
                        return
                    }

                    val doc = PdfDocument()
                    pdfDocument = doc

                    // Page size: 384 pt width, height matching receipt
                    val pageInfo = PdfDocument.PageInfo.Builder(384, receiptBitmap.height, 1).create()
                    val page = doc.startPage(pageInfo)

                    val canvas = page.canvas
                    canvas.drawColor(Color.WHITE)
                    canvas.drawBitmap(receiptBitmap, 0f, 0f, null)

                    doc.finishPage(page)

                    try {
                        FileOutputStream(destination.fileDescriptor).use { output ->
                            doc.writeTo(output)
                        }
                        callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    } catch (e: IOException) {
                        callback?.onWriteFailed(e.message)
                    } finally {
                        doc.close()
                        pdfDocument = null
                    }
                }
            },
            PrintAttributes.Builder()
                .setColorMode(PrintAttributes.COLOR_MODE_MONOCHROME)
                .build()
        )
    }
}
