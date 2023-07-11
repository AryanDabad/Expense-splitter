package com.example.expensemanager.ui

import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import com.example.expensemanager.R
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.EditText
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.example.expensemanager.Model.Data
import com.example.expensemanager.utils.DialogUtils
import com.google.firebase.database.*
import java.text.DateFormat
import java.util.*

class DashboardFragment : Fragment() {
    //Floating Button
    private var fab_main: FloatingActionButton? = null
    private var fab_income: FloatingActionButton? = null
    private var fab_expense: FloatingActionButton? = null

    //Floating Button TextView
    private var fab_income_text: TextView? = null
    private var fab_expense_text: TextView? = null
    private var isOpen = false

    // animation class objects
    private var fadeOpen: Animation? = null
    private var fadeClose: Animation? = null

    //Dashboard income and expense result
    private var totalIncomResult: TextView? = null
    private var totalExpenseResult: TextView? = null

    // Firebase
    private var mAuth: FirebaseAuth? = null
    private var mIncomeDatabase: DatabaseReference? = null
    private var mExpenseDatabase: DatabaseReference? = null

    //Recycler view
    private var mRecyclerIncome: RecyclerView? = null
    private var mRecyclerExpense: RecyclerView? = null

    private var mDialog: DialogUtils? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val myview = inflater.inflate(R.layout.fragment_dashboard, container, false)
        mDialog = DialogUtils(requireContext())
        mAuth = FirebaseAuth.getInstance()
        val mUser = mAuth!!.currentUser
        val uid = mUser!!.uid
        mIncomeDatabase = FirebaseDatabase.getInstance().reference.child("IncomeData").child(uid)
        mExpenseDatabase = FirebaseDatabase.getInstance().reference.child("ExpenseData").child(uid)
        mIncomeDatabase!!.keepSynced(true)
        mExpenseDatabase!!.keepSynced(true)
        //Connect Floating Button to layout
        fab_main = myview.findViewById(R.id.fb_main_plus_btn)
        fab_income = myview.findViewById(R.id.income_ft_btn)
        fab_expense = myview.findViewById(R.id.expense_ft_btn)

        // Connect floating text
        fab_income_text = myview.findViewById(R.id.income_ft_text)
        fab_expense_text = myview.findViewById(R.id.expense_ft_text)

        //Total income and expense
        totalIncomResult = myview.findViewById(R.id.income_set_result)
        totalExpenseResult = myview.findViewById(R.id.expense_set_result)

        //Recycler
        mRecyclerIncome = myview.findViewById(R.id.recycler_income)
        mRecyclerExpense = myview.findViewById(R.id.recycler_expense)

        //Animations
        fadeOpen = AnimationUtils.loadAnimation(activity, R.anim.fade_open)
        fadeClose = AnimationUtils.loadAnimation(activity, R.anim.fade_close)
        fab_main!!.setOnClickListener(View.OnClickListener {
            addData()
            floatingButtonAnimation()
        })

