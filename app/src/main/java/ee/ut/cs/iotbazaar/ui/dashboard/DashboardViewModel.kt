package ee.ut.cs.iotbazaar.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel for the DashboardFragment.
 * Currently serves as a placeholder for future dashboard logic.
 */
class DashboardViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }
    /**
     * LiveData for the text to be displayed in the dashboard screen.
     */
    val text: LiveData<String> = _text
}