package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.OrderItem
import com.example.data.model.OrderStatus
import com.example.data.model.ProductCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [ProductEntity::class, OrderEntity::class, CustomerEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
    abstract fun customerDao(): CustomerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "solaivanam_farm_database.db"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(db: AppDatabase) {
            val productDao = db.productDao()
            val orderDao = db.orderDao()
            val customerDao = db.customerDao()

            if (productDao.getCount() == 0) {
                val initialProducts = listOf(
                    // Keerai (Fresh Greens)
                    ProductEntity(
                        nameEn = "Sirukeerai",
                        nameTa = "சிறுகீரை",
                        category = ProductCategory.KEERAI,
                        price = 30.0,
                        unit = "கட்டு (bunch)",
                        stock = 45,
                        isAvailable = true,
                        descriptionEn = "Iron-rich fresh green, harvested early morning.",
                        descriptionTa = "இரும்புச்சத்து நிறைந்த காலை நேர புதிய அறுவடை."
                    ),
                    ProductEntity(
                        nameEn = "Arai Keerai",
                        nameTa = "அரைகீரை",
                        category = ProductCategory.KEERAI,
                        price = 30.0,
                        unit = "கட்டு (bunch)",
                        stock = 35,
                        isAvailable = true,
                        descriptionEn = "Nutrient-packed traditional tender greens.",
                        descriptionTa = "இயற்கை உரமிட்டு விளைவிக்கப்பட்ட பஞ்சகவ்ய கீரை."
                    ),
                    ProductEntity(
                        nameEn = "Pasalai Keerai",
                        nameTa = "பசலைக்கீரை",
                        category = ProductCategory.KEERAI,
                        price = 35.0,
                        unit = "கட்டு (bunch)",
                        stock = 25,
                        isAvailable = true,
                        descriptionEn = "Tender water spinach greens, cooling for summer.",
                        descriptionTa = "உடல் குளிர்ச்சி தரும் இயற்கை பசலை."
                    ),
                    ProductEntity(
                        nameEn = "Mudakathan Keerai",
                        nameTa = "முடக்கத்தான்",
                        category = ProductCategory.KEERAI,
                        price = 40.0,
                        unit = "கட்டு (bunch)",
                        stock = 20,
                        isAvailable = true,
                        descriptionEn = "Traditional herbal creeper for joint relief and health.",
                        descriptionTa = "மூட்டு வலி நீக்கும் பாரம்பரிய மருத்துவக் கொடி."
                    ),
                    ProductEntity(
                        nameEn = "Murungai Keerai",
                        nameTa = "முருங்கைக்கீரை",
                        category = ProductCategory.KEERAI,
                        price = 25.0,
                        unit = "கட்டு (bunch)",
                        stock = 50,
                        isAvailable = true,
                        descriptionEn = "Fresh organic drumstick leaves, supreme superfood.",
                        descriptionTa = "இயற்கை முருங்கை மரம் தந்த சத்துக்களஞ்சியம்."
                    ),
                    ProductEntity(
                        nameEn = "Vallarai Keerai",
                        nameTa = "வல்லாரை கீரை",
                        category = ProductCategory.KEERAI,
                        price = 45.0,
                        unit = "கட்டு (bunch)",
                        stock = 18,
                        isAvailable = true,
                        descriptionEn = "Memory booster herbal green for children and elders.",
                        descriptionTa = "நினைவாற்றல் பெருக்கும் பாரம்பரிய வல்லாரை."
                    ),
                    ProductEntity(
                        nameEn = "Manathakkali Keerai",
                        nameTa = "மணத்தக்காளி",
                        category = ProductCategory.KEERAI,
                        price = 35.0,
                        unit = "கட்டு (bunch)",
                        stock = 22,
                        isAvailable = true,
                        descriptionEn = "Natural remedy for stomach & mouth ulcers.",
                        descriptionTa = "வயிற்றுப் புண் ஆற்றும் அரிய மூலிகைக்கீரை."
                    ),

                    // Grains & Millets
                    ProductEntity(
                        nameEn = "Karuppu Kavuni Rice",
                        nameTa = "கருப்பு கவுனி அரிசி",
                        category = ProductCategory.GRAINS,
                        price = 190.0,
                        unit = "1 kg",
                        stock = 30,
                        isAvailable = true,
                        descriptionEn = "Ancient king's black rice, super antioxidant.",
                        descriptionTa = "மன்னர்கள் உண்ட அரிய கருப்பு அரிசி - புற்றுநோய் தடுப்பான்."
                    ),
                    ProductEntity(
                        nameEn = "Mappillai Samba Rice",
                        nameTa = "மாப்பிள்ளை சம்பா",
                        category = ProductCategory.GRAINS,
                        price = 135.0,
                        unit = "1 kg",
                        stock = 40,
                        isAvailable = true,
                        descriptionEn = "Traditional stamina-building hand-pounded red rice.",
                        descriptionTa = "நரம்பு வலுவூட்டும் கைக்குத்தல் பாரம்பரிய அரிசி."
                    ),
                    ProductEntity(
                        nameEn = "Kuthiraivali Millet",
                        nameTa = "குதிரைவாலி அரிசி",
                        category = ProductCategory.GRAINS,
                        price = 95.0,
                        unit = "1 kg",
                        stock = 25,
                        isAvailable = true,
                        descriptionEn = "Barnyard millet, high fiber & low glycemic index.",
                        descriptionTa = "உடல் எடையை கட்டுப்படுத்தும் நார்ச்சத்து நிறைந்த சிறுதானியம்."
                    ),
                    ProductEntity(
                        nameEn = "Thinai Rice",
                        nameTa = "தினை அரிசி",
                        category = ProductCategory.GRAINS,
                        price = 110.0,
                        unit = "1 kg",
                        stock = 20,
                        isAvailable = true,
                        descriptionEn = "Foxtail millet, golden ancient grain for heart health.",
                        descriptionTa = "இதய ஆரோக்கியம் காக்கும் பாரம்பரிய தங்க தானியம்."
                    ),

                    // Wood-Pressed Oils (மரச்செக்கு)
                    ProductEntity(
                        nameEn = "Chekku Nallennai (Gingelly)",
                        nameTa = "மரச்செக்கு நல்லெண்ணெய்",
                        category = ProductCategory.OILS,
                        price = 260.0,
                        unit = "500 ml",
                        stock = 30,
                        isAvailable = true,
                        descriptionEn = "Cold-pressed black sesame oil with pure palm jaggery.",
                        descriptionTa = "பனங்கருப்பட்டி சேர்த்து ஆட்டிய சுத்தமான மரச்செக்கு எண்ணெய்."
                    ),
                    ProductEntity(
                        nameEn = "Chekku Kadalai Ennai",
                        nameTa = "மரச்செக்கு கடலை எண்ணெய்",
                        category = ProductCategory.OILS,
                        price = 180.0,
                        unit = "500 ml",
                        stock = 28,
                        isAvailable = true,
                        descriptionEn = "Aromatic native groundnut cold-pressed oil.",
                        descriptionTa = "நாட்டு நிலக்கடலையில் ஆட்டிய மணமிக்க சுத்தமான எண்ணெய்."
                    ),
                    ProductEntity(
                        nameEn = "Wood-Pressed Coconut Oil",
                        nameTa = "மரச்செக்கு தேங்காய் எண்ணெய்",
                        category = ProductCategory.OILS,
                        price = 210.0,
                        unit = "500 ml",
                        stock = 22,
                        isAvailable = true,
                        descriptionEn = "Sulphur-free sun-dried copra pure coconut oil.",
                        descriptionTa = "இயற்கை கொப்பரைத் தேங்காய் மரச்செக்கு எண்ணெய்."
                    ),

                    // Herbal Powders (Podi)
                    ProductEntity(
                        nameEn = "Traditional Idli Milagai Podi",
                        nameTa = "பருப்பு இட்லி பொடி",
                        category = ProductCategory.PODI,
                        price = 90.0,
                        unit = "100 g",
                        stock = 35,
                        isAvailable = true,
                        descriptionEn = "Crunchy lentil gun powder with farm red chilies.",
                        descriptionTa = "பண்ணை வத்தல், உளுந்து சேர்த்து வறுத்த கைவண்ண பொடி."
                    ),
                    ProductEntity(
                        nameEn = "Pirandai Bone Health Podi",
                        nameTa = "பிரண்டை பொடி",
                        category = ProductCategory.PODI,
                        price = 120.0,
                        unit = "100 g",
                        stock = 15,
                        isAvailable = true,
                        descriptionEn = "Adamant creeper powder for bone density and digestion.",
                        descriptionTa = "எலும்பு தேய்மானம் தடுத்து பசி தூண்டும் பிரண்டை பொடி."
                    ),
                    ProductEntity(
                        nameEn = "Murungai Leaf Podi",
                        nameTa = "முருங்கை இலை பொடி",
                        category = ProductCategory.PODI,
                        price = 95.0,
                        unit = "100 g",
                        stock = 25,
                        isAvailable = true,
                        descriptionEn = "Shade-dried drumstick leaf powder for rice and rasam.",
                        descriptionTa = "நிழலில் உலர்த்தி அரைத்த முருங்கை இலை சத்து பொடி."
                    ),
                    ProductEntity(
                        nameEn = "Karuveppilai Podi",
                        nameTa = "கறிவேப்பிலை பொடி",
                        category = ProductCategory.PODI,
                        price = 85.0,
                        unit = "100 g",
                        stock = 20,
                        isAvailable = true,
                        descriptionEn = "Curry leaf powder for hair strength and eyesight.",
                        descriptionTa = "முடி வளர்ச்சி மற்றும் கண்பார்வைக்கு உகந்த பொடி."
                    )
                )
                productDao.insertProducts(initialProducts)
            }

            if (customerDao.getCount() == 0) {
                val customers = listOf(
                    CustomerEntity(
                        name = "Senthil Kumaran",
                        phone = "9840123456",
                        apartment = "Olympia Opaline",
                        flatNo = "Tower 3, Flat 704",
                        addressNotes = "Leave at door if not reachable."
                    ),
                    CustomerEntity(
                        name = "Meenakshi Sundaram",
                        phone = "9790876543",
                        apartment = "Green Meadows",
                        flatNo = "Block B, Flat 201",
                        addressNotes = "Ring bell twice, eco-bag provided."
                    ),
                    CustomerEntity(
                        name = "Ananya Ramaswamy",
                        phone = "9444198765",
                        apartment = "Olympia Opaline",
                        flatNo = "Tower 1, Flat 1202",
                        addressNotes = "Gate security knows Solaivanam."
                    ),
                    CustomerEntity(
                        name = "Vignesh Karthik",
                        phone = "9962345678",
                        apartment = "Hiranandani Parks",
                        flatNo = "Villa 42",
                        addressNotes = "Deliver early morning for fresh keerai."
                    ),
                    CustomerEntity(
                        name = "Kavitha Rajan",
                        phone = "9884567890",
                        apartment = "Prestige Bella Vista",
                        flatNo = "Tower 8, Flat 405",
                        addressNotes = "Call on arrival."
                    )
                )
                customerDao.insertCustomers(customers)
            }

            if (orderDao.getCount() == 0) {
                val sampleOrders = listOf(
                    OrderEntity(
                        invoiceNo = "SV-2026-1041",
                        customerName = "Senthil Kumaran",
                        customerPhone = "9840123456",
                        apartment = "Olympia Opaline",
                        flatNo = "Tower 3, Flat 704",
                        deliveryAddress = "Tower 3, Flat 704, Olympia Opaline, Navalur, Chennai",
                        items = listOf(
                            OrderItem(1, "Sirukeerai", "சிறுகீரை", 30.0, "கட்டு (bunch)", 2),
                            OrderItem(8, "Karuppu Kavuni Rice", "கருப்பு கவுனி அரிசி", 190.0, "1 kg", 1),
                            OrderItem(12, "Chekku Nallennai (Gingelly)", "மரச்செக்கு நல்லெண்ணெய்", 260.0, "500 ml", 1)
                        ),
                        subtotal = 510.0,
                        deliveryFee = 0.0, // Community bulk waiver
                        totalAmount = 510.0,
                        status = OrderStatus.PENDING,
                        paymentMode = "UPI on Delivery",
                        notes = "Fresh morning harvest keerai"
                    ),
                    OrderEntity(
                        invoiceNo = "SV-2026-1042",
                        customerName = "Meenakshi Sundaram",
                        customerPhone = "9790876543",
                        apartment = "Green Meadows",
                        flatNo = "Block B, Flat 201",
                        deliveryAddress = "Block B, Flat 201, Green Meadows, Perungudi",
                        items = listOf(
                            OrderItem(2, "Arai Keerai", "அரைகீரை", 30.0, "கட்டு (bunch)", 3),
                            OrderItem(4, "Mudakathan Keerai", "முடக்கத்தான்", 40.0, "கட்டு (bunch)", 1),
                            OrderItem(15, "Traditional Idli Milagai Podi", "பருப்பு இட்லி பொடி", 90.0, "100 g", 2)
                        ),
                        subtotal = 310.0,
                        deliveryFee = 25.0,
                        totalAmount = 335.0,
                        status = OrderStatus.PACKED,
                        paymentMode = "Cash on Delivery",
                        notes = "Include herbal recipe pamphlet"
                    ),
                    OrderEntity(
                        invoiceNo = "SV-2026-1043",
                        customerName = "Ananya Ramaswamy",
                        customerPhone = "9444198765",
                        apartment = "Olympia Opaline",
                        flatNo = "Tower 1, Flat 1202",
                        deliveryAddress = "Tower 1, Flat 1202, Olympia Opaline, Navalur",
                        items = listOf(
                            OrderItem(5, "Murungai Keerai", "முருங்கைக்கீரை", 25.0, "கட்டு (bunch)", 2),
                            OrderItem(6, "Vallarai Keerai", "வல்லாரை கீரை", 45.0, "கட்டு (bunch)", 1),
                            OrderItem(9, "Mappillai Samba Rice", "மாப்பிள்ளை சம்பா", 135.0, "1 kg", 2),
                            OrderItem(13, "Chekku Kadalai Ennai", "மரச்செக்கு கடலை எண்ணெய்", 180.0, "500 ml", 1)
                        ),
                        subtotal = 545.0,
                        deliveryFee = 0.0,
                        totalAmount = 545.0,
                        status = OrderStatus.PENDING,
                        paymentMode = "UPI (Prepaid)",
                        notes = "Pack in cloth bag"
                    ),
                    OrderEntity(
                        invoiceNo = "SV-2026-1039",
                        customerName = "Vignesh Karthik",
                        customerPhone = "9962345678",
                        apartment = "Hiranandani Parks",
                        flatNo = "Villa 42",
                        deliveryAddress = "Villa 42, Hiranandani Parks, Oragadam",
                        items = listOf(
                            OrderItem(16, "Pirandai Bone Health Podi", "பிரண்டை பொடி", 120.0, "100 g", 1),
                            OrderItem(14, "Wood-Pressed Coconut Oil", "மரச்செக்கு தேங்காய் எண்ணெய்", 210.0, "500 ml", 1)
                        ),
                        subtotal = 330.0,
                        deliveryFee = 30.0,
                        totalAmount = 360.0,
                        status = OrderStatus.DELIVERED,
                        paymentMode = "Cash on Delivery",
                        notes = "Delivered to security"
                    )
                )
                orderDao.insertOrders(sampleOrders)
            }
        }
    }
}
