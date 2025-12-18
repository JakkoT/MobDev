package ee.ut.cs.iotbazaar.ui.account

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import ee.ut.cs.iotbazaar.R
import ee.ut.cs.iotbazaar.databinding.FragmentAccountBinding
import ee.ut.cs.iotbazaar.ui.Login.LoginActivity
import ee.ut.cs.iotbazaar.ui.home.ProfilePopupFragment
import ee.ut.cs.iotbazaar.ui.user.UserViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/**
 * Fragment for the Account screen.
 * Displays user account information, allows changing name, and provides logout functionality.
 */
class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by activityViewModels()

    private val firestore = FirebaseFirestore.getInstance()
    private var userListener: ListenerRegistration? = null

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.accountProfileButton.setOnClickListener {
            ProfilePopupFragment().show(parentFragmentManager, "ProfilePopupFragment")
        }

        binding.accountBackButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Listen to fire base
        val uid = auth.currentUser?.uid
        if (uid != null) {
            userListener = firestore.collection("users_real")
                .document(uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Toast.makeText(requireContext(), "Failed to load user info", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val name = snapshot.getString("name") ?: "Anonymous"
                        val age = snapshot.getLong("age")?.toInt() ?: 0
                        binding.accountCurrentNameText.text = "Your name: $name"
                        binding.accountInsights.text = "Age: $age. Reserved items you are tracking appear on the home screen."
                    }
                }
        }

        // Change name button
        binding.accountChangeNameButton.setOnClickListener {
            val newName = binding.accountNameEditText.text.toString().trim()
            if (newName.isEmpty()) {
                Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firestore update
            if (uid != null) {
                firestore.collection("users_real")
                    .document(uid)
                    .update("name", newName)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Name updated", Toast.LENGTH_SHORT).show()
                        binding.accountNameEditText.text.clear()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to update name", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // Logout
        binding.accountLogoutButton.setOnClickListener {
            auth.signOut()
            Toast.makeText(requireContext(), R.string.logged_out_message, Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY
            }
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        userListener?.remove()
        _binding = null
    }
}

