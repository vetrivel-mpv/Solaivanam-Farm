package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.OrderItem
import com.example.data.model.OrderStatus
import com.example.data.model.Product
import com.example.data.model.ProductCategory

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nameEn: String,
    val nameTa: String,
    val category: ProductCategory,
    val price: Double,
    val unit: String,
    val stock: Int,
    val isAvailable: Boolean = true,
    val descriptionEn: String = "",
    val descriptionTa: String = "",
    val harvestDays: String = "Daily Fresh"
) {
    fun toDomain(): Product = Product(
        id = id,
        nameEn = nameEn,
        nameTa = nameTa,
        category = category,
        price = price,
        unit = unit,
        stock = stock,
        isAvailable = isAvailable,
        descriptionEn = descriptionEn,
        descriptionTa = descriptionTa,
        harvestDays = harvestDays
    )

    companion object {
        fun fromDomain(p: Product): ProductEntity = ProductEntity(
            id = p.id,
            nameEn = p.nameEn,
            nameTa = p.nameTa,
            category = p.category,
            price = p.price,
            unit = p.unit,
            stock = p.stock,
            isAvailable = p.isAvailable,
            descriptionEn = p.descriptionEn,
            descriptionTa = p.descriptionTa,
            harvestDays = p.harvestDays
        )
    }
}

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val apartment: String,
    val flatNo: String,
    val addressNotes: String = ""
)
