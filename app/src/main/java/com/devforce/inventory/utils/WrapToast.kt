@file:Suppress("DEPRECATION")

package com.devforce.inventory.utils
import android.app.Activity
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.devforce.inventory.R

fun showCustomToast(message: Int, activity: Activity, toastType: String) {
    val layout = activity.layoutInflater.inflate(
        R.layout.custom_toast,
        activity.findViewById(R.id.toast_container)
    )

    val textView = layout.findViewById<TextView>(R.id.toast_text)
    textView.text = activity.getText(message)

    val cardView = layout.findViewById<CardView>(R.id.button_card_parent)

    // Set background color based on toastType
    val backgroundColorResId =
        when (toastType) {
            "success" -> R.color.notify_success
            "error" -> R.color.notify_error
            "info" -> R.color.notify_info
            "warning" -> R.color.notify_warning
        else -> R.color.orange
    }

    cardView.setCardBackgroundColor(ContextCompat.getColor(activity, backgroundColorResId))

    val toast = Toast(activity)
    toast.apply {
        duration = Toast.LENGTH_LONG
        view = layout
        show()
    }
}