package com.example.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.example.NeverForgetApp
import com.example.data.model.LocationTrigger
import com.example.data.model.TaskEntity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class PresetLocation(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val icon: String
)

class LocationReminderManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    companion object {
        val PRESET_LOCATIONS = listOf(
            PresetLocation("Home (বাসা)", 23.8103, 90.4125, "home"),
            PresetLocation("Office / Workplace (অফিস)", 23.7925, 90.4078, "business"),
            PresetLocation("Supermarket / Market (বাজার/সুপারশপ)", 23.8041, 90.3667, "shopping_cart"),
            PresetLocation("Gym / Fitness Center (জিম)", 23.7808, 90.4192, "fitness_center"),
            PresetLocation("Library / Campus (লাইব্রেরি/ক্যাম্পাস)", 23.7330, 90.3934, "menu_book"),
            PresetLocation("Hospital / Clinic (হাসপাতাল)", 23.7509, 90.3935, "local_hospital")
        )
    }

    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation(
        onSuccess: (lat: Double, lng: Double) -> Unit,
        onError: (message: String) -> Unit
    ) {
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        onSuccess(location.latitude, location.longitude)
                    } else {
                        // Fallback to last location
                        fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                            if (lastLoc != null) {
                                onSuccess(lastLoc.latitude, lastLoc.longitude)
                            } else {
                                onError("Unable to get current GPS location. Please ensure location services are enabled.")
                            }
                        }.addOnFailureListener {
                            onError(it.localizedMessage ?: "Failed to retrieve location")
                        }
                    }
                }
                .addOnFailureListener {
                    onError(it.localizedMessage ?: "Location request failed")
                }
        } catch (e: SecurityException) {
            onError("Location permission not granted.")
        }
    }

    /**
     * Checks all pending location tasks against the given coordinates
     * and triggers notifications for arriving or leaving within the task radius.
     */
    fun evaluateLocationTasks(currentLat: Double, currentLng: Double) {
        val app = context.applicationContext as? NeverForgetApp ?: return
        CoroutineScope(Dispatchers.IO).launch {
            val locationTasks = app.repository.getPendingLocationTasksSync()
            val results = FloatArray(1)

            for (task in locationTasks) {
                val taskLat = task.latitude ?: continue
                val taskLng = task.longitude ?: continue

                Location.distanceBetween(currentLat, currentLng, taskLat, taskLng, results)
                val distanceInMeters = results[0]
                val radius = task.locationRadiusMeters

                Log.d("LocationReminder", "Task '${task.title}' distance to user: ${distanceInMeters}m (radius: ${radius}m)")

                if (task.locationTrigger == LocationTrigger.ON_ENTER) {
                    if (distanceInMeters <= radius && !task.isLocationTriggered) {
                        app.notificationHelper.showLocationReminderNotification(task, isArrival = true)
                        app.repository.updateTask(task.copy(isLocationTriggered = true))
                    }
                } else if (task.locationTrigger == LocationTrigger.ON_EXIT) {
                    if (distanceInMeters > radius && task.isLocationTriggered) {
                        app.notificationHelper.showLocationReminderNotification(task, isArrival = false)
                        app.repository.updateTask(task.copy(isLocationTriggered = false))
                    } else if (distanceInMeters <= radius && !task.isLocationTriggered) {
                        // Mark inside so exit can trigger later
                        app.repository.updateTask(task.copy(isLocationTriggered = true))
                    }
                }
            }
        }
    }
}
