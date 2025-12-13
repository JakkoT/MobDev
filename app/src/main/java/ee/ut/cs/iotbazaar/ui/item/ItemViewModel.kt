package ee.ut.cs.iotbazaar.ui.item

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import ee.ut.cs.iotbazaar.model.Item
import ee.ut.cs.iotbazaar.model.ScanHistoryItem
import ee.ut.cs.iotbazaar.repository.ItemRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ItemViewModel(application: Application) : AndroidViewModel(application) {
    // Initialize the repository
    private val repository = ItemRepository()

    private val currentUserId = MutableLiveData<String>().apply {
        value = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
    }

    // LiveData to observe the list of items
    val items: LiveData<List<Item>> = repository.getAllItems().asLiveData()

    // LiveData to observe items borrowed by the current user
    val userBorrowedItems: LiveData<List<Item>> = currentUserId.switchMap { uid ->
        repository.getAllItems()
            .combine(
                repository.getUserBorrowedItemsInfo(uid)
            ) { allItems, borrowedInfo ->
                allItems.filter { it.id in borrowedInfo.keys }
                    .map { item ->
                        item.copy(returnDate = borrowedInfo[item.id])
                    }
            }.asLiveData()
    }

    // LiveData for scan history
    val scanHistory: LiveData<List<ScanHistoryItem>> = currentUserId.switchMap { uid ->
        repository.getUserScanHistory(uid).asLiveData()
    }

    private val _reserveResult = MutableLiveData<Result<Unit>>()
    val reserveResult: LiveData<Result<Unit>> = _reserveResult

    fun refreshUserData() {
        val newUid = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        if (currentUserId.value != newUid) {
            currentUserId.value = newUid
        }
    }

    fun addItem(name: String) = addItem(name, 3)


    suspend fun getItem(id: String): Item? {
        return repository.getItemById(id)
    }

    fun addItem(name: String, stock: Int = 3) = viewModelScope.launch {
        repository.insert(name, stock)
    }

    // Function to delete an item
    fun deleteItem(item: Item) = viewModelScope.launch {
        repository.delete(item)
    }

    // Function to update an item (for example toggle reserved status)
    fun updateItem(item: Item) = viewModelScope.launch {
        repository.update(item)
    }
    fun reserveItemForCurrentUser(itemId: String, returnDate: Long, itemName: String) = viewModelScope.launch {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            _reserveResult.postValue(Result.failure(Exception("Kasutaja ei ole sisse logitud")))
            return@launch
        }

        val result = repository.reserveItemForUser(userId, itemId, returnDate)
        if (result.isSuccess) {
            repository.addScanHistory(userId, ScanHistoryItem(
                itemId = itemId,
                itemName = itemName,
                timestamp = System.currentTimeMillis(),
                action = "BORROW"
            ))
        }
        _reserveResult.postValue(result)
    }

    // Deprecated: kept for compatibility if needed, but prefer the one with itemName
    fun reserveItemForCurrentUser(itemId: String, returnDate: Long) = reserveItemForCurrentUser(itemId, returnDate, "Unknown Item")

    fun returnItem(itemId: String, itemName: String) = viewModelScope.launch {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            _reserveResult.postValue(Result.failure(Exception("Kasutaja ei ole sisse logitud")))
            return@launch
        }
        val result = repository.returnItem(userId, itemId)
        if (result.isSuccess) {
            repository.addScanHistory(userId, ScanHistoryItem(
                itemId = itemId,
                itemName = itemName,
                timestamp = System.currentTimeMillis(),
                action = "RETURN"
            ))
        }
        _reserveResult.postValue(result)
    }

    // Deprecated: kept for compatibility
    fun returnItem(itemId: String) = returnItem(itemId, "Unknown Item")


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
                repository.insert(name, stock = 3)
            }
        }
    }
}