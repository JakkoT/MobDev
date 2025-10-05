package ee.ut.cs.iotbazaar.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.navigation.findNavController
import ee.ut.cs.iotbazaar.R

class ProfilePopupFragment : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_popup, container, false)
        val account = view.findViewById<TextView>(R.id.profile_account)
        val inbox = view.findViewById<TextView>(R.id.profile_inbox)
        val settings = view.findViewById<TextView>(R.id.profile_settings)
        val logout = view.findViewById<TextView>(R.id.profile_logout)

        fun navigate(destinationId: Int) {
            // Use activity nav controller to ensure we target the main NavHost
            requireActivity().findNavController(R.id.nav_host_fragment_activity_main).navigate(destinationId)
            dismiss()
        }

        account.setOnClickListener { navigate(R.id.navigation_account) }
        inbox.setOnClickListener { navigate(R.id.navigation_inbox) }
        settings.setOnClickListener { navigate(R.id.navigation_settings) }
        logout.setOnClickListener {
            Toast.makeText(context, "Log Out clicked", Toast.LENGTH_SHORT).show()
            dismiss()
        }
        return view
    }
}
