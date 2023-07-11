package com.example.expensemanager.ui


import android.app.ProgressDialog
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.expensemanager.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : BaseActivity() {
    private var mEmail: EditText? = null
    private var mPassword: EditText? = null
    private var loginButton: Button? = null
    private var mForgotPassword: TextView? = null
    private var mSignup: TextView? = null
    private var mDialog: ProgressDialog? = null
    private var mAuth: FirebaseAuth? = null
    private var oneTapClient: SignInClient? = null
    private var signUpRequest: BeginSignInRequest? = null

    private var googleBtn: MaterialButton? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestGoogleSignIn()
        googleBtn = findViewById(R.id.btnGoogleSignUp)

        mAuth = FirebaseAuth.getInstance()
        if (mAuth!!.currentUser != null) {
            startActivity(Intent(applicationContext, HomeActivity::class.java))
            finish()
        }
        mDialog = ProgressDialog(this)
        login()
        googleBtn?.setOnClickListener {
            oneTapUI()
        }
    }

    private fun requestGoogleSignIn() {
        oneTapClient = Identity.getSignInClient(this)
        signUpRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()
    }

    private fun oneTapUI() {
        oneTapClient?.beginSignIn(signUpRequest!!)
            ?.addOnSuccessListener { result ->
                try {
                    startIntentSenderForResult(
                        result.pendingIntent.intentSender,
                        1,
                        null,
                        0,
                        0,
                        0
                    )
                } catch (e: IntentSender.SendIntentException) {
                    e.printStackTrace()
                    Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT)
                        .show()
                }
            }?.addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error while Creating account" + e.message,
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("FAIL", "" + e.message)
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            1 -> try {
                val credential = oneTapClient!!.getSignInCredentialFromIntent(data)
                val idToken = credential.googleIdToken
                if (idToken != null) {
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    mAuth?.signInWithCredential(firebaseCredential)?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this,
                                "Login success with Google",
                                Toast.LENGTH_SHORT
                            ).show()
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()

                        } else {
                            Toast.makeText(
                                this,
                                "Login Failure" + task.exception,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (e: ApiException) {
                e.printStackTrace()
            }
        }

    }

    private fun login() {
        mEmail = findViewById(R.id.email_login)
        mPassword = findViewById(R.id.password_login)
        loginButton = findViewById(R.id.btn_login)
        mForgotPassword = findViewById(R.id.forgot_password)
        mSignup = findViewById(R.id.signup)
        loginButton?.setOnClickListener(View.OnClickListener {
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
            mDialog!!.setMessage("Processing")
            mDialog!!.show()
            mAuth!!.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mDialog!!.dismiss()
                    Toast.makeText(applicationContext, "Login Successful!", Toast.LENGTH_SHORT)
                        .show()
                    startActivity(Intent(applicationContext, HomeActivity::class.java))
                    finish()
                } else {
                    mDialog!!.dismiss()
                    Toast.makeText(applicationContext, "Login Failed!", Toast.LENGTH_SHORT).show()
                }
            }
        })
        // Redirect to SignUp activity
        mSignup?.setOnClickListener {
            startActivity(
                Intent(
                    applicationContext,
                    RegistrationActivity::class.java
                )
            )
        }
        // Redirect to reset password activity
        mForgotPassword?.setOnClickListener {
            startActivity(
                Intent(
                    applicationContext,
                    ResetPasswordActivity::class.java
                )
            )
        }
    }
}