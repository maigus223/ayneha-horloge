// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS

package com.maigus.aynehahorloge.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.maigus.aynehahorloge.MainActivity
import com.maigus.aynehahorloge.R
import java.util.Calendar

/**
 * Met à jour les deux widgets et planifie le prochain rafraîchissement,
 * juste après chaque minute pleine (même principe que les horloges Android).
 */
object WidgetUpdater {

    const val ACTION_TICK = "com.maigus.aynehahorloge.ACTION_TICK_MINUTE"

    fun widgetCount(context: Context): Int {
        val m = AppWidgetManager.getInstance(context)
        return m.getAppWidgetIds(ComponentName(context, HorlogeWidgetProvider::class.java)).size +
            m.getAppWidgetIds(ComponentName(context, GrandWidgetProvider::class.java)).size
    }

    fun updateAll(context: Context) {
        val m = AppWidgetManager.getInstance(context)
        for (id in m.getAppWidgetIds(ComponentName(context, HorlogeWidgetProvider::class.java))) {
            update(context, m, id, large = false)
        }
        for (id in m.getAppWidgetIds(ComponentName(context, GrandWidgetProvider::class.java))) {
            update(context, m, id, large = true)
        }
    }

    fun update(context: Context, manager: AppWidgetManager, id: Int, large: Boolean) {
        val now = Calendar.getInstance()
        val opts = manager.getAppWidgetOptions(id)
        val portrait = context.resources.configuration.orientation != android.content.res.Configuration.ORIENTATION_LANDSCAPE
        // En portrait : largeur mini / hauteur maxi ; en paysage : l'inverse.
        val wDp = if (portrait) opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) else opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
        val hDp = if (portrait) opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT) else opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)

        val views = RemoteViews(context.packageName, if (large) R.layout.widget_grand else R.layout.widget_horloge)
        val bmp = if (large) {
            GlyphBitmap.renderLarge(context, if (wDp > 0) wDp else 250, if (hDp > 0) hDp else 180, now)
        } else {
            GlyphBitmap.renderSmall(context, if (wDp > 0) wDp else 130, if (hDp > 0) hDp else 70, now)
        }
        views.setImageViewBitmap(R.id.iv_widget, bmp)

        val open = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.iv_widget, open)
        manager.updateAppWidget(id, views)
    }

    private fun tickIntent(context: Context): PendingIntent {
        val intent = Intent(context, SystemEventsReceiver::class.java).apply { action = ACTION_TICK }
        return PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun scheduleNextTick(context: Context) {
        if (widgetCount(context) == 0) { cancel(context); return }
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val next = Calendar.getInstance().apply {
            add(Calendar.MINUTE, 1)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 200)
        }.timeInMillis
        val pi = tickIntent(context)
        try {
            val exactAllowed = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()
            when {
                exactAllowed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ->
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, pi)
                exactAllowed -> am.setExact(AlarmManager.RTC_WAKEUP, next, pi)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ->
                    am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next, pi)
                else -> am.set(AlarmManager.RTC_WAKEUP, next, pi)
            }
        } catch (e: SecurityException) {
            // Alarmes exactes refusées : alarme normale (peut avoir un peu de retard).
            am.set(AlarmManager.RTC_WAKEUP, next, pi)
        }
    }

    fun cancel(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(tickIntent(context))
    }

    /** Mise à jour immédiate + planification de la minute suivante. */
    fun refresh(context: Context) {
        updateAll(context)
        scheduleNextTick(context)
    }
}
