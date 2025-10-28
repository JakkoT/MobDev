package ee.ut.cs.iotbazaar

import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import ee.ut.cs.iotbazaar.databinding.ActivityMainBinding
import ee.ut.cs.iotbazaar.ui.user.UserViewModel
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AlertDialog


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.topAppBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val navView: BottomNavigationView = binding.navView

        // Request camera permission at runtime
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 101)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.navigation_home))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, _, _ ->
            binding.topAppBar.title = ""
            binding.topAppBar.navigationIcon = null
        }

        // Explicit navigation handling to ensure Home always works
        navView.setOnItemSelectedListener { item ->
            val destId = when (item.itemId) {
                R.id.navigation_home -> R.id.navigation_home
                R.id.navigation_catalog -> R.id.navigation_catalog
                R.id.navigation_inbox -> R.id.navigation_inbox
                else -> null
            }
            destId?.let {
                if (navController.currentDestination?.id != it) {
                    try {
                        navController.navigate(it)
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Navigation error to $it", e)
                    }
                }
                true
            } ?: false
        }

        viewModel = ViewModelProvider(this)[UserViewModel::class.java]

        // Observe users LiveData and log changes
        viewModel.users.observe(this) { users ->
            Log.d("RoomExample", "Users: $users")
        }

        // Seed a default user if none exists
        viewModel.addUserIfNotExists("Jakko", 22)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
    override fun onStart() {
        super.onStart()

        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()

        // Kui kasutaja pole sisse loginud → suuna tagasi LoginActivity-sse
        if (auth.currentUser == null) {
            val intent = android.content.Intent(this, ee.ut.cs.iotbazaar.ui.Login.LoginActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } else if (!isInternetAvailable()) {
            // Kui internet puudub → log out ja suuna LoginActivity-sse
            handleNoInternet()
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    private fun handleNoInternet() {
        AlertDialog.Builder(this)
            .setTitle("No Internet Connection")
            .setMessage("You are offline. Please check your internet connection.")
            .setPositiveButton("OK") { _, _ ->
                // Log out Firebase user
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                auth.signOut()
                // Suuna LoginActivity-sse
                val intent = android.content.Intent(this, ee.ut.cs.iotbazaar.ui.Login.LoginActivity::class.java)
                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setCancelable(false)
            .show()
    }



}
