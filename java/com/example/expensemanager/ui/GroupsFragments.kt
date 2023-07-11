package com.example.expensemanager.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensemanager.Model.GroupMemberModel
import com.example.expensemanager.Model.GroupModel
import com.example.expensemanager.R
import com.example.expensemanager.databinding.AddpersonlayoutBinding
import com.example.expensemanager.databinding.CreategrouplayoutBinding
import com.example.expensemanager.databinding.FragmentGroupsFragmentsBinding
import com.example.expensemanager.databinding.GroupitemBinding
import com.example.expensemanager.utils.DialogUtils
import com.google.firebase.database.*

class GroupsFragments : Fragment() {

    var groupList: ArrayList<GroupModel>? = null
    private lateinit var binding: FragmentGroupsFragmentsBinding
    private var groupRef: DatabaseReference? = null
    private var groupMemberRef: DatabaseReference? = null
    private var mAdaptor: GroupAdaptor? = null
    private var mDialog: DialogUtils? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGroupsFragmentsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        groupRef = FirebaseDatabase.getInstance().getReference("groups")
        groupMemberRef = FirebaseDatabase.getInstance().getReference("groupmembers")
        mDialog = DialogUtils(requireContext())
        initRecycler()
        loadGroups()
        binding.btnAdd.setOnClickListener {
            showCreateGroupDialog()
        }
    }

    private fun showCreateGroupDialog() {
        val groupDialog = CreategrouplayoutBinding.inflate(layoutInflater)
        mDialog?.customDialog(groupDialog.root, false)
        with(groupDialog) {
            btnCreate.setOnClickListener {
                val text = txtName.editText?.text.toString()
                val ammount = txtAmmount.editText?.text.toString()
                if (text.isEmpty()) {
                    txtName.error = "Required..."
                } else if (ammount.isEmpty()) {
                    txtAmmount.error = "Required..."
                } else {
                    mDialog?.dismissDialog()
                    val key = groupRef?.push()?.key
                    val data = GroupModel(text)
                    data.groupId = key
                    data.budget = ammount.toInt()
                    groupRef?.child(key!!)?.setValue(data)
                }
            }
            btnCancel.setOnClickListener {
                mDialog?.dismissDialog()
            }
        }
    }

    private fun initRecycler() {
        mAdaptor = GroupAdaptor()
        binding.groupRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdaptor
        }
    }

    private fun loadGroups() {
        groupRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                groupList = ArrayList()
                snapshot.children.forEach {
                    groupList?.add(it.getValue(GroupModel::class.java) as GroupModel)
                }
                mAdaptor?.setData(groupList!!)
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    inner class GroupAdaptor : RecyclerView.Adapter<GroupAdaptor.MyViewHolder>() {
        private var mList: ArrayList<GroupModel> = ArrayList()

        inner class MyViewHolder(val groupBinding: GroupitemBinding) :
            RecyclerView.ViewHolder(groupBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return MyViewHolder(GroupitemBinding.inflate(LayoutInflater.from(parent.context)))
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val singleItem = mList[position]
            with(holder.groupBinding) {
                btnMore.setOnClickListener {
                    showPopUp(it, singleItem)
                }
                mCard.setOnClickListener {
                    startActivity(
                        Intent(
                            requireContext(),
                            GroupMemberActivity::class.java
                        ).putExtra("mData", singleItem)
                    )
                }
                txtFirst.text = (singleItem.groupName?.first() ?: "A").toString()
                txtGroupName.text = singleItem.groupName
                txtAmmount.text = singleItem.budget.toString()
            }
        }

        private fun showPopUp(it: View?, singleItem: GroupModel) {
            val popupMenu = PopupMenu(it?.context!!, it)
            popupMenu.menuInflater.inflate(R.menu.moremenu, popupMenu.menu)
            popupMenu.show()
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.deleteGroup -> {
                        showDeleteDialog(singleItem)
                    }
                    R.id.addPerson -> {
                        showAddPersonDialog(singleItem)
                    }
                }
                true
            }

        }


        override fun getItemCount(): Int {
            return mList.size
        }

        fun setData(newList: ArrayList<GroupModel>) {
            mList = newList
            notifyDataSetChanged()
        }


    }

    private fun showAddPersonDialog(singleItem: GroupModel) {
        val addpersonlayoutBinding = AddpersonlayoutBinding.inflate(layoutInflater)
        mDialog?.customDialog(addpersonlayoutBinding.root, false)
        with(addpersonlayoutBinding) {
            btnCancel.setOnClickListener {
                mDialog?.dismissDialog()
            }
            btnCreate.setOnClickListener {
                mDialog?.dismissDialog()
                val text = txtName.editText?.text.toString()
                val ammount = txtAmmount.editText?.text.toString()
                if (text.isEmpty()) {
                    txtName.error = "Required..."
                } else if (ammount.isEmpty()) {
                    txtAmmount.error = "Required..."
                } else if (singleItem.budget < ammount.toInt()) {
                    txtAmmount.error = "Out of Budget..."
                } else {
                    val key = groupMemberRef?.push()?.key
                    val data = GroupMemberModel(singleItem.groupName)
                    data.memberGroupId = key
                    data.groupId = singleItem.groupId
                    data.memberId = text
                    data.assigned = ammount.toInt()
                    singleItem.budget = singleItem.budget - ammount.toInt()
                    groupMemberRef?.child(key!!)?.setValue(data)
                    groupRef?.child(singleItem.groupId!!)?.setValue(singleItem)
                    Toast.makeText(requireContext(), "Member add successful", Toast.LENGTH_SHORT)
                        .show()
                    mDialog?.dismissDialog()
                }
            }
        }
    }

    private fun showDeleteDialog(singleItem: GroupModel) {
        AlertDialog.Builder(requireContext()).also {
            it.setTitle("Delete")
            it.setMessage("Are your sure to delete ${singleItem.groupName}")
            it.setCancelable(false)
            it.setPositiveButton("Yes") { dialog, i ->
                dialog.dismiss()
                groupRef?.child(singleItem.groupId!!)?.removeValue()
            }
            it.setNegativeButton("No") { dialog, i ->
                dialog.dismiss()
            }
        }.create().show()
    }
}