package ee.ut.cs.iotbazaar.ui.Login

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

fun saveUserToFirestore() {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser ?: return
    val db = FirebaseFirestore.getInstance()

    val userData = mapOf(
        "uid" to user.uid,
        "email" to user.email,
        "name" to (user.displayName ?: "Anonymous"),
        "takenItems" to emptyList<Map<String, Any>>(),
        "createdAt" to System.currentTimeMillis()
    )

    val userRef = db.collection("users_real").document(user.uid)
    userRef.get().addOnSuccessListener { doc ->
        if (!doc.exists()) {
            userRef.set(userData, SetOptions.merge())
        }
    }
}
