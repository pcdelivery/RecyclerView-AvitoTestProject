package com.example.recyclersampleapplication

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity(), RecycleViewItemClickListener, RecycleViewDataChanged {
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewAdapter: DigitsAdapter

    private lateinit var mLayoutManager: GridLayoutManager
    private lateinit var mDataManager: DataManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mLayoutManager = GridLayoutManager(this, 2)

        recyclerView = findViewById(R.id.recyclerViewMain)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = mLayoutManager

        mDataManager = DataManager(this, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)

        recyclerViewAdapter = DigitsAdapter(mDataManager.getData(), this)
        recyclerView.adapter = recyclerViewAdapter

        Log.d("[Main Thread]", Thread.currentThread().id.toString())
    }

    override fun onStart() {
        super.onStart()

        mDataManager.setPoolInterchange(true)
        mDataManager.setValuesIncreasing(true)
        mDataManager.startManipulations()
    }

    override fun onViewClicked(clickedViewId: Int, clickedItemPosition: Int) {
        if (clickedViewId == R.id.deleteItemButton)
            mDataManager.removeElementAt(clickedItemPosition)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
            mLayoutManager.spanCount = 2
        else
            mLayoutManager.spanCount = 4
    }

    override fun itemAdded(position: Int) {
        recyclerViewAdapter.notifyItemInserted(position)
    }

    override fun itemRemoved(position: Int) {
        recyclerViewAdapter.notifyItemRemoved(position)
    }

    override fun eachDigitIncreased() {
        recyclerViewAdapter.notifyDataSetChanged()
        recyclerView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake))
    }
}