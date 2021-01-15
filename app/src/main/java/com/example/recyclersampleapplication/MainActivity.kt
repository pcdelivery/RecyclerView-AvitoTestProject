package com.example.recyclersampleapplication

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
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
        mDataLocker = ReentrantLock()

        recyclerView = findViewById(R.id.rv2)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = mLayoutManager

        recycledDigitsList = ArrayList()
        digitsList = ArrayList<Int>()
        for (i in 1..15)
            digitsList.add(i)

        recyclerViewAdapter = DigitsAdapter(digitsList, this)
        recyclerView.adapter = recyclerViewAdapter


        Log.d("Here!", "[Main Thread " + Thread.currentThread().id + "]")

        val dispose = runThisForever()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            Log.d("onNext", "it: $it [Observing at Thread " + Thread.currentThread().id + "]")

                            val randomIndex = (Math.random() * digitsList.size).toInt()

                            try {
                                mDataLocker.lock()
                                digitsList.add(randomIndex, it)
//                                recyclerViewAdapter.notifyDataSetChanged()
                                recyclerViewAdapter.notifyItemInserted(randomIndex)
                            }
                            finally {
                                mDataLocker.unlock()
                            }

                            Log.d("onNext", "New random place $randomIndex")
                        },
                        {
                            Log.d("onError", "it: $it")
                        },
                        {
                            Log.d("onComplete", "noit")
                        }
                )

        val dispose2 = runThatForever(mDataLocker)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            Log.d(RECYCLER_VIEW_INCREASER_TAG, "onNext branch: it $it")

                            recyclerViewAdapter.notifyDataSetChanged()
                            recyclerView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake))
                        },
                        {
                            Log.d(RECYCLER_VIEW_INCREASER_TAG, "onError branch: $it")
                        },
                        {
                            Log.d(RECYCLER_VIEW_INCREASER_TAG, "onComplete branch")
                        }
                )
    }

    override fun onViewClicked(clickedViewId: Int, clickedItemPosition: Int) {
        if (clickedViewId == R.id.deleteItemButton) {
            Log.d("0", "[viewid]: " + clickedViewId + " [itempos]: " + clickedItemPosition)
            Log.d("1", "array was: " + digitsList)

            try {
                mDataLocker.lock()
                recycledDigitsList.add(digitsList.removeAt(clickedItemPosition))
//                recyclerViewAdapter.notifyDataSetChanged()
                recyclerViewAdapter.notifyItemRemoved(clickedItemPosition)
            } finally {
                mDataLocker.unlock()
            }

            Log.d("1", "array now: " + digitsList)
            Log.d("1", "recycled array now: " + recycledDigitsList)

                // TODO is it necessary?
//            recyclerView.removeViewAt(clickedItemPosition)
//            recyclerViewAdapter.notifyItemRemoved(clickedItemPosition)
        }
    }

    override fun onViewLongClicked(clickedViewId: Int, clickedItemPosition: Int) {
        Toast.makeText(this, "Ok, maybe next time...", Toast.LENGTH_SHORT).show()
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
                Log.d("runThisForever", "[Running on Thread " + Thread.currentThread().id + "]")

//                val randomNumber = (Math.random() * 100).toInt()
//                it.onNext(randomNumber)

                if (recycledDigitsList.isNotEmpty())
                    it.onNext(recycledDigitsList.removeAt(0))
            }
        }
    }

    private fun runThatForever(locker: ReentrantLock): Observable<Int> {
        return Observable.create {
            while (true) {
                // Что значит "постоянно"?
                val randomArrayIncrementationTime = (Math.random() * 5000).toLong()      // TODO try << // TODO getrandomint
                Thread.sleep(randomArrayIncrementationTime)

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