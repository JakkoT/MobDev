package ee.ut.cs.iotbazaar

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import ee.ut.cs.iotbazaar.databinding.ActivityMainBinding
import ee.ut.cs.iotbazaar.ui.home.ProfilePopupFragment
import ee.ut.cs.iotbazaar.ui.user.UserViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        // Request camera permission at runtime
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 101)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Only Home is top-level now so Catalogue & Notifications show back arrow
        val appBarConfiguration = AppBarConfiguration(setOf(R.id.navigation_home))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        viewModel = ViewModelProvider(this)[UserViewModel::class.java]

        // Observe users LiveData and log changes
        viewModel.users.observe(this) { users ->
            Log.d("RoomExample", "Users: $users")
        }

        // Seed a default user if none exists
        viewModel.addUserIfNotExists("Jakko", 22)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                ProfilePopupFragment().show(supportFragmentManager, "ProfilePopupFragment")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}