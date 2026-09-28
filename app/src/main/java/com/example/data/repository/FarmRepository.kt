package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.CustomerEntity
import com.example.data.local.OrderEntity
import com.example.data.local.ProductEntity
import com.example.data.model.OrderStatus
import com.example.data.model.ProductCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class HarvestItemDemand(
    val productId: Long,
    val nameEn: String,
    val nameTa: String,
    val category: ProductCategory,
    val unit: String,
    val totalQuantityNeeded: Int,
    val orderCount: Int,
    val currentStock: Int
)

data class ApartmentConsolidationGroup(
    val apartmentName: String,
    val orderCount: Int,
    val totalAmount: Double,
    val orders: List<OrderEntity>,
    val bundledItemsSummary: List<String>
)

class FarmRepository(private val db: AppDatabase) {
    val allProducts: Flow<List<ProductEntity>> = db.productDao().getAllProducts()
    val allOrders: Flow<List<OrderEntity>> = db.orderDao().getAllOrders()
    val allCustomers: Flow<List<CustomerEntity>> = db.customerDao().getAllCustomers()

    suspend fun createOrder(order: OrderEntity): Long {
        val id = db.orderDao().insertOrder(order)
        // Deduct stock for ordered items
        order.items.forEach { item ->
            val product = db.productDao().getProductById(item.productId)
            if (product != null) {
                val newStock = (product.stock - item.quantity).coerceAtLeast(0)
                db.productDao().updateStock(product.id, newStock)
            }
        }
        return id
    }

    suspend fun updateOrderStatus(orderId: Long, status: OrderStatus) {
        db.orderDao().updateOrderStatus(orderId, status)
    }

    suspend fun updateProductStock(productId: Long, stock: Int) {
        db.productDao().updateStock(productId, stock)
    }

    suspend fun updateProductAvailability(productId: Long, isAvailable: Boolean) {
        db.productDao().updateAvailability(productId, isAvailable)
    }

    suspend fun saveProduct(product: ProductEntity): Long {
        return db.productDao().insertProduct(product)
    }

    suspend fun saveCustomer(customer: CustomerEntity): Long {
        return db.customerDao().insertCustomer(customer)
    }

    // Calculates batch demand for farm harvest from all pending & packed orders
    fun getHarvestDemand(): Flow<List<HarvestItemDemand>> {
        return allOrders.map { orders ->
            val activeOrders = orders.filter { it.status == OrderStatus.PENDING || it.status == OrderStatus.PACKED }
            val demandMap = mutableMapOf<Long, Pair<Int, Int>>() // productId -> (totalQty, orderCount)
            val itemInfoMap = mutableMapOf<Long, Triple<String, String, String>>() // (nameEn, nameTa, unit)

            activeOrders.forEach { order ->
                order.items.forEach { item ->
                    val current = demandMap[item.productId] ?: (0 to 0)
                    demandMap[item.productId] = (current.first + item.quantity) to (current.second + 1)
                    itemInfoMap[item.productId] = Triple(item.nameEn, item.nameTa, item.unit)
                }
            }

            demandMap.map { (productId, counts) ->
                val info = itemInfoMap[productId] ?: Triple("Organic Produce", "இயற்கை விளைபொருள்", "unit")
                HarvestItemDemand(
                    productId = productId,
                    nameEn = info.first,
                    nameTa = info.second,
                    category = if (info.second.contains("கீரை") || info.first.contains("Keerai")) ProductCategory.KEERAI
                               else if (info.first.contains("Oil") || info.second.contains("எண்ணெய்")) ProductCategory.OILS
                               else if (info.first.contains("Podi") || info.second.contains("பொடி")) ProductCategory.PODI
                               else ProductCategory.GRAINS,
                    unit = info.third,
                    totalQuantityNeeded = counts.first,
                    orderCount = counts.second,
                    currentStock = 0
                )
            }.sortedWith(compareBy({ it.category }, { -it.totalQuantityNeeded }))
        }
    }

    // Aggregates orders by Apartment / Community to bundle for delivery runs
    fun getApartmentConsolidation(): Flow<List<ApartmentConsolidationGroup>> {
        return allOrders.map { orders ->
            val grouped = orders.groupBy { it.apartment.ifBlank { "Direct Orders" } }
            grouped.map { (aptName, aptOrders) ->
                val totalAmount = aptOrders.sumOf { it.totalAmount }
                val itemTotals = mutableMapOf<String, Int>()
                aptOrders.forEach { ord ->
                    ord.items.forEach { itm ->
                        val label = "${itm.nameEn} (${itm.nameTa})"
                        itemTotals[label] = (itemTotals[label] ?: 0) + itm.quantity
                    }
                }
                val summary = itemTotals.map { "${it.value}x ${it.key}" }
                ApartmentConsolidationGroup(
                    apartmentName = aptName,
                    orderCount = aptOrders.size,
                    totalAmount = totalAmount,
                    orders = aptOrders,
                    bundledItemsSummary = summary
                )
            }.sortedByDescending { it.orderCount }
        }
    }

    suspend fun clearAllUserData() {
        db.orderDao().deleteAllOrders()
        db.customerDao().deleteAllCustomers()
    }
}
