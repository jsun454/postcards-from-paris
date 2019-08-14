package com.example.jeffrey.postcardsfromparis.util

import android.view.GestureDetector
import android.view.MotionEvent

/**
 * This interface can be used to handle single tap gesture events without the other boilerplate functions
 */
interface SingleTapGestureListener : GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    override fun onShowPress(e: MotionEvent?) {}
    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean = true
    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean = true
    override fun onLongPress(e: MotionEvent?) {}
    override fun onDoubleTap(e: MotionEvent?): Boolean = true
    override fun onDoubleTapEvent(e: MotionEvent?): Boolean = true
    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean = true
}