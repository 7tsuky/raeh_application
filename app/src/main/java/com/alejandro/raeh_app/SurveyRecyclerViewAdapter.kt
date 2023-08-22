package com.alejandro.raeh_app

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast

import com.alejandro.raeh_app.databinding.FragmentItemSurveyBinding

class SurveyRecyclerViewAdapter(
    private val answers: List<String>
) : RecyclerView.Adapter<SurveyRecyclerViewAdapter.AnswerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnswerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_item_survey, parent, false)
        return AnswerViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnswerViewHolder, position: Int) {
        holder.bind(answers[position])
    }

    override fun getItemCount(): Int = answers.size

    inner class AnswerViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        private val answerTextView: TextView = view.findViewById(R.id.answerTextView)

        fun bind(answer: String) {
            answerTextView.text = answer;
            answerTextView.setOnClickListener {
                Toast.makeText(itemView.context, "Answer clicked", Toast.LENGTH_SHORT).show()
            }
        }
    }

}