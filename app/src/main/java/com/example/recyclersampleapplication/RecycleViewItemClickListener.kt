package com.example.recyclersampleapplication

interface RecycleViewItemClickListener {
    fun onViewClicked(clickedViewId: Int, clickedItemPosition: Int)
    fun onViewLongClicked(clickedViewId: Int, clickedItemPosition: Int)
}