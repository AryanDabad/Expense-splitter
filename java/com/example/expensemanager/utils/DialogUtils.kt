package com.example.expensemanager.utils

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.view.ViewGroup


class DialogUtils(context: Context) : Dialog(context) {
    var dismiss: (() -> Any?)? = null


    fun customDialog(view: View, isCancelable: Boolean) {
        setContentView(view)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        setCancelable(isCancelable)
        show()
    }



    override fun setOnDismissListener(listener: DialogInterface.OnDismissListener?) {
        super.setOnDismissListener(listener)
        dismiss?.invoke()
    }

    fun dismissDialog() {
        if (isShowing)
            dismiss()
    }
}