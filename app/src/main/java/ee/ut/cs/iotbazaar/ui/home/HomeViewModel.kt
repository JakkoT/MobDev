package ee.ut.cs.iotbazaar.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel for the HomeFragment.
 * Currently serves as a placeholder for future home screen logic.
 */
class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {}
    /**
     * LiveData for the text to be displayed on the home screen.
     */
    val text: LiveData<String> = _text
}