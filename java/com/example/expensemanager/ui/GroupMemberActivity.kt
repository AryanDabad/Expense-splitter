package com.example.expensemanager.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensemanager.Model.GroupMemberModel
import com.example.expensemanager.Model.GroupModel
import com.example.expensemanager.R
import com.example.expensemanager.databinding.FragmentGroupsFragmentsBinding
import com.example.expensemanager.databinding.GroupitemBinding
import com.google.firebase.database.*

class GroupMemberActivity : BaseActivity() {
    private lateinit var binding: FragmentGroupsFragmentsBinding
    var groupData: GroupModel? = null
    var groupList: ArrayList<GroupMemberModel>? = null

    private var groupMemberRef: DatabaseReference? = null
    private var mAdaptor: GroupAdaptor? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentGroupsFragmentsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        groupMemberRef = FirebaseDatabase.getInstance().getReference("groupmembers")
        groupData = intent.extras?.getSerializable("mData") as GroupModel
        with(binding) {
            mText.text = groupData?.groupName
            btnAdd.visibility = View.GONE
        }
        initRecycler()
        loadData()
    }

    private fun initRecycler() {
        mAdaptor = GroupAdaptor()
        binding.groupRecycler.apply {
            layoutManager = LinearLayoutManager(this@GroupMemberActivity)
            adapter = mAdaptor
        }
    }

    private fun loadData() {
        groupMemberRef
            ?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    groupList = ArrayList()
                    snapshot.children.forEach {
                        groupList?.add(it.getValue(GroupMemberModel::class.java) as GroupMemberModel)
                    }
                    val filterData = groupList?.filter { it.groupId.equals(groupData?.groupId) }
                    mAdaptor?.setData(filterData as ArrayList<GroupMemberModel>)
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    inner class GroupAdaptor : RecyclerView.Adapter<GroupAdaptor.MyViewHolder>() {
        private var mList: ArrayList<GroupMemberModel> = ArrayList()

        inner class MyViewHolder(val groupBinding: GroupitemBinding) :
            RecyclerView.ViewHolder(groupBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return MyViewHolder(GroupitemBinding.inflate(LayoutInflater.from(parent.context)))
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val singleItem = mList[position]
            with(holder.groupBinding) {
                btnMore.visibility = View.GONE
                txtFirst.text = (singleItem.memberId?.first() ?: "A").toString()
                txtGroupName.text = singleItem.memberId
                txtAmmount.text = singleItem.assigned.toString()
            }
        }


        override fun getItemCount(): Int {
            return mList.size
        }

        fun setData(newList: ArrayList<GroupMemberModel>) {
            mList = newList
            notifyDataSetChanged()
        }


    }

}