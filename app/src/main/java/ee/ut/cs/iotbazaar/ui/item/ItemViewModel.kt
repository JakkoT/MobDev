package ee.ut.cs.iotbazaar.ui.item

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import ee.ut.cs.iotbazaar.model.Item
import ee.ut.cs.iotbazaar.repository.ItemRepository
import kotlinx.coroutines.launch

class ItemViewModel(application: Application) : AndroidViewModel(application) {
    // Initialize the repository
    private val repository = ItemRepository()

    // LiveData to observe the list of items
    val items: LiveData<List<Item>> = repository.getAllItems().asLiveData()

    fun addItem(name: String) = addItem(name, false)

    suspend fun getItem(id: String): Item? {
        return repository.getItemById(id)
    }

    fun addItem(name: String, reserved: Boolean) = viewModelScope.launch {
        repository.insert(name, reserved)
    }

    // Function to delete an item
    fun deleteItem(item: Item) = viewModelScope.launch {
        repository.delete(item)
    }

    // Function to update an item (for example toggle reserved status)
    fun updateItem(item: Item) = viewModelScope.launch {
        repository.update(item)
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
     * Ensure all reserved seed items exist. If DB empty, this simply inserts them all;
     * if partially populated, only missing ones are added.
     */
    fun ensureReservedSeedItems() = viewModelScope.launch {
        for (name in reservedSeedItems) {
            if (!repository.exists(name)) {
                repository.insert(name, reserved = true)
            }
        }
    }
}
