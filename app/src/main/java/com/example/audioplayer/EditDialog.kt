package com.example.audioplayer

import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment


class EditDialog:DialogFragment() {


    override fun onStart() {
        super.onStart()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_edit_name,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }

}