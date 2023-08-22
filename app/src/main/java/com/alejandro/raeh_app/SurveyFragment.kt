package com.alejandro.raeh_app

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.Navigation

/**
 * A fragment representing a list of Items.
 */
class SurveyFragment : Fragment() {

    private lateinit var surveyQuestionTextView: TextView
    private lateinit var surveyAnswersList: RecyclerView
    private lateinit var surveyBackButton: ImageButton
    private lateinit var surveyCloseButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_item_survey_list, container, false)

        // Configure surveyQuestionTextView
        surveyQuestionTextView = view.findViewById(R.id.surveyQuestionTextView)

        // Configure RecyclerView Adapter
        surveyAnswersList = view.findViewById(R.id.surveyAnswersList)
        surveyAnswersList.adapter = SurveyRecyclerViewAdapter(listOf("Lorem ipsum", "Dolor sit amet", "Consectetur adipiscing elit"))

        // Configure surveyBackButton
        surveyBackButton = view.findViewById(R.id.surveyBackButton)
        surveyBackButton.setOnClickListener {
            Toast.makeText(context, "Back button clicked", Toast.LENGTH_SHORT).show()
        }

        // Configure surveyCloseButton
        surveyCloseButton = view.findViewById(R.id.surveyCloseButton)
        surveyCloseButton.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_itemSurveyFragment_to_welcomeFragment)
        }

        return view
    }

}