package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.OrderItem
import com.example.data.model.OrderStatus
import com.example.data.model.ProductCategory
import org.json.JSONArray
import org.json.JSONObject

class Converters {
    @TypeConverter
    fun fromCategory(category: ProductCategory): String = category.name

    @TypeConverter
    fun toCategory(value: String): ProductCategory = try {
        ProductCategory.valueOf(value)
    } catch (_: Exception) {
        ProductCategory.KEERAI
    }

    @TypeConverter
    fun fromOrderStatus(status: OrderStatus): String = status.name

    @TypeConverter
    fun toOrderStatus(value: String): OrderStatus = try {
        OrderStatus.valueOf(value)
    } catch (_: Exception) {
        OrderStatus.PENDING
    }

    @TypeConverter
    fun fromOrderItems(items: List<OrderItem>): String {
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject()
            obj.put("productId", item.productId)
            obj.put("nameEn", item.nameEn)
            obj.put("nameTa", item.nameTa)
            obj.put("unitPrice", item.unitPrice)
            obj.put("unit", item.unit)
            obj.put("quantity", item.quantity)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toOrderItems(json: String): List<OrderItem> {
        val list = mutableListOf<OrderItem>()
        if (json.isBlank()) return list
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    OrderItem(
                        productId = obj.optLong("productId", 0L),
                        nameEn = obj.optString("nameEn", ""),
                        nameTa = obj.optString("nameTa", ""),
                        unitPrice = obj.optDouble("unitPrice", 0.0),
                        unit = obj.optString("unit", ""),
                        quantity = obj.optInt("quantity", 1)
                    )
                )
            }
        } catch (_: Exception) {
            // fallback
        }
        return list
    }
}