        //Calculate total income
        mIncomeDatabase!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var total = 0
                for (mysnap in snapshot.children) {
                    val data = mysnap.getValue(
                        Data::class.java
                    )
                    total += data!!.amount
                    val stResult = total.toString()
                    totalIncomResult!!.setText("$stResult.00")
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        //Calculate total expense
        mExpenseDatabase!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var total = 0
                for (mysnap in snapshot.children) {
                    val data = mysnap.getValue(
                        Data::class.java
                    )
                    total += data!!.amount
                    val stResult = total.toString()
                    totalExpenseResult!!.setText("$stResult.00")
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        //Recycler
        val layoutManagerIncome =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        layoutManagerIncome.stackFromEnd = true
        layoutManagerIncome.reverseLayout = true
        mRecyclerIncome!!.setHasFixedSize(true)
        mRecyclerIncome!!.setLayoutManager(layoutManagerIncome)
        val layoutManagerExpense =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        layoutManagerExpense.stackFromEnd = true
        layoutManagerExpense.reverseLayout = true
        mRecyclerExpense!!.setHasFixedSize(true)
        mRecyclerExpense!!.setLayoutManager(layoutManagerExpense)
        return myview
    }

    //Floating button animation
    private fun floatingButtonAnimation() {
        if (isOpen) {
            fab_income!!.startAnimation(fadeClose)
            fab_expense!!.startAnimation(fadeClose)
            fab_income!!.isClickable = false
            fab_expense!!.isClickable = false
            fab_income_text!!.startAnimation(fadeClose)
            fab_expense_text!!.startAnimation(fadeClose)
            fab_income_text!!.isClickable = false
            fab_expense_text!!.isClickable = false
        } else {
            fab_income!!.startAnimation(fadeOpen)
            fab_expense!!.startAnimation(fadeOpen)
            fab_income!!.isClickable = true
            fab_expense!!.isClickable = true
            fab_expense_text!!.startAnimation(fadeOpen)
            fab_income_text!!.startAnimation(fadeOpen)
            fab_income_text!!.isClickable = true
            fab_expense_text!!.isClickable = true
        }
        isOpen = !isOpen
    }

    private fun addData() {
        //Fab Button Income
        fab_income!!.setOnClickListener { insertIncomeData() }
        fab_expense!!.setOnClickListener { insertExpenseData() }
    }

    fun insertIncomeData() {
        val inflater = LayoutInflater.from(activity)
        val myview = inflater.inflate(R.layout.custom_layout_for_insertdata, null)
        mDialog?.customDialog(myview, false)

        val edtamount = myview.findViewById<EditText>(R.id.amount)
        val edtType = myview.findViewById<EditText>(R.id.type_edt)
        val edtNote = myview.findViewById<EditText>(R.id.note_edt)
        val saveBtn = myview.findViewById<Button>(R.id.btnSave)
        val cancelBtn = myview.findViewById<Button>(R.id.btnCancel)
        saveBtn.setOnClickListener(View.OnClickListener {
            val type = edtType.text.toString().trim { it <= ' ' }
            val amount = edtamount.text.toString().trim { it <= ' ' }
            val note = edtNote.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(type)) {
                edtType.error = "Please Enter A Type"
                return@OnClickListener
            }
            if (TextUtils.isEmpty(amount)) {
                edtamount.error = "Please Enter Amount"
                return@OnClickListener
            }
            if (TextUtils.isEmpty(note)) {
                edtNote.error = "Please Enter A Note"
                return@OnClickListener
            }
            val amountInInt = amount.toInt()

            //Create random ID inside database
            val id = mIncomeDatabase!!.push().key
            val mDate = DateFormat.getDateInstance().format(Date())
            val data = Data(amountInInt, type, note, id, mDate)
            mIncomeDatabase!!.child(id!!).setValue(data)
            Toast.makeText(activity, "Transaction Added Successfully!", Toast.LENGTH_SHORT).show()
            mDialog?.dismissDialog()
            floatingButtonAnimation()
        })
        cancelBtn.setOnClickListener {
            floatingButtonAnimation()
            mDialog?.dismiss()
        }
    }

