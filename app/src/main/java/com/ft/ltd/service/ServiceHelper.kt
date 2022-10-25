package com.ft.ltd.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.animation.ExperimentalAnimationApi
import com.ft.ltd.MainActivity
import com.ft.ltd.util.Constants.CANCEL_REQUEST_CODE
import com.ft.ltd.util.Constants.CLICK_REQUEST_CODE
import com.ft.ltd.util.Constants.RESUME_REQUEST_CODE
import com.ft.ltd.util.Constants.STOPWATCH_STATE
import com.ft.ltd.util.Constants.STOP_REQUEST_CODE

@ExperimentalAnimationApi
object ServiceHelper {
    private val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0

    fun clickPendingIntent(context: Context): PendingIntent{
        val clickIntent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(context, CLICK_REQUEST_CODE, clickIntent, flag)
    }

    fun stopPendingIntent(context: Context): PendingIntent{
        val stopIntent = Intent(context, StopWatchService::class.java).apply {
            putExtra(STOPWATCH_STATE, StopWatchState.Stopped.name)
        }
        return PendingIntent.getActivity(context, STOP_REQUEST_CODE, stopIntent, flag)
    }

    fun resumePendingIntent(context: Context): PendingIntent{
        val resumeIntent = Intent(context, StopWatchService::class.java).apply {
            putExtra(STOPWATCH_STATE, StopWatchState.Started.name)
        }
        return PendingIntent.getActivity(context, RESUME_REQUEST_CODE, resumeIntent, flag)
    }

    fun cancelPendingIntent(context: Context): PendingIntent{
        val cancelIntent = Intent(context, StopWatchService::class.java).apply {
            putExtra(STOPWATCH_STATE, StopWatchState.Canceled.name)
        }
        return PendingIntent.getActivity(context, CANCEL_REQUEST_CODE, cancelIntent, flag)
    }

    fun triggerForegroundService(context: Context, action: String){
        Intent(context, StopWatchService::class.java).apply {
            this.action = action
            context.startService(this)
        }
    }
}