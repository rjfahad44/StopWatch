package com.ft.ltd.di

import android.app.NotificationManager
import android.content.Context
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.core.app.NotificationCompat
import com.ft.ltd.R
import com.ft.ltd.service.ServiceHelper
import com.ft.ltd.util.Constants.NOTIFICATION_CHANNEL_ID
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@ExperimentalAnimationApi
@Module
@InstallIn(ServiceComponent::class)
object NotificationModel {

    @ServiceScoped
    @Provides
    fun provideNotificationBuilder(@ApplicationContext context: Context): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("StopWatch")
            .setContentText("00:00:00")
            .setSmallIcon(R.drawable.ic_baseline_timer_24)
//            .setAutoCancel(false)
            .setOngoing(true)
            .addAction(R.drawable.ic_baseline_pause_24, "Stop", ServiceHelper.stopPendingIntent(context))
            .addAction(R.drawable.ic_baseline_close_24, "Cancel", ServiceHelper.cancelPendingIntent(context))
            .setContentIntent(ServiceHelper.clickPendingIntent(context))
    }

    @ServiceScoped
    @Provides
    fun provideNotificationManager(@ApplicationContext context: Context): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
}