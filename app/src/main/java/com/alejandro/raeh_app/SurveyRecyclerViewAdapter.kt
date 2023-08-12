package com.alejandro.raeh_app

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.alejandro.raeh_app.placeholder.PlaceholderContent.PlaceholderItem
import com.alejandro.raeh_app.databinding.FragmentItemSurveyBinding

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */
class SurveyRecyclerViewAdapter(
    private val values: List<PlaceholderItem>
) : RecyclerView.Adapter<SurveyRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentItemSurveyBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentItemSurveyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        override fun toString(): String {
            return "h"
        }
    }

}