package ee.ut.cs.iotbazaar.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ee.ut.cs.iotbazaar.databinding.FragmentSettingsBinding
import ee.ut.cs.iotbazaar.ui.home.ProfilePopupFragment
import androidx.navigation.fragment.findNavController
import ee.ut.cs.iotbazaar.theme.ThemePreferences

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.settingsProfileButton.setOnClickListener {
            ProfilePopupFragment().show(parentFragmentManager, "ProfilePopupFragment")
        }

        binding.settingsBackButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Initialize dark mode switch state
        val isDark = ThemePreferences.isDarkEnabled(requireContext())
        binding.darkModeSwitch.isChecked = isDark

        // Toggle listener to persist and apply theme
        binding.darkModeSwitch.setOnCheckedChangeListener { _, checked ->
            // persist + apply; AppCompat will recreate the activity automatically
            ThemePreferences.setDarkEnabled(requireContext(), checked)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
