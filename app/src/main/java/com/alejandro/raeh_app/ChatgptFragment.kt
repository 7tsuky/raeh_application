package com.alejandro.raeh_app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class ChatgptFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var context = ""

        /// Create thread and call aip
        var view = inflater.inflate(R.layout.fragment_chatgpt, container, false)

        return view
    }

    // Retrofit

}