package com.example.recyclersampleapplication

import android.util.Log
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
    private val mActionListener = actionListener

    private var mPoolInterchangeIsOn = false
    private var mValuesIncreasingIsOn = false
    private var mIncreaseValuesPeriod = 5000L
    private var mInsertNewValuePeriod = 6000L

    init {
        mDigitsList.addAll(initialData.toList())
    }

    fun startManipulations() {
        insertNewDigitTask()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe (
                        { index ->
                            Log.d(RECYCLERVIEW_FILLER_OBSERVING_TAG, "onNext branch:" +
                                    "Random number: ${mDigitsList[index]}," +
                                    "Random index: $index")

                            mActionListener.itemAdded(index)
                        },
                        {
                            Log.e(RECYCLERVIEW_FILLER_OBSERVING_TAG, "onError branch: $it")
                        }
                )

        increaseAllDigitsTask()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            Log.d(RECYCLERVIEW_INCREASER_OBSERVING_TAG, "onNext branch")
                            mActionListener.eachDigitIncreased()
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
            Log.d(TRASH_BEAN_CLICKED_TAG, "Array before removing: $mDigitsList")

            if (mPoolInterchangeIsOn)
                mRecycledDigitsList.add(mDigitsList.removeAt(position))
            else
                mDigitsList.removeAt(position)

            mActionListener.itemRemoved(position)

            Log.d(TRASH_BEAN_CLICKED_TAG, "Array after removing: $mDigitsList")
            Log.d(TRASH_BEAN_CLICKED_TAG, "RecycledPool array now: $mRecycledDigitsList")
        } finally {
            mDataLocker.unlock()
        }
    }

    fun setPoolInterchange(bool: Boolean): DataManager {
        mPoolInterchangeIsOn = bool
        return this
    }

    fun setValuesIncreasing(bool: Boolean): DataManager {
        mValuesIncreasingIsOn = bool
        return this
    }

    fun setNewValueInsertionPeriod(millis: Long): DataManager {
        mInsertNewValuePeriod = millis
        return this
    }

    fun setValuesIncreasePeriod(millis: Long): DataManager {
        mIncreaseValuesPeriod = millis
        return this
    }

    fun getData(): ArrayList<Int> {
        return mDigitsList
    }

    private fun insertNewDigitTask(): Observable<Int> {
        return Observable.create {
            while (true) {
                if (mInsertNewValuePeriod > 0L)
                    Thread.sleep(mInsertNewValuePeriod)
                else if (mInsertNewValuePeriod < 0L) {
                    val randomLocalPeriod = Random().nextLong().absoluteValue % mInsertNewValuePeriod.absoluteValue
                    Thread.sleep(randomLocalPeriod)
                }
                else
                    continue

                val randomIndex = Random().nextInt(mDigitsList.size)
                var numberToInsert: Int

                // TODO
                if (mPoolInterchangeIsOn && mRecycledDigitsList.isNotEmpty()) {
                    numberToInsert = mRecycledDigitsList.removeAt(0)
                }
                else if (!mPoolInterchangeIsOn) {
                    numberToInsert = Random().nextInt().absoluteValue % 100
                }
                else
                    continue

                try {
                    mDataLocker.lock()
                    mDigitsList.add(randomIndex, numberToInsert)
                } finally {
                    mDataLocker.unlock()
                }

                it.onNext(randomIndex)
            }
        }
    }

    private fun increaseAllDigitsTask(): Observable<Int> {
        return Observable.create {
            while (true) {
                // TODO Interpretator function
                if (mIncreaseValuesPeriod > 0L)
                    Thread.sleep(mIncreaseValuesPeriod)
                else if (mIncreaseValuesPeriod < 0L) {
                    val randomLocalPeriod = Random().nextLong().absoluteValue % mIncreaseValuesPeriod.absoluteValue
                    Thread.sleep(randomLocalPeriod)
                }
                else
                    continue

                try {
                    mDataLocker.lock()

                    for (i in 0 until mDigitsList.size)
                        mDigitsList[i]++

                } finally {
                    mDataLocker.unlock()
                }

                it.onNext(1)
            }
        }
    }
}