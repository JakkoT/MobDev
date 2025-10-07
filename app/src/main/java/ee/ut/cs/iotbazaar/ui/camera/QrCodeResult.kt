package ee.ut.cs.iotbazaar.ui.camera



import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import ee.ut.cs.iotbazaar.R

class QrCodeResultFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_qr_code_result, container, false)
        val textView = view.findViewById<TextView>(R.id.qrResultText)
        val qrValue = arguments?.getString("qrValue") ?: "Väärtus puudub"
        textView.text = "Skaneeritud väärtus:\n$qrValue"
        return view
    }
}
