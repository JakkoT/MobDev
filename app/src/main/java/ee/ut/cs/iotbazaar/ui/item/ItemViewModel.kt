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
    private val db = AppDatabase.getInstance(application)
    private val repository = ItemRepository(db.itemDao())

    val items: LiveData<List<Item>> = repository.getAllItems().asLiveData()

    fun addItem(name: String) = viewModelScope.launch {
        repository.insert(name)
    }

    fun deleteItem(item: Item) = viewModelScope.launch {
        repository.delete(item)
    }

    fun seedIfEmpty() = viewModelScope.launch {
        if (repository.count() == 0) {
            listOf("ESP32 Dev Board", "Raspberry Pi 4", "Arduino Nano", "LoRa Sensor", "BLE Beacon")
                .forEach { repository.insert(it) }
        }
    }
}
