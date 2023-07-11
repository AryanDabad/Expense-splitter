package com.example.expensemanager.Model

import java.io.Serializable

class GroupModel(var groupName: String? = ""):Serializable {
    var groupId: String? = ""

    var budget:Int=0
}