package ee.ut.cs.iotbazaar.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel for the NotificationsFragment.
 * Currently serves as a placeholder for future notification logic.
 */
class NotificationsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is notifications Fragment"
    }
    /**
     * LiveData for the text to be displayed in the notifications screen.
     */
    val text: LiveData<String> = _text
}