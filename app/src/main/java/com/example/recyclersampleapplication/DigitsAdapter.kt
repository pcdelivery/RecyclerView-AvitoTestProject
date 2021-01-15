package com.example.recyclersampleapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Custom RecyclerView adapter to manage TextView + ImageButton bundle, based on integer array data
 *
 * @param digitsContent Data array of Int - identifier for each RecyclerView element
 * @param actionListener On element click listener to update RecyclerView layout from Activity
 */
class DigitsAdapter(digitsContent: ArrayList<Int>, actionListener: RecycleViewItemClickListener): RecyclerView.Adapter<DigitsAdapter.ViewHolder>() {
    private val digits: ArrayList<Int> = digitsContent
    private val mListener: RecycleViewItemClickListener = actionListener

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.itemNumberText)
        val imageButton: ImageButton = itemView.findViewById(R.id.deleteItemButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item, parent, false)

        val holder = ViewHolder(v)

        holder.imageButton.setOnClickListener {
            mListener.onViewClicked(it.id, holder.adapterPosition)
        }

        return holder
    }

    override fun getItemCount(): Int {
        return digits.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = digits[position].toString()
    }
}