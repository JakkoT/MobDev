package ee.ut.cs.iotbazaar.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import ee.ut.cs.iotbazaar.R
import ee.ut.cs.iotbazaar.ui.Login.LoginActivity

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
        val auth = FirebaseAuth.getInstance()
        logout.setOnClickListener {
            Toast.makeText(requireContext(), "Log Out clicked", Toast.LENGTH_SHORT).show()

            auth.signOut()

            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_NO_HISTORY
            startActivity(intent)  // 👈 see oli puudu
            dismiss()              // sulgeb popupi, kui see on DialogFragment
        }
        return view
    }

    override fun onStart() {
        super.onStart()
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
            isDraggable = true
        }
    }
}
