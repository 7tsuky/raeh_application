package com.alejandro.raeh_app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.Navigation

class WelcomeFragment : Fragment() {

    private lateinit var welcomeStartButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_welcome, container, false)

        welcomeStartButton = view.findViewById<Button>(R.id.welcomeStartButton)
        welcomeStartButton.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_welcomeFragment_to_itemSurveyFragment)
        }
;
        return view
    }
}