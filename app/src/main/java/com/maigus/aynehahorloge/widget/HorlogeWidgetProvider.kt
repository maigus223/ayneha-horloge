// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS

package com.maigus.aynehahorloge.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle

/** Petit widget : guuru + heure (heures : minutes), actualisé à la minute. */
class HorlogeWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        WidgetUpdater.refresh(context)
    }

    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, newOptions: Bundle) {
        // Le widget a été agrandi ou réduit : on redessine à la nouvelle taille.
        WidgetUpdater.update(context, manager, id, large = false)
    }

    override fun onEnabled(context: Context) = WidgetUpdater.refresh(context)

    override fun onDisabled(context: Context) = WidgetUpdater.scheduleNextTick(context)
}
