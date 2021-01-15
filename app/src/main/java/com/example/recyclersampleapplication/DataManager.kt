package com.example.recyclersampleapplication

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.ArrayList
import kotlin.math.absoluteValue

class DataManager(actionListener: RecycleViewDataChanged, vararg initialData: Int){
    private val mDigitsList = ArrayList<Int>()
    private val mRecycledDigitsList = ArrayList<Int>()
    private val mDataLocker = ReentrantLock(true)
    private var mPoolInterchangeIsOn = true
    private var mValuesIncreasingIsOn = true
    private val mActionListener = actionListener
//    private val mRecyclerView = view
//    private lateinit var mRecyclerViewAdapter: DigitsAdapter

    init {
        mDigitsList.addAll(initialData.toList())

//        mRecyclerViewAdapter = DigitsAdapter(mDigitsList, this)
//        mRecyclerView.adapter = mRecyclerViewAdapter
    }

    fun startManipulations() {
        pasteNewDigit()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe (
                        { digitToPaste ->
                            try {
                                mDataLocker.lock()

                                val randomIndex = Random().nextInt(mDigitsList.size)

                                Log.d(RECYCLERVIEW_FILLER_OBSERVING_TAG, "onNext branch:" +
                                        "Random number: $digitToPaste," +
                                        "Random index: $randomIndex")

                                mDigitsList.add(randomIndex, digitToPaste)
//                                mRecyclerViewAdapter.notifyItemInserted(randomIndex)
                                mActionListener.itemAdded(randomIndex)
                            } finally {
                                mDataLocker.unlock()
                            }
                        },
                        {
                            Log.e(RECYCLERVIEW_FILLER_OBSERVING_TAG, "onError branch: $it")
                        }
                )

        increaseAllDigits()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { _->
                            try {
                                mDataLocker.lock()

                                Log.d(RECYCLERVIEW_INCREASER_OBSERVING_TAG, "onNext branch")

                                for (i in 0 until mDigitsList.size)
                                    mDigitsList[i]++

//                                mRecyclerViewAdapter.notifyDataSetChanged()
//                                mRecyclerView.startAnimation(AnimationUtils.loadAnimation(mRecyclerView.context, R.anim.shake))
                                mActionListener.eachDigitIncreased()
                            } finally {
                                mDataLocker.unlock()
                            }
                        },
                        {
                            Log.e(RECYCLERVIEW_INCREASER_OBSERVING_TAG, "onError branch: $it")
                        }
                )
    }

    fun removeElementAt(position: Int) {
        try {
            mDataLocker.lock()

            Log.d(TRASH_BEAN_CLICKED_TAG, "ItemPos: $position")
            Log.d(TRASH_BEAN_CLICKED_TAG, "Array before removing: " + mDigitsList)

            if (mPoolInterchangeIsOn)
                mRecycledDigitsList.add(mDigitsList.removeAt(position))
            else
                mDigitsList.removeAt(position)

//            mRecyclerViewAdapter.notifyItemRemoved(position)
            mActionListener.itemRemoved(position)

            Log.d(TRASH_BEAN_CLICKED_TAG, "Array after removing: " + mDigitsList)
            Log.d(TRASH_BEAN_CLICKED_TAG, "RecycledPool array now: " + mRecycledDigitsList)
        } finally {
            mDataLocker.unlock()
        }
    }

    fun setPoolInterchange(bool: Boolean) {
        mPoolInterchangeIsOn = bool
    }

    fun isPoolInterchangeOn(): Boolean {
        return mPoolInterchangeIsOn
    }

    fun setValuesIncreasing(bool: Boolean) {
        mValuesIncreasingIsOn = bool
    }

    fun isValuesIncreaseSet() : Boolean {
        return mValuesIncreasingIsOn
    }

    fun getData(): ArrayList<Int> {
        return mDigitsList
    }

    private fun pasteNewDigit(): Observable<Int> {
        return Observable.create {
            while (true) {
                Thread.sleep(5000)

                if (mPoolInterchangeIsOn && mRecycledDigitsList.isNotEmpty())
                    it.onNext(mRecycledDigitsList.removeAt(0))
                else if (!mPoolInterchangeIsOn) {
                    val randomNumber = (Math.random() * 100).toInt()        // TODO
                    it.onNext(randomNumber)
                }
            }
        }
    }

    private fun increaseAllDigits(): Observable<Int> {
        return Observable.create {
            while (true) {
                // Что значит "постоянно"?
                val randomArrayIncrementationTime = Random().nextLong().absoluteValue % 6000
                Thread.sleep(randomArrayIncrementationTime)

                // Лучше вообще тред не создавать тогда...
                if (mValuesIncreasingIsOn)
                    it.onNext(1)     // TODO onComplete or something
            }
        }
    }
}