package ee.ut.cs.iotbazaar.ui.account

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import ee.ut.cs.iotbazaar.R

// AccountFragment for displaying user account information.
class AccountFragment : Fragment(R.layout.fragment_account) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Profile icon moved to global ActionBar menu; no local view references needed.
    }
}