    fun insertExpenseData() {

        val inflater = LayoutInflater.from(activity)
        val myview = inflater.inflate(R.layout.custom_layout_for_insertdata, null)
        mDialog?.customDialog(myview, false)

        val edtamount = myview.findViewById<EditText>(R.id.amount)
        val edttype = myview.findViewById<EditText>(R.id.type_edt)
        val edtnote = myview.findViewById<EditText>(R.id.note_edt)
        val saveBtn = myview.findViewById<Button>(R.id.btnSave)
        val cancelBtn = myview.findViewById<Button>(R.id.btnCancel)
        saveBtn.setOnClickListener(View.OnClickListener {
            val amount = edtamount.text.toString().trim { it <= ' ' }
            val type = edttype.text.toString().trim { it <= ' ' }
            val note = edtnote.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(type)) {
                edttype.error = "Please Enter A Type"
                return@OnClickListener
            }
            if (TextUtils.isEmpty(amount)) {
                edtamount.error = "Please Enter Amount"
                return@OnClickListener
            }
            if (TextUtils.isEmpty(note)) {
                edtnote.error = "Please Enter A Note"
                return@OnClickListener
            }
            val amountInInt = amount.toInt()

            //Create random ID inside database
            val id = mExpenseDatabase!!.push().key
            val mDate = DateFormat.getDateInstance().format(Date())
            val data = Data(amountInInt, type, note, id, mDate)
            mExpenseDatabase!!.child(id!!).setValue(data)
            Toast.makeText(activity, "Transaction Added Successfully!", Toast.LENGTH_SHORT).show()
            mDialog?.dismiss()
            floatingButtonAnimation()
        })
        cancelBtn.setOnClickListener {
            mDialog?.dismiss()
            floatingButtonAnimation()
        }
    }

    override fun onStart() {
        super.onStart()
        val incomeAdapter: FirebaseRecyclerAdapter<Data, IncomeViewHolder> =
            object : FirebaseRecyclerAdapter<Data, IncomeViewHolder>(
                Data::class.java,
                R.layout.dashboard_income,
                IncomeViewHolder::class.java,
                mIncomeDatabase
            ) {
                override fun populateViewHolder(
                    incomeViewHolder: IncomeViewHolder,
                    data: Data,
                    i: Int
                ) {
                    incomeViewHolder.setIncomeType(data.type)
                    incomeViewHolder.setIncomeAmount(data.amount)
                    incomeViewHolder.setIncomeDate(data.date)
                }
            }
        mRecyclerIncome!!.adapter = incomeAdapter
        val expenseAdapter: FirebaseRecyclerAdapter<Data, ExpenseViewHolder> =
            object : FirebaseRecyclerAdapter<Data, ExpenseViewHolder>(
                Data::class.java,
                R.layout.dashboard_expense,
                ExpenseViewHolder::class.java,
                mExpenseDatabase
            ) {
                override fun populateViewHolder(
                    expenseViewHolder: ExpenseViewHolder,
                    data: Data,
                    i: Int
                ) {
                    expenseViewHolder.setExpenseType(data.type)
                    expenseViewHolder.setExpenseAmount(data.amount)
                    expenseViewHolder.setExpenseDate(data.date)
                }
            }
        mRecyclerExpense!!.adapter = expenseAdapter
    }

    // For income Data
    class IncomeViewHolder(var mIncomeView: View) : RecyclerView.ViewHolder(
        mIncomeView
    ) {
        fun setIncomeType(type: String?) {
            val mtype = mIncomeView.findViewById<TextView>(R.id.type_Income_ds)
            Log.i("TYPE", type!!)
            mtype.text = type
        }

        fun setIncomeAmount(amount: Int) {
            val mAmount = mIncomeView.findViewById<TextView>(R.id.amount_Income_ds)
            val strAmount = amount.toString()
            Log.i("AMOUNT", strAmount)
            mAmount.text = strAmount
        }

        fun setIncomeDate(date: String?) {
            val mDate = mIncomeView.findViewById<TextView>(R.id.date_Income_ds)
            Log.i("DATE", date!!)
            mDate.text = date
        }
    }

    // For expense Data
    class ExpenseViewHolder(var mExpenseView: View) : RecyclerView.ViewHolder(
        mExpenseView
    ) {
        fun setExpenseType(type: String?) {
            val mtype = mExpenseView.findViewById<TextView>(R.id.type_Expense_ds)
            mtype.text = type
        }

        fun setExpenseAmount(amount: Int) {
            val mAmount = mExpenseView.findViewById<TextView>(R.id.amount_Expense_ds)
            val strAmount = amount.toString()
            mAmount.text = strAmount
        }

        fun setExpenseDate(date: String?) {
            val mDate = mExpenseView.findViewById<TextView>(R.id.date_Expense_ds)
            mDate.text = date
        }
    }
}