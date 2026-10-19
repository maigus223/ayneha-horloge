// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS

package com.maigus.aynehahorloge.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.maigus.aynehahorloge.AynehaGlyphs
import com.maigus.aynehahorloge.MainActivity
import com.maigus.aynehahorloge.R
import java.util.Calendar

/**
 * Widget "Horloge" : affiche l'heure (heures:minutes) en glyphes AYNEHA.
 *
 * Android ne permet pas de rafraîchir un widget seconde par seconde (ni même
 * de garantir updatePeriodMillis en dessous de ~30 min). On simule donc une
 * mise à jour à la minute grâce à une alarme exacte qui se replanifie
 * elle-même juste après chaque minute pleine — c'est la même technique que
 * les widgets d'horloge du système Android.
 */
class HorlogeWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_TICK = "com.maigus.aynehahorloge.ACTION_TICK_MINUTE"

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, HorlogeWidgetProvider::class.java))
            for (id in ids) {
                updateWidget(context, manager, id)
            }
        }

        private fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
            val cal = Calendar.getInstance()
            val views = RemoteViews(context.packageName, R.layout.widget_horloge)

            // Texte AYNEHA pre-rendu en bitmap (RemoteViews n'applique pas les
            // polices custom sur un TextView) : voir GlyphBitmap.kt.
            // Mention au-dessus de l'heure : taille multipliée par 1,5 (13sp -> 19,5sp).
            views.setImageViewBitmap(
                R.id.iv_label_guuru,
                GlyphBitmap.render(
                    context, AynehaGlyphs.LABEL_GUURU,
                    textSizeSp = 19.5f, color = 0xFFD9CDB4.toInt(), letterSpacingEm = 0.05f
                )
            )
            views.setImageViewBitmap(
                R.id.iv_time,
                GlyphBitmap.render(
                    context, AynehaGlyphs.timeGlyphsHM(cal),
                    textSizeSp = 30f, color = 0xFFE0AC47.toInt(), letterSpacingEm = 0.08f
                )
            )

            val openApp = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, openApp,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.iv_time, pendingIntent)
            views.setOnClickPendingIntent(R.id.iv_label_guuru, pendingIntent)

            manager.updateAppWidget(widgetId, views)
        }

        private fun scheduleNextTick(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, HorlogeWidgetProvider::class.java).apply { action = ACTION_TICK }
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val next = Calendar.getInstance().apply {
                add(Calendar.MINUTE, 1)
                set(Calendar.SECOND, 1)
                set(Calendar.MILLISECOND, 0)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, next.timeInMillis, pendingIntent
                )
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, next.timeInMillis, pendingIntent)
            }
        }

        private fun cancelTicks(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, HorlogeWidgetProvider::class.java).apply { action = ACTION_TICK }
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        updateAll(context)
        scheduleNextTick(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TICK) {
            updateAll(context)
            scheduleNextTick(context)
        }
    }

    override fun onEnabled(context: Context) {
        scheduleNextTick(context)
    }

    override fun onDisabled(context: Context) {
        cancelTicks(context)
    }
}
