package com.alejandro.raeh_app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs

class ChatgptFragment : Fragment() {

    val args: ChatgptFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var context = args.surveyContext

        /// Create thread and call aip
        var view = inflater.inflate(R.layout.fragment_chatgpt, container, false)

        return view
    }

    // Retrofit

}