package com.example.recyclersampleapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlin.coroutines.coroutineContext

class DigitsAdapter(digitsContent: ArrayList<Int>, actionListener: RecycleViewItemClickListener): RecyclerView.Adapter<DigitsAdapter.ViewHolder>() {
    private var digits: ArrayList<Int> = digitsContent
    private var mListener: RecycleViewItemClickListener = actionListener

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        public var textView: TextView
        public var imageButton: ImageButton

        init {
                textView = itemView.findViewById(R.id.itemNumberText)
                imageButton = itemView.findViewById(R.id.deleteItemButton)
        }
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
//        holder.imageButton.setOnClickListener {
//            digits.remove(position)
//            notifyItemRemoved(position)
//            notifyDataSetChanged()
//        }
    }
}