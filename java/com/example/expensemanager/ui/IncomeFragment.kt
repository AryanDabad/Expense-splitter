package com.example.expensemanager.ui

import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.widget.EditText
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.expensemanager.R
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensemanager.Model.Data
import com.example.expensemanager.utils.DialogUtils
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.*
import java.text.DateFormat
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [IncomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class IncomeFragment : Fragment() {


    //Firebase database
    private var mAuth: FirebaseAuth? = null
    private var mIncomeDatabase: DatabaseReference? = null

    //Recyclerview
    private var recyclerView: RecyclerView? = null

    //Text view
    private var incomeTotalSum: TextView? = null

    //Update edit text
    private var edtAmount: EditText? = null
    private var edtType: EditText? = null
    private var edtNote: EditText? = null

    //button for update and delete
    private var btnUpdate: Button? = null
    private var btnDelete: Button? = null

    //Data item value
    private var type: String? = null
    private var note: String? = null
    private var amount = 0f
    private var post_key: String? = null
    private var mDialogUtils: DialogUtils? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val myview = inflater.inflate(R.layout.fragment_income, container, false)
        mAuth = FirebaseAuth.getInstance()
        val mUser = mAuth!!.currentUser
        val uid = mUser!!.uid
        mIncomeDatabase = FirebaseDatabase.getInstance().reference.child("IncomeData").child(uid)
        incomeTotalSum = myview.findViewById(R.id.income_txt_result)
        recyclerView = myview.findViewById(R.id.recycler_id_income)
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        recyclerView?.setHasFixedSize(true)
        recyclerView?.setLayoutManager(layoutManager)
        mIncomeDatabase!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalvalue = 0
                for (mysnapshot in snapshot.children) {
                    val data = mysnapshot.getValue(
                        Data::class.java
                    )
                    totalvalue += data!!.amount
                    val sTotalvalue = totalvalue.toString()
                    incomeTotalSum?.setText("$sTotalvalue.00")
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        return myview
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mDialogUtils= DialogUtils(requireContext())
    }
    override fun onStart() {
        super.onStart()
        val adapter: FirebaseRecyclerAdapter<Data, MyViewHolder> =
            object : FirebaseRecyclerAdapter<Data, MyViewHolder>(
                Data::class.java,
                R.layout.income_recycler_data,
                MyViewHolder::class.java,
                mIncomeDatabase
            ) {
                override fun populateViewHolder(
                    viewHolder: MyViewHolder,
                    model: Data,
                    position: Int
                ) {
                    viewHolder.setType(model.type!!)
                    viewHolder.setNote(model.note!!)
                    viewHolder.setDate(model.date!!)
                    viewHolder.setAmount(model.amount.toFloat())
                    viewHolder.mView.setOnClickListener {
                        post_key = getRef(position).key
                        type = model.type
                        note = model.note
                        amount = model.amount.toFloat()
                        updateDataItem()
                    }
                }
            }
        recyclerView!!.adapter = adapter
    }

    class MyViewHolder(var mView: View) : RecyclerView.ViewHolder(
        mView
    ) {
        fun setType(type: String) {
            val mType = mView.findViewById<TextView>(R.id.type_txt_income)
            mType.text = type
        }

        fun setNote(note: String) {
            val mNote = mView.findViewById<TextView>(R.id.note_txt_income)
            mNote.text = note
        }

        fun setDate(date: String) {
            val mDate = mView.findViewById<TextView>(R.id.date_txt_income)
            mDate.text = date
        }

        fun setAmount(amount: Float) {
            val mAmount = mView.findViewById<TextView>(R.id.amount_txt_income)
            val smAmount = amount.toString()
            mAmount.text = smAmount
        }
    }

    private fun updateDataItem() {
        val inflater = LayoutInflater.from(activity)
        val myview = inflater.inflate(R.layout.update_data_item, null)
        mDialogUtils?.customDialog(myview,true)
        edtAmount = myview.findViewById(R.id.amount)
        edtType = myview.findViewById(R.id.type_edt)
        edtNote = myview.findViewById(R.id.note_edt)

        //Set data to edit text
        edtType?.setText(type)
        edtType?.setSelection(type!!.length)
        edtNote?.setText(note)
        edtNote?.setSelection(note!!.length)
        edtAmount?.setText(amount.toString())
        edtAmount?.setSelection(amount.toString().length)
        btnUpdate = myview.findViewById(R.id.btnUpdUpdate)
        btnDelete = myview.findViewById(R.id.btnUpdDelete)

        btnUpdate?.setOnClickListener(View.OnClickListener {
            type = edtType?.getText().toString().trim { it <= ' ' }
            note = edtNote?.getText().toString().trim { it <= ' ' }
            var mdamount = amount.toString()
            mdamount = edtAmount?.getText().toString().trim { it <= ' ' }
            val myAmount = mdamount.toInt()
            val mDate = DateFormat.getDateInstance().format(Date())
            val data = Data(myAmount, type, note, post_key, mDate)
            mIncomeDatabase!!.child(post_key!!).setValue(data)
            mDialogUtils?.dismissDialog()
        })
        btnDelete?.setOnClickListener(View.OnClickListener {
            mIncomeDatabase!!.child(post_key!!).removeValue()
            mDialogUtils?.dismissDialog()
        })
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment IncomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String?, param2: String?): IncomeFragment {
            val fragment = IncomeFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}