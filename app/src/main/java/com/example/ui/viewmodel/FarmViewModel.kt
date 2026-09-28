package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.CustomerEntity
import com.example.data.local.OrderEntity
import com.example.data.local.ProductEntity
import com.example.data.model.OrderItem
import com.example.data.model.OrderStatus
import com.example.data.model.ProductCategory
import com.example.data.repository.ApartmentConsolidationGroup
import com.example.data.repository.FarmRepository
import com.example.data.repository.HarvestItemDemand
import com.example.printer.BluetoothPrinterDevice
import com.example.printer.PrinterConnectionState
import com.example.printer.ThermalPrinterManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppLanguage {
    ENGLISH, TAMIL
}

enum class UserRole(val titleEn: String, val titleTa: String) {
    CUSTOMER("Customer", "வாடிக்கையாளர்"),
    ADMIN("Admin / Farm Ops", "பண்ணை நிர்வாகம்"),
    PACKER("Packer / Delivery", "பேக்கிங் & டெலிவரி")
}

data class CartLine(
    val product: ProductEntity,
    val quantity: Int
) {
    val total: Double get() = product.price * quantity
}

class FarmViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application, viewModelScope)
    val repository = FarmRepository(db)
    val printerManager = ThermalPrinterManager(application, viewModelScope)

    // Language
    private val _language = MutableStateFlow(AppLanguage.TAMIL) // Default Tamil & bilingual
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    // Role
    private val _role = MutableStateFlow(UserRole.CUSTOMER)
    val role: StateFlow<UserRole> = _role.asStateFlow()

    private val _isAdminUnlocked = MutableStateFlow(true) // Set default true for smooth demo, with PIN prompt option
    val isAdminUnlocked: StateFlow<Boolean> = _isAdminUnlocked.asStateFlow()

    // Database Flows
    val products: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<OrderEntity>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<CustomerEntity>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val harvestDemand: StateFlow<List<HarvestItemDemand>> = repository.getHarvestDemand()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val apartmentConsolidation: StateFlow<List<ApartmentConsolidationGroup>> = repository.getApartmentConsolidation()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart State: productId -> quantity
    private val _cartMap = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val cartMap: StateFlow<Map<Long, Int>> = _cartMap.asStateFlow()

    // Cart calculations combined with products
    val cartLines: StateFlow<List<CartLine>> = combine(products, _cartMap) { prodList, cart ->
        val prodMap = prodList.associateBy { it.id }
        cart.mapNotNull { (id, qty) ->
            prodMap[id]?.let { CartLine(it, qty) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartSubtotal: StateFlow<Double> = cartLines.combine(MutableStateFlow(0)) { lines, _ ->
        lines.sumOf { it.total }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Selected Checkout Customer Info
    val checkoutName = MutableStateFlow("")
    val checkoutPhone = MutableStateFlow("")
    val checkoutApartment = MutableStateFlow("Olympia Opaline")
    val checkoutFlatNo = MutableStateFlow("")
    val checkoutAddress = MutableStateFlow("")
    val checkoutPaymentMode = MutableStateFlow("Cash on Delivery")
    val checkoutNotes = MutableStateFlow("")

    // Harvest Checked status (local in-memory checklist for today's harvest session)
    private val _harvestCheckedItems = MutableStateFlow<Set<Long>>(emptySet())
    val harvestCheckedItems: StateFlow<Set<Long>> = _harvestCheckedItems.asStateFlow()

    // Printer & Dialog States
    val showVirtualPrinterDialog = MutableStateFlow(false)
    val showCartBottomSheet = MutableStateFlow(false)
    val showAdminPinDialog = MutableStateFlow(false)
    val showNewProductDialog = MutableStateFlow(false)

    // Selected order for detailed receipt view
    val selectedOrderForReceipt = MutableStateFlow<OrderEntity?>(null)

    // UI Toast message
    val userNotice = MutableStateFlow<String?>(null)

    fun toggleLanguage() {
        _language.value = if (_language.value == AppLanguage.TAMIL) AppLanguage.ENGLISH else AppLanguage.TAMIL
    }

    fun setRole(newRole: UserRole) {
        if (newRole == UserRole.ADMIN && !_isAdminUnlocked.value) {
            showAdminPinDialog.value = true
        } else {
            _role.value = newRole
        }
    }

    fun unlockAdmin(pin: String): Boolean {
        return if (pin == "2026" || pin == "1234") {
            _isAdminUnlocked.value = true
            _role.value = UserRole.ADMIN
            showAdminPinDialog.value = false
            true
        } else {
            false
        }
    }

    // Cart actions
    fun addToCart(product: ProductEntity) {
        val current = _cartMap.value.toMutableMap()
        val currentQty = current[product.id] ?: 0
        if (currentQty < product.stock) {
            current[product.id] = currentQty + 1
            _cartMap.value = current
            userNotice.value = "${product.nameTa} கூடையில் சேர்க்கப்பட்டது"
        } else {
            userNotice.value = "கையிருப்பு குறைவு (Stock limit reached)"
        }
    }

    fun removeFromCart(productId: Long) {
        val current = _cartMap.value.toMutableMap()
        val currentQty = current[productId] ?: 0
        if (currentQty > 1) {
            current[productId] = currentQty - 1
        } else {
            current.remove(productId)
        }
        _cartMap.value = current
    }

    fun clearCart() {
        _cartMap.value = emptyMap()
    }

    fun autofillCustomer(customer: CustomerEntity) {
        checkoutName.value = customer.name
        checkoutPhone.value = customer.phone
        checkoutApartment.value = customer.apartment
        checkoutFlatNo.value = customer.flatNo
        checkoutAddress.value = "${customer.flatNo}, ${customer.apartment}"
        checkoutNotes.value = customer.addressNotes
        userNotice.value = "${customer.name} விவரங்கள் பூர்த்தி செய்யப்பட்டன"
    }

    fun placeOrder(onSuccess: (OrderEntity) -> Unit) {
        val lines = cartLines.value
        if (lines.isEmpty()) {
            userNotice.value = "கூடை காலியாக உள்ளது (Cart is empty)"
            return
        }

        if (checkoutName.value.isBlank() || checkoutPhone.value.isBlank()) {
            userNotice.value = "பெயர் மற்றும் தொலைபேசி எண் தேவை (Name & Phone required)"
            return
        }

        viewModelScope.launch {
            val invoiceNumber = "SV-${SimpleDateFormat("yyyyMMdd-HHmm", Locale.getDefault()).format(Date())}"
            val subtotal = lines.sumOf { it.total }
            // Community delivery waiver: orders >= ₹400 have free delivery!
            val deliveryFee = if (subtotal >= 400.0) 0.0 else 30.0
            val total = subtotal + deliveryFee

            val orderItems = lines.map {
                OrderItem(
                    productId = it.product.id,
                    nameEn = it.product.nameEn,
                    nameTa = it.product.nameTa,
                    unitPrice = it.product.price,
                    unit = it.product.unit,
                    quantity = it.quantity
                )
            }

            val orderEntity = OrderEntity(
                invoiceNo = invoiceNumber,
                customerName = checkoutName.value.trim(),
                customerPhone = checkoutPhone.value.trim(),
                apartment = checkoutApartment.value.trim(),
                flatNo = checkoutFlatNo.value.trim(),
                deliveryAddress = checkoutAddress.value.ifBlank { "${checkoutFlatNo.value}, ${checkoutApartment.value}" },
                items = orderItems,
                subtotal = subtotal,
                deliveryFee = deliveryFee,
                totalAmount = total,
                status = OrderStatus.PENDING,
                paymentMode = checkoutPaymentMode.value,
                notes = checkoutNotes.value
            )

            val orderId = repository.createOrder(orderEntity)
            val savedOrder = orderEntity.copy(id = orderId)

            // Auto-save customer if new
            repository.saveCustomer(
                CustomerEntity(
                    name = checkoutName.value.trim(),
                    phone = checkoutPhone.value.trim(),
                    apartment = checkoutApartment.value.trim(),
                    flatNo = checkoutFlatNo.value.trim(),
                    addressNotes = checkoutNotes.value
                )
            )

            clearCart()
            showCartBottomSheet.value = false
            selectedOrderForReceipt.value = savedOrder

            // Automatically print to 58mm thermal printer or virtual paper roll!
            printerManager.printOrder(savedOrder) { success, msg ->
                userNotice.value = if (success) "ஆர்டர் பதிவானது! 58mm ரசீது அச்சிடப்பட்டது" else msg
            }

            onSuccess(savedOrder)
        }
    }

    fun updateOrderStatus(orderId: Long, newStatus: OrderStatus) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus)
            userNotice.value = "ஆர்டர் நிலை மாற்றப்பட்டது: ${newStatus.taName}"
        }
    }

    fun printOrderReceipt(order: OrderEntity) {
        selectedOrderForReceipt.value = order
        printerManager.printOrder(order) { success, msg ->
            userNotice.value = if (success) "பிரிண்ட் செய்யப்பட்டது (${order.invoiceNo})" else msg
        }
    }

    fun toggleHarvestChecked(productId: Long) {
        val current = _harvestCheckedItems.value.toMutableSet()
        if (current.contains(productId)) {
            current.remove(productId)
        } else {
            current.add(productId)
        }
        _harvestCheckedItems.value = current
    }

    fun updateStock(productId: Long, newStock: Int) {
        viewModelScope.launch {
            repository.updateProductStock(productId, newStock.coerceAtLeast(0))
        }
    }

    fun toggleAvailability(productId: Long, isAvailable: Boolean) {
        viewModelScope.launch {
            repository.updateProductAvailability(productId, isAvailable)
        }
    }

    fun addNewProduct(
        nameEn: String,
        nameTa: String,
        category: ProductCategory,
        price: Double,
        unit: String,
        stock: Int,
        descEn: String,
        descTa: String
    ) {
        viewModelScope.launch {
            val newP = ProductEntity(
                nameEn = nameEn,
                nameTa = nameTa,
                category = category,
                price = price,
                unit = unit,
                stock = stock,
                isAvailable = true,
                descriptionEn = descEn,
                descriptionTa = descTa
            )
            repository.saveProduct(newP)
            showNewProductDialog.value = false
            userNotice.value = "$nameTa சேர்க்கப்பட்டது!"
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllUserData()
            clearCart()
            selectedOrderForReceipt.value = null
            userNotice.value = "அனைத்து ஆர்டர் தரவுகளும் நீக்கப்பட்டன (All local order data cleared)"
        }
    }
}
