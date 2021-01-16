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
 * @param actionListener Data change listener to update RecyclerView layout from Activity.
 *  Triggers each time when data changes
 * @param initialData Integer set to fill RecyclerView on create
 */
class DataManager(actionListener: RecycleViewDataChanged, vararg initialData: Int){
    private val dataSet = ArrayList<Int>()
    private val recycledDataSet = ArrayList<Int>()
    private val dataLocker = ReentrantLock(true)
    private val dataChangeActionListener = actionListener

    private var poolInterchangeIsOn = false
    private var valuesIncreasingIsOn = false
    private var increaseValuesPeriod = 5000L
    private var insertNewValuePeriod = 6000L

    init {
        dataSet.addAll(initialData.toList())
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
                .subscribe ( { index ->
                            Log.d(RECYCLERVIEW_FILLER_OBSERVING_TAG, "onNext branch:" +
                                    "Random number: ${dataSet[index]}," +
                                    "Random index: $index")

                            dataChangeActionListener.itemAdded(index)
                        },
                        {
                            Log.e(RECYCLERVIEW_FILLER_OBSERVING_TAG, "onError branch: $it")
                        }
                )

        increaseAllDigitsTask()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( {
                            Log.d(RECYCLERVIEW_INCREASER_OBSERVING_TAG, "onNext branch")
                            dataChangeActionListener.eachDigitIncreased()
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
            dataLocker.lock()

            Log.d(TRASH_BEAN_CLICKED_TAG, "ItemPos: $position")
            Log.d(TRASH_BEAN_CLICKED_TAG, "Array before removing: $dataSet")

            if (poolInterchangeIsOn)
                recycledDataSet.add(dataSet.removeAt(position))
            else
                dataSet.removeAt(position)

            dataChangeActionListener.itemRemoved(position)

            Log.d(TRASH_BEAN_CLICKED_TAG, "Array after removing: $dataSet")
            Log.d(TRASH_BEAN_CLICKED_TAG, "RecycledPool array now: $recycledDataSet")
        } finally {
            dataLocker.unlock()
        }
    }

    /**
     * Sets new elements insertion mode
     * @param bool true - grab new elements from already removed elements pool
     *  (won't insert if pool is empty); false - generates new digit to insert
     */
    fun setPoolInterchange(bool: Boolean): DataManager {
        poolInterchangeIsOn = bool
        return this
    }

    /**
     * Sets array elements incrementation mode
     * @param bool true - increase all elements in array according to specified time period;
     *  false - don't increase elements
     */
    fun setValuesIncreasing(bool: Boolean): DataManager {
        valuesIncreasingIsOn = bool
        return this
    }

    /**
     * Set insertion time period in milliseconds
     */
    fun setNewValueInsertionPeriod(millis: Long): DataManager {
        insertNewValuePeriod = millis
        return this
    }

    /**
     * Set incrementation time period in milliseconds
     */
    fun setValuesIncreasePeriod(millis: Long): DataManager {
        increaseValuesPeriod = millis
        return this
    }

    fun getData(): ArrayList<Int> {
        return dataSet
    }

    /**
     * Insertion async task.
     * Inserts new value at a random place in data set according to specified time period
     *
     * Interprets specified insertionNewValuePeriod as follows:
     *      1) If insertionNewValuePeriod == 0, insertion is off
     *      2) If insertionNewValuePeriod > 0, inserts at each period
     *      3) If insertionNewValuePeriod < 0, inserts at a random time between 0 and
     *          insertionNewValuePeriod
     *
     * If pool interchange mode is off, still inserts random data elements from 0 to 100
     *
     * @return Index of inserted value to update RecycleView adapter
     */
    private fun insertNewDigitTask(): Observable<Int> {
        return Observable.create {
            while (true) {
                if (insertNewValuePeriod > 0L) {
                    Thread.sleep(insertNewValuePeriod)
                }
                else if (insertNewValuePeriod < 0L) {
                    val randomLocalPeriod =
                            Random().nextLong().absoluteValue % insertNewValuePeriod.absoluteValue
                    Thread.sleep(randomLocalPeriod)
                }
                else continue

                val randomIndex = Random().nextInt(dataSet.size)

                val numberToInsert = if (poolInterchangeIsOn && recycledDataSet.isNotEmpty()) {
                    recycledDataSet.removeAt(0)
                } else if (!poolInterchangeIsOn) {
                    Random().nextInt().absoluteValue % 100
                } else continue

                Log.d(RECYCLERVIEW_FILLER_METHOD_TAG, "Random index: $randomIndex\tValue to insert: $numberToInsert")
                Log.d(RECYCLERVIEW_FILLER_METHOD_TAG, "Data set before insertion: $dataSet")

                try {
                    dataLocker.lock()
                    dataSet.add(randomIndex, numberToInsert)
                } finally {
                    dataLocker.unlock()
                }

                Log.d(RECYCLERVIEW_FILLER_METHOD_TAG, "Data set after insertion: $dataSet")

                it.onNext(randomIndex)
            }
        }
    }

    /**
     * Incrementation async task. Increases each element in data set according to specified time
     * period.
     *
     * Interprets specified insertionNewValuePeriod as follows:
     *      1) If insertionNewValuePeriod == 0, insertion is off
     *      2) If insertionNewValuePeriod > 0, inserts at each period
     *      3) If insertionNewValuePeriod < 0, inserts at a random time between 0 and
     *          insertionNewValuePeriod
     */
    private fun increaseAllDigitsTask(): Observable<Int> {
        return Observable.create {
            while (true) {
                if (increaseValuesPeriod > 0L) {
                    Thread.sleep(increaseValuesPeriod)
                }
                else if (increaseValuesPeriod < 0L) {
                    val randomLocalPeriod = Random().nextLong().absoluteValue % increaseValuesPeriod.absoluteValue
                    Thread.sleep(randomLocalPeriod)
                }
                else continue

                if (!valuesIncreasingIsOn) continue

                Log.d(RECYCLERVIEW_INCREASER_METHOD_TAG, "Data set before data increase: $dataSet")

                try {
                    dataLocker.lock()

                    for (i in 0 until dataSet.size)
                        dataSet[i]++

                } finally {
                    dataLocker.unlock()
                }

                Log.d(RECYCLERVIEW_INCREASER_METHOD_TAG, "Data set after data increase: $dataSet")

                it.onNext(1)
            }
        }
    }
}