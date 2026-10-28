package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.text.NumberFormat
import java.util.Locale

object AppUtils {
    fun formatIqdPrice(price: Long): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        return "${formatter.format(price)} د.ع"
    }

    fun dialPhoneNumber(context: Context, phone: String) {
        val cleanPhone = phone.replace(" ", "").replace("-", "")
        if (cleanPhone.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$cleanPhone")
            }
            context.startActivity(intent)
        }
    }
}
