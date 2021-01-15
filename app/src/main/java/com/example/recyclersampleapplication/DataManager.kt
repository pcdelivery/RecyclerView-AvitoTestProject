package com.example.recyclersampleapplication

import android.util.Log
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.ArrayList
import kotlin.math.absoluteValue

/**
 * Object for RecyclerView data managing:
 *      1) New data elements appending
 *      2) All data elements incrementation
 *
 * Insertion and incrementation periods, working mode can be configured
 *
 * @param actionListener Data change listener to update RecyclerView layout from Activity. Triggers each time when data changes
 * @param initialData Integer set to fill RecyclerView on create
 */
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

    /**
     * The method starts new async tasks on separate threads:
     *      1) New element insertion
     *      2) All elements incrementation
     *
     * Intermediate results are observed in main thread
     */
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

    /**
     * Removes element in data array;
     * Triggers actionListener itemRemoved() method
     */
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

    /**
     * Sets new elements insertion mode
     * @param bool true - grab new elements from already removed elements pool (won't insert if pool is empty); false - generates new digit to insert
     */
    fun setPoolInterchange(bool: Boolean): DataManager {
        mPoolInterchangeIsOn = bool
        return this
    }

    /**
     * Sets array elements incrementation mode
     * @param bool true - increase all elements in array according to specified time period; false - don't increase elements
     */
    fun setValuesIncreasing(bool: Boolean): DataManager {
        mValuesIncreasingIsOn = bool
        return this
    }

    /**
     * Set insertion time period in milliseconds
     */
    fun setNewValueInsertionPeriod(millis: Long): DataManager {
        mInsertNewValuePeriod = millis
        return this
    }

    /**
     * Set incrementation time period in milliseconds
     */
    fun setValuesIncreasePeriod(millis: Long): DataManager {
        mIncreaseValuesPeriod = millis
        return this
    }

    fun getData(): ArrayList<Int> {
        return mDigitsList
    }

    /**
     * Insertion async task
     */
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

    /**
     * Incrementation async task
     */
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