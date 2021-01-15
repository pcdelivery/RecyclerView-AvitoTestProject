package com.example.recyclersampleapplication

interface RecycleViewDataChanged {
    fun itemAdded(position: Int)
    fun itemRemoved(position: Int)
    fun eachDigitIncreased()
}