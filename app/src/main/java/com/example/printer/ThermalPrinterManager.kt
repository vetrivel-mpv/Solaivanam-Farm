package com.example.printer

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.data.local.OrderEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.util.UUID

sealed class PrinterConnectionState {
    object Disconnected : PrinterConnectionState()
    data class Connecting(val deviceName: String) : PrinterConnectionState()
    data class Connected(val deviceName: String, val address: String) : PrinterConnectionState()
    data class Printing(val invoiceNo: String) : PrinterConnectionState()
    data class Error(val message: String) : PrinterConnectionState()
}

data class BluetoothPrinterDevice(
    val name: String,
    val address: String,
    val isPaired: Boolean = true
)

class ThermalPrinterManager(private val context: Context, private val scope: CoroutineScope) {
    companion object {
        private const val TAG = "ThermalPrinterManager"
        // Standard Serial Port Profile (SPP) UUID used by virtually all Bluetooth thermal receipt printers
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    private val _connectionState = MutableStateFlow<PrinterConnectionState>(PrinterConnectionState.Disconnected)
    val connectionState: StateFlow<PrinterConnectionState> = _connectionState.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothPrinterDevice>>(emptyList())
    val pairedDevices: StateFlow<List<BluetoothPrinterDevice>> = _pairedDevices.asStateFlow()

    // For Virtual Thermal Printer Simulator and Preview
    private val _lastPrintedBitmap = MutableStateFlow<Bitmap?>(null)
    val lastPrintedBitmap: StateFlow<Bitmap?> = _lastPrintedBitmap.asStateFlow()

    private val _lastPrintedOrder = MutableStateFlow<OrderEntity?>(null)
    val lastPrintedOrder: StateFlow<OrderEntity?> = _lastPrintedOrder.asStateFlow()

    private val _isVirtualMode = MutableStateFlow(true)
    val isVirtualMode: StateFlow<Boolean> = _isVirtualMode.asStateFlow()

    private var activeSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    init {
        refreshPairedDevices()
    }

    @SuppressLint("MissingPermission")
    fun refreshPairedDevices() {
        try {
            val adapter = BluetoothAdapter.getDefaultAdapter()
            if (adapter == null || !adapter.isEnabled) {
                _pairedDevices.value = listOf(
                    BluetoothPrinterDevice("Niyama BT-58 (Simulated)", "00:11:22:33:44:55"),
                    BluetoothPrinterDevice("Everycom POS-58 (Simulated)", "AA:BB:CC:DD:EE:FF")
                )
                return
            }

            val devices = adapter.bondedDevices?.map { device ->
                BluetoothPrinterDevice(
                    name = device.name ?: "Thermal Printer (${device.address.takeLast(5)})",
                    address = device.address,
                    isPaired = true
                )
            } ?: emptyList()

            // Prepend known default simulator options if empty
            if (devices.isEmpty()) {
                _pairedDevices.value = listOf(
                    BluetoothPrinterDevice("Niyama BT-58 (Demo)", "00:11:22:33:44:55"),
                    BluetoothPrinterDevice("TVS RP-58 (Demo)", "12:34:56:78:90:AB")
                )
            } else {
                _pairedDevices.value = devices
            }
        } catch (e: Exception) {
            Log.w(TAG, "Bluetooth not accessible or permissions pending: ${e.message}")
            _pairedDevices.value = listOf(
                BluetoothPrinterDevice("Niyama BT-58 (Virtual)", "00:11:22:33:44:55")
            )
        }
    }

    fun toggleVirtualMode(enabled: Boolean) {
        _isVirtualMode.value = enabled
        if (enabled && activeSocket != null) {
            disconnect()
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothPrinterDevice) {
        if (device.address == "00:11:22:33:44:55" || device.address.contains("Demo") || device.name.contains("Simulated") || _isVirtualMode.value) {
            _connectionState.value = PrinterConnectionState.Connected(device.name, device.address)
            return
        }

        scope.launch(Dispatchers.IO) {
            _connectionState.value = PrinterConnectionState.Connecting(device.name)
            try {
                val adapter = BluetoothAdapter.getDefaultAdapter()
                if (adapter == null || !adapter.isEnabled) {
                    withContext(Dispatchers.Main) {
                        _connectionState.value = PrinterConnectionState.Error("Bluetooth is turned off")
                    }
                    return@launch
                }

                val remoteDevice: BluetoothDevice = adapter.getRemoteDevice(device.address)
                val socket = remoteDevice.createRfcommSocketToServiceRecord(SPP_UUID)
                adapter.cancelDiscovery()
                socket.connect()

                activeSocket = socket
                outputStream = socket.outputStream

                withContext(Dispatchers.Main) {
                    _connectionState.value = PrinterConnectionState.Connected(device.name, device.address)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to connect to Bluetooth printer", e)
                withContext(Dispatchers.Main) {
                    _connectionState.value = PrinterConnectionState.Error("Could not connect to ${device.name}: ${e.message}")
                }
            }
        }
    }

    fun disconnect() {
        try {
            outputStream?.close()
            activeSocket?.close()
        } catch (_: Exception) {}
        activeSocket = null
        outputStream = null
        _connectionState.value = PrinterConnectionState.Disconnected
    }

    /**
     * Prints an order by rendering Tamil Canvas to 58mm ESC/POS Raster bitmap and sending
     * over Bluetooth socket or to the Virtual Thermal Printer.
     */
    fun printOrder(order: OrderEntity, onComplete: (Boolean, String) -> Unit) {
        scope.launch(Dispatchers.Default) {
            val prev = _connectionState.value
            _connectionState.value = PrinterConnectionState.Printing(order.invoiceNo)

            try {
                // 1. Render bilingual Tamil Canvas bitmap
                val bitmap = EscPosRasterizer.renderReceiptBitmap(order)
                _lastPrintedBitmap.value = bitmap
                _lastPrintedOrder.value = order

                // 2. Generate ESC/POS raster byte stream
                val rasterBytes = EscPosRasterizer.bitmapToEscPosRasterBytes(bitmap)

                // 3. If connected to physical Bluetooth printer, stream the bytes
                val stream = outputStream
                if (stream != null && activeSocket?.isConnected == true) {
                    withContext(Dispatchers.IO) {
                        // Stream in chunks of 512 bytes with slight delay to prevent printer buffer overrun
                        val chunkSize = 512
                        var offset = 0
                        while (offset < rasterBytes.size) {
                            val count = (rasterBytes.size - offset).coerceAtMost(chunkSize)
                            stream.write(rasterBytes, offset, count)
                            stream.flush()
                            offset += count
                            Thread.sleep(15)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        _connectionState.value = prev
                        onComplete(true, "Printed via Bluetooth to POS printer!")
                    }
                } else {
                    // Virtual mode / preview simulation
                    Thread.sleep(600) // Realistic printer motor latency simulation
                    withContext(Dispatchers.Main) {
                        _connectionState.value = prev
                        onComplete(true, "Rendered to 58mm Thermal Paper Roll!")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Print failed", e)
                withContext(Dispatchers.Main) {
                    _connectionState.value = PrinterConnectionState.Error("Print failed: ${e.message}")
                    onComplete(false, "Print error: ${e.localizedMessage}")
                }
            }
        }
    }
}
