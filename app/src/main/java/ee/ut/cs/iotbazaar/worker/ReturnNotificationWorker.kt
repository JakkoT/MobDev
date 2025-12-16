package ee.ut.cs.iotbazaar.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import ee.ut.cs.iotbazaar.R
import ee.ut.cs.iotbazaar.repository.ItemRepository
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

/**
 * Background worker responsible for checking item return deadlines and sending notifications.
 * Runs periodically or can be triggered manually for testing.
 */
class ReturnNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val repository = ItemRepository()

    /**
     * Executes the background work.
     * Checks borrowed items for the current user and sends notifications if they are due soon.
     *
     * @return The result of the work (Success, Failure, or Retry).
     */
    override suspend fun doWork(): Result {
        // Allow triggering a test notification manually
        if (inputData.getBoolean("is_test", false)) {
            sendNotification(9999, "this is test notification")
            return Result.success()
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            return Result.success()
        }

        try {
            val borrowedItems = repository.getUserBorrowedItemsInfoOneShot(userId)
            val currentTime = System.currentTimeMillis()

            borrowedItems.forEach { (itemId, returnDate) ->
                val diffMs = returnDate - currentTime
                // Calculate days remaining (rounding up)
                val daysLeft = ceil(diffMs.toDouble() / TimeUnit.DAYS.toMillis(1)).toInt()

                if (daysLeft in listOf(7, 3, 1, 0)) {
                    val item = repository.getItemById(itemId)
                    val itemName = item?.name ?: "Unknown Item"
                    val message = "Your item '$itemName' is due for return in $daysLeft day(s)."
                    sendNotification(itemId.hashCode(), message)
                }
            }

            return Result.success()
        } catch (_: Exception) {
            return Result.retry()
        }
    }

    /**
     * Sends a local notification to the user.
     *
     * @param notificationId Unique ID for the notification.
     * @param message The content text of the notification.
     */
    private fun sendNotification(notificationId: Int, message: String) {
        val context = applicationContext
        val channelId = "return_reminders"

        // Create channel if needed
        createNotificationChannel(context, channelId)

        val title = "Return Reminder"

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Make sure this resource exists or use a fallback
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        try {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat.from(context).notify(notificationId, builder.build())
            }
        } catch (_: SecurityException) {
            // Permission not granted
        }
    }

    /**
     * Creates the notification channel required for Android O and above.
     */
    private fun createNotificationChannel(context: Context, channelId: String) {
        val name = "Return Reminders"
        val descriptionText = "Notifications for item return deadlines"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
