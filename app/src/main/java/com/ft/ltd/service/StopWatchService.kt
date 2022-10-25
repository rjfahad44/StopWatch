package com.ft.ltd.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import com.ft.ltd.R
import com.ft.ltd.util.Constants.ACTION_SERVICE_CANCEL
import com.ft.ltd.util.Constants.ACTION_SERVICE_START
import com.ft.ltd.util.Constants.ACTION_SERVICE_STOP
import com.ft.ltd.util.Constants.NOTIFICATION_CHANNEL_ID
import com.ft.ltd.util.Constants.NOTIFICATION_CHANNEL_NAME
import com.ft.ltd.util.Constants.NOTIFICATION_ID
import com.ft.ltd.util.Constants.STOPWATCH_STATE
import com.ft.ltd.util.formatTime
import com.ft.ltd.util.pad
import dagger.hilt.android.AndroidEntryPoint
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


@SuppressLint("NewApi")
@ExperimentalAnimationApi
@AndroidEntryPoint
class StopWatchService: Service() {

    @Inject
    lateinit var notificationManager: NotificationManager
    @Inject
    lateinit var notificationBuilder: NotificationCompat.Builder

    private val binder = StopWatchBinder()
    private var duration: Duration = Duration.ZERO
    private lateinit var timer: Timer

    var hourS = mutableStateOf("00")
        private set
    var minuteS = mutableStateOf("00")
        private set
    var secondS = mutableStateOf("00")
        private set

    var currentState = mutableStateOf(StopWatchState.Idle)
        private set

    override fun onBind(intent: Intent?) = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.getStringExtra(STOPWATCH_STATE)){
            StopWatchState.Started.name->{
                setResumeButton()
                startForegroundService()
                startStopWatch{ hourS, minuteS, secondS ->
                    updateNotification(hourS = hourS, minuteS = minuteS, secondS = secondS)
                }
            }
            StopWatchState.Stopped.name->{
                stopStopWatch()
                setStopButton()
            }

            StopWatchState.Canceled.name->{
                stopStopWatch()
                cancelStopWatch()
                stopForegroundService()
            }
        }

        intent?.action.let {
            when(it){
                ACTION_SERVICE_START->{
                    stopStopWatch()
                    startForegroundService()
                    startStopWatch { h, m, s ->
                        updateNotification(hourS = h, minuteS = m, secondS = s)
                    }
                }

                ACTION_SERVICE_STOP->{
                    stopStopWatch()
                    setResumeButton()
                }

                ACTION_SERVICE_CANCEL->{
                    stopStopWatch()
                    cancelStopWatch()
                    stopForegroundService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
    
    private fun startStopWatch(onTick: (h: String, m: String, s: String)-> Unit){
        currentState.value = StopWatchState.Started
        timer = fixedRateTimer(initialDelay = 1000L, period = 1000L){
            duration = duration.plus(1.seconds)
            updateTimeUnits()
            onTick(hourS.value, minuteS.value, secondS.value)
        }
    }

    private fun stopStopWatch(){
        if (this::timer.isInitialized){
            timer.cancel()
        }
        currentState.value = StopWatchState.Stopped
    }

    private fun cancelStopWatch(){
        if (this::timer.isInitialized){
            timer.cancel()
        }
        currentState.value = StopWatchState.Stopped
    }

    private fun updateTimeUnits() {
        duration.toComponents { hourS, minuteS, secondS, _->
            this@StopWatchService.hourS.value = hourS.toInt().pad()
            this@StopWatchService.minuteS.value = minuteS.toInt().pad()
            this@StopWatchService.secondS.value = secondS.toInt().pad()
        }
    }

    private fun startForegroundService(){
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun stopForegroundService(){
        notificationManager.cancel(NOTIFICATION_ID)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun updateNotification(hourS: String, minuteS: String, secondS: String) {
        notificationManager.notify(
            NOTIFICATION_ID,
            notificationBuilder.setContentText(
                formatTime(
                    hours = hourS,
                    minutes = minuteS,
                    seconds = secondS
                )
            ).build()
        )
    }


    @SuppressLint("RestrictedApi")
    private fun setStopButton() {
        notificationBuilder.mActions.removeAt(0)
        notificationBuilder.mActions.add(
            0,
            NotificationCompat.Action(
                R.drawable.ic_baseline_stop_24,
                "Stop",
                ServiceHelper.stopPendingIntent(this)
            )
        )
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    @SuppressLint("RestrictedApi")
    private fun setResumeButton() {
        notificationBuilder.mActions.removeAt(0)
        notificationBuilder.mActions.add(
            0,
            NotificationCompat.Action(
                R.drawable.ic_baseline_pause_24,
                "Resume",
                ServiceHelper.resumePendingIntent(this)
            )
        )
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    inner class StopWatchBinder: Binder(){
        fun getService(): StopWatchService = this@StopWatchService
    }
}

enum class StopWatchState{
    Idle,
    Started,
    Stopped,
    Canceled
}