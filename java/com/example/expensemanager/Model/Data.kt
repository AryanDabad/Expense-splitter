package com.example.expensemanager.Model

data class Data(
    var amount: Int = 0,
    var type: String? = null,
    var note: String? = null,
    var id: String? = null,
    var date: String? = null
)