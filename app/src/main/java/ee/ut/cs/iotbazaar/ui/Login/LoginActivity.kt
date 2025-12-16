package ee.ut.cs.iotbazaar.ui.Login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import ee.ut.cs.iotbazaar.MainActivity
import ee.ut.cs.iotbazaar.R
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AlertDialog
import ee.ut.cs.iotbazaar.theme.ThemePreferences


/**
 * Activity responsible for user authentication (Login).
 * Supports Email/Password login and Google Sign-In.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    /**
     * Called when the activity is starting.
     * Initializes UI components, sets up click listeners for login actions, and configures Google Sign-In.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply user-selected theme before view inflation
        ThemePreferences.applySavedMode(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // 🔹 Google sign-in konfiguratsioon
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val emailField = findViewById<EditText>(R.id.emailField)
        val passwordField = findViewById<EditText>(R.id.passwordField)
        val loginBtn = findViewById<Button>(R.id.loginBtn)
        val googleSignInBtn = findViewById<com.google.android.gms.common.SignInButton>(R.id.googleSignInBtn)
        val registerRedirectBtn = findViewById<Button>(R.id.registerRedirectBtn)
        registerRedirectBtn.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        loginBtn.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            // Validate form first (deterministic UI feedback without network)
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Provide all the information", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Then check connectivity before remote calls
            if (!isInternetAvailable()) {
                showNoInternetDialog()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Wrong email or password", Toast.LENGTH_SHORT).show()
                    }
                }
        }


        // 🔹 Google login nupule vajutus
        googleSignInBtn.setOnClickListener {
            if (!isInternetAvailable()) {
                showNoInternetDialog()
                return@setOnClickListener
            }

            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

    }

    /**
     * Launcher for the Google Sign-In activity result.
     * Handles the result of the sign-in intent.
     */
    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Authenticates with Firebase using the Google account credentials.
     *
     * @param acct The GoogleSignInAccount object containing the user's ID token.
     */
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    saveUserToFirestore()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    /**
     * Checks if the device has an active internet connection.
     *
     * @return True if internet is available, false otherwise.
     */
    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Displays a dialog informing the user that there is no internet connection.
     */
    private fun showNoInternetDialog() {
        AlertDialog.Builder(this)
            .setTitle("No Internet Connection")
            .setMessage("Please check your internet connection and try again.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .show()
    }
}
