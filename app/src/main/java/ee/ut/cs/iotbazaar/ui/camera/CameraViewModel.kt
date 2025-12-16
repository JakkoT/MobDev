package ee.ut.cs.iotbazaar.ui.camera

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel for the CameraFragment.
 * Currently serves as a placeholder for future camera-related logic.
 */
class CameraViewModel: ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Camera Fragment"
    }
    /**
     * LiveData for the text to be displayed in the camera screen.
     */
    val text: LiveData<String> = _text

}
