// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS

package com.maigus.aynehahorloge.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Reçoit : le « tic » de chaque minute, le redémarrage du téléphone, la
 * mise à jour de l'application, et tout changement d'heure, de date ou de
 * fuseau horaire. Dans tous les cas : redessiner les widgets et replanifier.
 */
class SystemEventsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        WidgetUpdater.refresh(context)
    }
}
