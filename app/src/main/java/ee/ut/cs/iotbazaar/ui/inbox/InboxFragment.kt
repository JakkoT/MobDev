package ee.ut.cs.iotbazaar.ui.inbox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import ee.ut.cs.iotbazaar.R
import ee.ut.cs.iotbazaar.databinding.FragmentInboxBinding
import ee.ut.cs.iotbazaar.ui.user.UserViewModel

class InboxFragment : Fragment() {

    private var _binding: FragmentInboxBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserViewModel by activityViewModels()
    private lateinit var inboxAdapter: InboxAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInboxBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inboxAdapter = InboxAdapter()
        binding.userRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = inboxAdapter
        }

        viewModel.users.observe(viewLifecycleOwner) { users ->
            val distinct = users.distinctBy { it.name }
            inboxAdapter.submitList(distinct)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
