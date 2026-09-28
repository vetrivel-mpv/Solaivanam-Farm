package com.example.data.model

data class OrderItem(
    val productId: Long,
    val nameEn: String,
    val nameTa: String,
    val unitPrice: Double,
    val unit: String,
    val quantity: Int
) {
    val totalAmount: Double get() = unitPrice * quantity
}

enum class OrderStatus(val enName: String, val taName: String) {
    PENDING("Pending", "காத்திருக்கிறது"),
    PACKED("Packed", "பேக் செய்யப்பட்டது"),
    OUT_FOR_DELIVERY("Out for Delivery", "டெலிவரிக்கு புறப்பட்டது"),
    DELIVERED("Delivered", "டெலிவரி செய்யப்பட்டது"),
    CANCELLED("Cancelled", "ரத்து செய்யப்பட்டது")
}

data class Order(
    val id: Long = 0,
    val invoiceNo: String,
    val customerName: String,
    val customerPhone: String,
    val apartment: String,
    val flatNo: String,
    val deliveryAddress: String,
    val items: List<OrderItem>,
    val subtotal: Double,
    val deliveryFee: Double,
    val totalAmount: Double,
    val status: OrderStatus = OrderStatus.PENDING,
    val paymentMode: String = "Cash on Delivery",
    val orderTimestamp: Long = System.currentTimeMillis(),
    val harvestBatchDate: String = "",
    val notes: String = ""
)

data class Customer(
    val id: Long = 0,
    val name: String,
    val phone: String,
    val apartment: String,
    val flatNo: String,
    val addressNotes: String = ""
)
