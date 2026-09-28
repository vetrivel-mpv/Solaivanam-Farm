package com.example.data.model

enum class ProductCategory(val enName: String, val taName: String, val icon: String) {
    KEERAI("Fresh Greens", "கீரை வகைகள்", "🌿"),
    GRAINS("Grains & Millets", "பாரம்பரிய தானியங்கள்", "🌾"),
    OILS("Wood-Pressed Oils", "மரச்செக்கு எண்ணெய்கள்", "🫒"),
    PODI("Herbal Powders", "மூலிகைப் பொடிகள்", "🥣")
}

data class Product(
    val id: Long = 0,
    val nameEn: String,
    val nameTa: String,
    val category: ProductCategory,
    val price: Double,
    val unit: String, // e.g., "கட்டு (bunch)", "1 kg", "500 ml", "100 g"
    val stock: Int,
    val isAvailable: Boolean = true,
    val descriptionEn: String = "",
    val descriptionTa: String = "",
    val harvestDays: String = "Daily Fresh"
)
