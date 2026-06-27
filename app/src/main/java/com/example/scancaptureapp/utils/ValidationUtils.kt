package com.example.scancaptureapp.utils

import android.content.Context
import android.util.Patterns
import com.example.scancaptureapp.R

object ValidationUtils {

    fun validateEmail(context: Context, email: String): String? = when {
        email.isBlank() -> context.getString(R.string.error_email_required)
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> context.getString(R.string.error_email_invalid)
        else -> null
    }

    fun validatePassword(context: Context, password: String): String? = when {
        password.isBlank() -> context.getString(R.string.error_password_required)
        password.length < 6 -> context.getString(R.string.error_password_min_length)
        else -> null
    }

    fun validateName(context: Context, name: String): String? = when {
        name.isBlank() -> context.getString(R.string.error_name_required)
        name.length < 2 -> context.getString(R.string.error_name_min_length)
        else -> null
    }

    fun validateConfirmPassword(context: Context, password: String, confirm: String): String? = when {
        confirm.isBlank() -> context.getString(R.string.error_confirm_password_required)
        confirm != password -> context.getString(R.string.error_passwords_mismatch)
        else -> null
    }
}
