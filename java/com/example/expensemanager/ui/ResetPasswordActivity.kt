package com.example.expensemanager.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.expensemanager.R
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : BaseActivity() {
    private var mAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        setContentView(R.layout.activity_reset_password)
        val mailText = findViewById<EditText>(R.id.forgot_password_email)
        val sendEmail_btn = findViewById<Button>(R.id.btn_reset_password)
        sendEmail_btn.setOnClickListener {
            val email = mailText.text.toString().trim { it <= ' ' }
            mAuth!!.sendPasswordResetEmail(email).addOnSuccessListener {
                Toast.makeText(
                    applicationContext,
                    "Please Check your mail for Password reset Instructions",
                    Toast.LENGTH_LONG
                ).show()
                startActivity(Intent(applicationContext, MainActivity::class.java))
            }.addOnFailureListener { e ->
                Toast.makeText(
                    applicationContext,
                    "Error! Reset Link is not sent!. " + e.message,
                    Toast.LENGTH_LONG
                ).show()
                startActivity(Intent(applicationContext, MainActivity::class.java))
            }
        }
    }
}