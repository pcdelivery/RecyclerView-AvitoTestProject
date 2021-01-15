package com.example.recyclersampleapplication

import android.content.res.Configuration
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.OrientationEventListener
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.AnimationUtilsCompat
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.functions.Cancellable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.sync.Mutex
import org.reactivestreams.Subscriber
import java.util.*
import java.util.concurrent.locks.Lock
import kotlin.collections.ArrayList
import kotlin.math.nextDown
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.absoluteValue

class MainActivity : AppCompatActivity(), RecycleViewItemClickListener{
    private lateinit var digitsList: ArrayList<Int>
    private lateinit var recycledDigitsList: ArrayList<Int>
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewAdapter: DigitsAdapter

    private lateinit var mLayoutManager: GridLayoutManager
    private lateinit var mDataLocker: ReentrantLock

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mLayoutManager = GridLayoutManager(this, 2)
        mDataLocker = ReentrantLock(true)

        recyclerView = findViewById(R.id.recyclerViewMain)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = mLayoutManager

        recycledDigitsList = ArrayList()
        digitsList = ArrayList()
        for (i in 1..15)
            digitsList.add(i)

        recyclerViewAdapter = DigitsAdapter(digitsList, this)
        recyclerView.adapter = recyclerViewAdapter

        Log.d("[Main Thread]", Thread.currentThread().id.toString())

        runThisForever()
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                    {
                        Log.d(RECYCLERVIEW_FILLER_OBSERVING_TAG, "onNext branch: Random index: $it")

                        val randomIndex = (Math.random() * digitsList.size).toInt()     // TODO

                        try {
                            mDataLocker.lock()
                            digitsList.add(randomIndex, it)
//                                recyclerViewAdapter.notifyDataSetChanged()
                            recyclerViewAdapter.notifyItemInserted(randomIndex)
                        }
                        finally {
                            mDataLocker.unlock()
                        }

                        Log.d(RECYCLERVIEW_FILLER_OBSERVING_TAG, "onNext branch: New random place: $randomIndex")
                    },
                    {
                        // TODO?
                        Log.d(RECYCLERVIEW_FILLER_OBSERVING_TAG, "onError branch: $it")
                    },
                    {
                        // TODO?
                        Log.d(RECYCLERVIEW_FILLER_OBSERVING_TAG, "onComplete branch")
                    }
            )

        runThatForever(mDataLocker)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                    {
                        Log.d(RECYCLERVIEW_INCREASER_OBSERVING_TAG, "onNext branch: it $it")

                        recyclerViewAdapter.notifyDataSetChanged()
                        recyclerView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake))
                    },
                    {
                        // TODO?
                        Log.w(RECYCLERVIEW_INCREASER_OBSERVING_TAG, "onError branch: $it")
                    },
                    {
                        // TODO?
                        Log.w(RECYCLERVIEW_INCREASER_OBSERVING_TAG, "onComplete branch")
                    }
            )
    }

    override fun onViewClicked(clickedViewId: Int, clickedItemPosition: Int) {
        if (clickedViewId == R.id.deleteItemButton) {
            Log.d(TRASH_BEAN_CLICKED_TAG, "ViewID: $clickedViewId ItemPos: $clickedItemPosition")
            Log.d(TRASH_BEAN_CLICKED_TAG, "Array before removing: " + digitsList)

            try {
                mDataLocker.lock()
                recycledDigitsList.add(digitsList.removeAt(clickedItemPosition))
                recyclerViewAdapter.notifyItemRemoved(clickedItemPosition)
            } finally {
                mDataLocker.unlock()
            }

            Log.d(TRASH_BEAN_CLICKED_TAG, "Array after removing: " + digitsList)
            Log.d(TRASH_BEAN_CLICKED_TAG, "RecycledPool array now: " + recycledDigitsList)
        }
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

    private fun runThisForever(): Observable<Int> {
        return Observable.create {
            while (true) {
                Thread.sleep(5000)

                if (poolInterchangeIsOn && recycledDigitsList.isNotEmpty())
                    it.onNext(recycledDigitsList.removeAt(0))
                else if (!poolInterchangeIsOn) {
                    val randomNumber = (Math.random() * 100).toInt()        // TODO
                    it.onNext(randomNumber)
                }
            }
        }
    }

    private fun runThatForever(locker: ReentrantLock): Observable<Int> {
        return Observable.create {
            while (true) {
                // Что значит "постоянно"?
                val randomArrayIncrementationTime = Random().nextLong().absoluteValue % 6000
                Thread.sleep(randomArrayIncrementationTime)

                // Лучше вообще тред не создавать тогда...
                if (!valuesIncreasingIsOn)
                    continue

                try {
                    locker.lock()
                    for (i in 0 until digitsList.size)
                        digitsList[i]++

                } finally {
                    locker.unlock()
                }

                it.onNext(1)                // TODO onComplete or something
            }
        }
    }
}