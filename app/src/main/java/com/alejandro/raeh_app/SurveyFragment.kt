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
import androidx.compose.runtime.collection.mutableVectorOf
import androidx.core.os.trace
import androidx.navigation.Navigation
import com.google.gson.Gson
import java.io.IOException
import java.nio.charset.Charset
import java.util.Vector

/**
 * A fragment representing a list of Items.
 */
class SurveyFragment : Fragment() {

    // Survey objects
    data class Survey(
        val nodes: List<Node>
    )
    data class Node(
        val id: Int,
        val type: String,
        val value: String,
        val children: List<Int>
    )

    // XML objects
    private lateinit var surveyQuestionTextView: TextView
    private lateinit var surveyAnswersList: RecyclerView
    private lateinit var surveyBackButton: ImageButton
    private lateinit var surveyCloseButton: ImageButton
    private lateinit var viewOfLayout: View

    // Members
    private lateinit var surveyGraph: Survey
    private var traceQuestionNodeIds: Vector<Int> = Vector<Int>()
    private var finishedSurvey = false
    private var traceNodeIds: Vector<Int> = Vector<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val jsonMsg = loadJSONFile("survey.json")
        if(jsonMsg != null){
            surveyGraph = Gson().fromJson(jsonMsg, Survey::class.java)
        } else {
            return
        }
        traceQuestionNodeIds.addAll(arrayOf(-1, 0))
        traceNodeIds.addAll(arrayOf(0))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewOfLayout = inflater.inflate(R.layout.fragment_item_survey_list, container, false)

        // Configure surveyQuestionTextView
        surveyQuestionTextView = viewOfLayout.findViewById(R.id.surveyQuestionTextView)

        // Configure RecyclerView Adapter
        surveyAnswersList = viewOfLayout.findViewById(R.id.surveyAnswersList)
        updateSurvey()

        // Configure surveyBackButton
        surveyBackButton = viewOfLayout.findViewById(R.id.surveyBackButton)
        surveyBackButton.setOnClickListener {
            returnToLastQuestion();
        }

        // Configure surveyCloseButton
        surveyCloseButton = viewOfLayout.findViewById(R.id.surveyCloseButton)
        surveyCloseButton.setOnClickListener {
            Navigation.findNavController(viewOfLayout).navigate(R.id.action_itemSurveyFragment_to_welcomeFragment)
        }

        return viewOfLayout
    }

    private fun loadJSONFile(path: String): String? {
        try {
            val inputStream = context?.assets?.open(path)
            val size = inputStream?.available()
            val buffer = ByteArray(size ?: 0)
            inputStream?.read(buffer)
            inputStream?.close()
            return String(buffer, Charset.defaultCharset())
        } catch (ex: IOException) {
            ex.printStackTrace()
            Toast.makeText(context, "Failed loading the JSON file", Toast.LENGTH_SHORT).show()
            return null
        }
    }

    private fun returnToLastQuestion(){
        if (traceQuestionNodeIds.elementAt(traceQuestionNodeIds.size - 2) == -1) {
            Navigation.findNavController(viewOfLayout).navigate(R.id.action_itemSurveyFragment_to_welcomeFragment)
            return
        }
        finishedSurvey = false
        traceQuestionNodeIds.removeLast()
        updateSurvey()
    }

    private fun updateSurvey(){
        // Fade out
        if (finishedSurvey){
            val action= SurveyFragmentDirections.actionItemSurveyFragmentToChatgptFragment(traceNodeIds.toIntArray())
            Navigation.findNavController(viewOfLayout).navigate(action)
            return
        }
        val currentNode = getCurrentNode()
        surveyQuestionTextView.text = currentNode.value

        val questionSets: Array<Node> = getNextSetOfAnswers()
        surveyAnswersList.animate().alpha(0f).setDuration(200).withEndAction {
            surveyAnswersList.adapter = SurveyRecyclerViewAdapter(questionSets)

            // Fade in
            surveyAnswersList.alpha = 0f
            surveyAnswersList.animate().alpha(1f).duration = 200
        }.start()
    }

    private fun getNextSetOfAnswers(): Array<Node> {
        // Step 1: Retrieve the current node
        val currentNode = getCurrentNode()

        // Step 2: Retrieve the child node IDs
        val childNodeIds = currentNode.children

        // Step 3 & 4: For each child node ID, retrieve the corresponding node and extract its value
        val answers = childNodeIds.map { childId ->
            val childNode = surveyGraph.nodes.find { it.id == childId }
                ?: throw IllegalStateException("Child node not found in survey graph.")
            childNode
        }

        return answers.toTypedArray()
    }

    private fun getCurrentNode(): Node {
        return surveyGraph.nodes.find { it.id == traceQuestionNodeIds.lastElement() }
            ?: throw IllegalStateException("Current node not found in survey graph.")
    }

    private inner class SurveyRecyclerViewAdapter(
        private val answers: Array<Node>
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

            fun bind(answer: Node) {
                answerTextView.text = answer.value;
                answerTextView.setOnClickListener {
                    finishedSurvey = answer.children.isEmpty()
                    if (!finishedSurvey){
                        traceQuestionNodeIds.add(answer.children[0])
                        traceNodeIds.add(answer.id)
                        traceNodeIds.add(answer.children[0])
                    }
                    updateSurvey()
                }
            }
        }
    }
}