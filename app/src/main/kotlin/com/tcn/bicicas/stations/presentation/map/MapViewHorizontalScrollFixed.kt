package com.tcn.bicicas.stations.presentation.map

import android.content.Context
import android.view.MotionEvent
import com.google.android.gms.maps.MapView

class MapViewHorizontalScrollFixed(context: Context) : MapView(context) {

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN ->
                parent.requestDisallowInterceptTouchEvent(true)

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP ->
                parent.requestDisallowInterceptTouchEvent(false)

            else -> Unit
        }
        return super.onInterceptTouchEvent(ev)
    }

}