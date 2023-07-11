package com.example.expensemanager.ui

import com.google.firebase.auth.FirebaseAuth
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import com.example.expensemanager.R
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseUser
import java.text.SimpleDateFormat
import java.util.*

class AccountFragment : Fragment() {
    private val mAuth: FirebaseAuth? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val myView = inflater.inflate(R.layout.fragment_account2, container, false)
        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val emailUser = view.findViewById<EditText>(R.id.email_account)
        val dateofCreation = view.findViewById<EditText>(R.id.dateofCreation)
        val timeOfCreation = view.findViewById<EditText>(R.id.timeOfCreation)
        val signInAt = view.findViewById<EditText>(R.id.lastSignInAt)
        val user = FirebaseAuth.getInstance().currentUser
        emailUser.setText(user!!.email)
        val timestampCreate = user.metadata!!.creationTimestamp
        val date1 = Date(timestampCreate)
        val jdf = SimpleDateFormat("dd MMM yyyy")
        val java_date = jdf.format(date1)
        val jdf1 = SimpleDateFormat("HH:mm:ss z")
        val TimeOfCreation = jdf1.format(date1)
        dateofCreation.setText(java_date)
        timeOfCreation.setText(TimeOfCreation)
        val lastSignInTS = user.metadata!!.lastSignInTimestamp
        val date2 = Date(lastSignInTS)
        val jdf2 = SimpleDateFormat("dd MMM yyyy    HH:mm:ss z")
        val SignInAt = jdf2.format(date2)
        signInAt.setText(SignInAt)
    }
}