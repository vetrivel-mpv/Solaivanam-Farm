package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.OrderStatus
import com.example.data.model.ProductCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY category, nameEn ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY nameEn ASC")
    fun getProductsByCategory(category: ProductCategory): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("UPDATE products SET stock = :newStock WHERE id = :id")
    suspend fun updateStock(id: Long, newStock: Int)

    @Query("UPDATE products SET isAvailable = :isAvailable WHERE id = :id")
    suspend fun updateAvailability(id: Long, isAvailable: Boolean)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getCount(): Int
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY orderTimestamp DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE status = :status ORDER BY orderTimestamp DESC")
    fun getOrdersByStatus(status: OrderStatus): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE apartment = :apartment ORDER BY orderTimestamp DESC")
    fun getOrdersByApartment(apartment: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun getOrderById(id: Long): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<OrderEntity>)

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, status: OrderStatus)

    @Query("DELETE FROM orders WHERE id = :orderId")
    suspend fun deleteOrder(orderId: Long)

    @Query("SELECT COUNT(*) FROM orders")
    suspend fun getCount(): Int

    @Query("DELETE FROM orders")
    suspend fun deleteAllOrders()
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerEntity>)

    @Query("SELECT COUNT(*) FROM customers")
    suspend fun getCount(): Int

    @Query("DELETE FROM customers")
    suspend fun deleteAllCustomers()
}
