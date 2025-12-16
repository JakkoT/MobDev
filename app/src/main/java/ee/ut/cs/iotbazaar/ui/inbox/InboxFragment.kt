package ee.ut.cs.iotbazaar.ui.inbox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import ee.ut.cs.iotbazaar.databinding.FragmentInboxBinding
import ee.ut.cs.iotbazaar.ui.home.ProfilePopupFragment
import ee.ut.cs.iotbazaar.ui.user.UserViewModel
import androidx.navigation.fragment.findNavController
import android.widget.Toast
import ee.ut.cs.iotbazaar.api.RetrofitClient
import ee.ut.cs.iotbazaar.model.Quote
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Fragment for the Inbox screen.
 * Displays a list of users (simulating an inbox) and fetches a random quote from an API.
 */
class InboxFragment : Fragment() {

    private var _binding: FragmentInboxBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserViewModel by activityViewModels()
    private lateinit var inboxAdapter: InboxAdapter

    /**
     * Inflates the layout for this fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInboxBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Called immediately after onCreateView.
     * Sets up the RecyclerView, observes user data, and fetches a random quote.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inboxAdapter = InboxAdapter()
        binding.userRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = inboxAdapter
        }

        binding.inboxProfileButton.setOnClickListener {
            ProfilePopupFragment().show(parentFragmentManager, "ProfilePopupFragment")
        }

        binding.inboxBackButton.setOnClickListener {
            findNavController().navigateUp()
        }

        viewModel.users.observe(viewLifecycleOwner) { users ->
            val distinct = users
                .filter { !it.name.equals("Anonymous", ignoreCase = true) }
                .distinctBy { it.name }
            inboxAdapter.submitList(distinct)
        }

        fetchRandomQuote()
    }

    /**
     * Fetches a random quote from the external API using Retrofit.
     * Updates the UI with the quote or an error message.
     */
    private fun fetchRandomQuote() {
        // Show loading state
        binding.MOTD.text = "Loading quote..."

        // Make API call
        RetrofitClient.quoteApiService.getRandomQuote().enqueue(object : Callback<Quote> {
            // Handle successful response
            override fun onResponse(call: Call<Quote>, response: Response<Quote>) {
                if (response.isSuccessful && response.body() != null) {
                    val quote = response.body()!!
                    binding.MOTD.text = "\"${quote.quote}\"\n- ${quote.author}"
                } else {
                    binding.MOTD.text = "Failed to load quote. Please try again later."
                }
            }
            // Handle failure
            override fun onFailure(call: Call<Quote>, t: Throwable) {
                binding.MOTD.text = "No internet connection. Please check your network."
                Toast.makeText(
                    requireContext(),
                    "Error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
