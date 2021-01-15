package com.example.recyclersampleapplication

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity(), RecycleViewItemClickListener, RecycleViewDataChanged {
//    private lateinit var digitsList: ArrayList<Int>
//    private lateinit var recycledDigitsList: ArrayList<Int>
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewAdapter: DigitsAdapter

    private lateinit var mLayoutManager: GridLayoutManager
//    private lateinit var mDataLocker: ReentrantLock
    private lateinit var mDataManager: DataManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mLayoutManager = GridLayoutManager(this, 2)
//        mDataLocker = ReentrantLock(true)

        recyclerView = findViewById(R.id.recyclerViewMain)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = mLayoutManager

//        recycledDigitsList = ArrayList()
//        digitsList = ArrayList()
//        for (i in 1..15)
//            digitsList.add(i)

        mDataManager = DataManager(this, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)

        recyclerViewAdapter = DigitsAdapter(mDataManager.getData(), this)
        recyclerView.adapter = recyclerViewAdapter


        Log.d("[Main Thread]", Thread.currentThread().id.toString())

//        runThisForever()
//            .subscribeOn(Schedulers.newThread())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe(
//                    {
//                        Log.d(RECYCLERVIEW_FILLER_OBSERVING_TAG, "onNext branch: Random index: $it")
//
//                        val randomIndex = (Math.random() * digitsList.size).toInt()     // TODO
//
//                        try {
//                            mDataLocker.lock()
//                            digitsList.add(randomIndex, it)
////                                recyclerViewAdapter.notifyDataSetChanged()
//                            recyclerViewAdapter.notifyItemInserted(randomIndex)
//                        }
//                        finally {
//                            mDataLocker.unlock()
//                        }
//
//                        Log.d(RECYCLERVIEW_FILLER_OBSERVING_TAG, "onNext branch: New random place: $randomIndex")
//                    },
//                    {
//                        // TODO?
//                        Log.d(RECYCLERVIEW_FILLER_OBSERVING_TAG, "onError branch: $it")
//                    },
//                    {
//                        // TODO?
//                        Log.d(RECYCLERVIEW_FILLER_OBSERVING_TAG, "onComplete branch")
//                    }
//            )
//
//        runThatForever(mDataLocker)
//            .subscribeOn(Schedulers.newThread())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe(
//                    {
//                        Log.d(RECYCLERVIEW_INCREASER_OBSERVING_TAG, "onNext branch: it $it")
//
//                        recyclerViewAdapter.notifyDataSetChanged()
//                        recyclerView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake))
//                    },
//                    {
//                        // TODO?
//                        Log.w(RECYCLERVIEW_INCREASER_OBSERVING_TAG, "onError branch: $it")
//                    },
//                    {
//                        // TODO?
//                        Log.w(RECYCLERVIEW_INCREASER_OBSERVING_TAG, "onComplete branch")
//                    }
//            )
    }

    override fun onStart() {
        super.onStart()

        mDataManager.startManipulations()
    }

    override fun onViewClicked(clickedViewId: Int, clickedItemPosition: Int) {
        if (clickedViewId == R.id.deleteItemButton)
            mDataManager.removeElementAt(clickedItemPosition)
    }

    override fun onViewLongClicked(clickedViewId: Int, clickedItemPosition: Int) {
        TODO("Not yet implemented")
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
    }

//    private fun runThisForever(): Observable<Int> {
//        return Observable.create {
//            while (true) {
//                Thread.sleep(5000)
//
//                if (poolInterchangeIsOn && recycledDigitsList.isNotEmpty())
//                    it.onNext(recycledDigitsList.removeAt(0))
//                else if (!poolInterchangeIsOn) {
//                    val randomNumber = (Math.random() * 100).toInt()        // TODO
//                    it.onNext(randomNumber)
//                }
//            }
//        }
//    }
//
//    private fun runThatForever(locker: ReentrantLock): Observable<Int> {
//        return Observable.create {
//            while (true) {
//                // Что значит "постоянно"?
//                val randomArrayIncrementationTime = Random().nextLong().absoluteValue % 6000
//                Thread.sleep(randomArrayIncrementationTime)
//
//                // Лучше вообще тред не создавать тогда...
//                if (!valuesIncreasingIsOn)
//                    continue
//
//                try {
//                    locker.lock()
//                    for (i in 0 until digitsList.size)
//                        digitsList[i]++
//
//                } finally {
//                    locker.unlock()
//                }
//
//                it.onNext(1)                // TODO onComplete or something
//            }
//        }
//    }
}