package ee.ut.cs.iotbazaar.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import ee.ut.cs.iotbazaar.R
import ee.ut.cs.iotbazaar.databinding.FragmentAccountBinding
import ee.ut.cs.iotbazaar.ui.home.ProfilePopupFragment
import ee.ut.cs.iotbazaar.ui.user.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import android.content.Intent
import ee.ut.cs.iotbazaar.ui.Login.LoginActivity

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by activityViewModels()

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

        binding.accountLogoutButton.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            auth.signOut()
            Toast.makeText(requireContext(), R.string.logged_out_message, Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_NO_HISTORY
            }
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
