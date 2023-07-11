package com.example.expensemanager.ui

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.expensemanager.R
import com.google.firebase.auth.FirebaseAuth

class RegistrationActivity : BaseActivity() {
    private var mEmail: EditText? = null
    private var mPassword: EditText? = null
    private var signupButton: Button? = null
    private var mLogin: TextView? = null
    private var mDialog: ProgressDialog? = null


    //Firebase...
    private var mAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        mAuth = FirebaseAuth.getInstance()
        mDialog = ProgressDialog(this)
        signup()

    }



    private fun signup() {
        mEmail = findViewById(R.id.email_signup)
        mPassword = findViewById(R.id.password_signup)
        signupButton = findViewById(R.id.btn_signup)
        mLogin = findViewById(R.id.login)
        signupButton?.setOnClickListener(View.OnClickListener {
            val email = mEmail?.text.toString().trim { it <= ' ' }
            val password = mPassword?.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(email)) {
                mEmail?.error = "Email cannot be empty. Please enter a valid Email id"
                return@OnClickListener
            }
            if (TextUtils.isEmpty(password)) {
                mPassword?.error = "Password cannot be empty."
                return@OnClickListener
            }
            Log.i("val", email)
            Log.i("val", password)
            mDialog!!.setMessage("Please wait while we process your data")
            mDialog!!.show()
            mAuth!!.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mDialog!!.dismiss()
                    Toast.makeText(
                        applicationContext,
                        "Registration successful!",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(applicationContext, HomeActivity::class.java))
                } else {
                    mDialog!!.dismiss()
                    Toast.makeText(applicationContext, "Registration Failed!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        })
        mLogin?.setOnClickListener {
            startActivity(
                Intent(
                    applicationContext,
                    MainActivity::class.java
                )
            )
        }
    }
}