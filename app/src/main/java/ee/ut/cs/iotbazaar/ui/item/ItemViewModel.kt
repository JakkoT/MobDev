package ee.ut.cs.iotbazaar.ui.item

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import ee.ut.cs.iotbazaar.data.database.AppDatabase
import ee.ut.cs.iotbazaar.model.Item
import ee.ut.cs.iotbazaar.repository.ItemRepository
import kotlinx.coroutines.launch

class ItemViewModel(application: Application) : AndroidViewModel(application) {
    // Initialize the database and repository
    private val db = AppDatabase.getInstance(application)
    private val repository = ItemRepository(db.itemDao())

    // LiveData to observe the list of items
    val items: LiveData<List<Item>> = repository.getAllItems().asLiveData()

    // Backward-compatible single-parameter version (defaults reserved = false)
    fun addItem(name: String) = addItem(name, false)

    // New version supporting reserved flag from Catalogue form
    fun addItem(name: String, reserved: Boolean) = viewModelScope.launch {
        repository.insert(name, reserved)
    }

    // Function to delete an item
    fun deleteItem(item: Item) = viewModelScope.launch {
        repository.delete(item)
    }

    // Central list of reserved sample items (all reserved)
    private val reservedSeedItems = listOf(
        "ESP32 Dev Board",
        "Raspberry Pi 4",
        "Arduino Nano",
        "LoRa Sensor",
        "BLE Beacon",
        "Smart Thermostat",
        "Zigbee Gateway",
        "NB-IoT Tracker",
        "Industrial Sensor Node"
    )

    /**
     * Ensure all reserved seed items exist (idempotent). If DB empty, this simply inserts them all;
     * if partially populated, only missing ones are added. Replaces former seedIfEmpty() and
     * ensureReservedSamples().
     */
    fun ensureReservedSeedItems() = viewModelScope.launch {
        for (name in reservedSeedItems) {
            if (!repository.exists(name)) {
                repository.insert(name, reserved = true)
            }
        }
    }
}
