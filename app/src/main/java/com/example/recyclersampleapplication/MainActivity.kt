package com.example.recyclersampleapplication

import android.content.res.Configuration
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity(), RecycleViewItemClickListener, RecycleViewDataChanged {
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewAdapter: DigitsAdapter

    private lateinit var layoutManager: GridLayoutManager
    private lateinit var dataManager: DataManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val initialColumns = getOrientationColumns(resources.configuration.orientation)

        layoutManager = GridLayoutManager(this, initialColumns)

        recyclerView = findViewById(R.id.recyclerViewMain)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = layoutManager

        dataManager = DataManager(this, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)

        recyclerViewAdapter = DigitsAdapter(dataManager.getData(), this)
        recyclerView.adapter = recyclerViewAdapter
    }

    override fun onStart() {
        super.onStart()

        dataManager
                .setPoolInterchange(true)
                .setValuesIncreasing(true)
                .setNewValueInsertionPeriod(5000)
                .setValuesIncreasePeriod(3500)
                .startManipulations()
    }

    override fun onViewClicked(clickedViewId: Int, clickedItemPosition: Int) {
        if (clickedViewId == R.id.deleteItemButton)
            dataManager.removeElementAt(clickedItemPosition)
    }

    private fun getOrientationColumns(orientation: Int): Int {
        return when (orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> 4
            else -> 2
        }
    }

    /**
     * Triggers each time when orientation changes
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        layoutManager.spanCount = getOrientationColumns(newConfig.orientation)
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