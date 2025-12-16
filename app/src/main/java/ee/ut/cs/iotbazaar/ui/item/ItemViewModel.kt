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

/**
 * ViewModel responsible for managing Item data and business logic.
 * Handles interactions between the UI and the ItemRepository.
 */
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
    /**
     * LiveData holding the result of the last reservation attempt.
     */
    val reserveResult: LiveData<Result<Unit>> = _reserveResult

    /**
     * Refreshes the current user ID from FirebaseAuth.
     * Should be called when the user login state might have changed.
     */
    fun refreshUserData() {
        val newUid = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        if (currentUserId.value != newUid) {
            currentUserId.value = newUid
        }
    }

    /**
     * Adds a new item with default stock.
     * @param name The name of the item.
     */
    fun addItem(name: String) = addItem(name, 3)


    /**
     * Retrieves a specific item by its ID.
     * @param id The unique identifier of the item.
     * @return The Item object if found, null otherwise.
     */
    suspend fun getItem(id: String): Item? {
        return repository.getItemById(id)
    }

    /**
     * Adds a new item to the repository.
     * @param name The name of the item.
     * @param stock The initial stock quantity (default is 3).
     */
    fun addItem(name: String, stock: Int = 3) = viewModelScope.launch {
        repository.insert(name, stock)
    }

    /**
     * Deletes an item from the repository.
     * @param item The item to delete.
     */
    fun deleteItem(item: Item) = viewModelScope.launch {
        repository.delete(item)
    }

    /**
     * Updates an existing item in the repository.
     * @param item The item with updated values.
     */
    fun updateItem(item: Item) = viewModelScope.launch {
        repository.update(item)
    }

    /**
     * Reserves an item for the currently logged-in user.
     * Also records the action in the user's scan history.
     *
     * @param itemId The ID of the item to reserve.
     * @param returnDate The timestamp when the item is expected to be returned.
     * @param itemName The name of the item (for history logging).
     */
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

    /**
     * Reserves an item for the currently logged-in user.
     * Deprecated: kept for compatibility if needed, but prefer the one with itemName.
     *
     * @param itemId The ID of the item to reserve.
     * @param returnDate The timestamp when the item is expected to be returned.
     */
    fun reserveItemForCurrentUser(itemId: String, returnDate: Long) = reserveItemForCurrentUser(itemId, returnDate, "Unknown Item")

    /**
     * Returns a borrowed item for the currently logged-in user.
     *
     * @param itemId The ID of the item to return.
     * @param itemName The name of the item (for history logging).
     */
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