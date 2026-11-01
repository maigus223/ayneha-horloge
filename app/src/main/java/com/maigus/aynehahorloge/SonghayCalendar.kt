// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS

package com.maigus.aynehahorloge

import android.content.Context
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.*

/**
 * Calendrier lunaire songhay - mêmes calculs que index.html.
 * Chaque mois commence le lendemain du premier soir où le croissant de lune
 * est visible à l'oeil nu quelque part sur Terre, avant minuit UTC (critère
 * d'Odeh) : le même calendrier pour tous. Les débuts de mois de 1899 à 2201
 * sont précalculés dans MOIS_HEX (1 bit par mois : 1 = 30 jours, 0 = 29 jours),
 * le même tableau que index.html. En dehors : cycle arithmétique de 30 ans.
 * Le jour lunaire change au coucher du soleil, là où l'on se trouve.
 */
object SonghayCalendar {

    const val LUNAR_YEAR_OFFSET = 2577
    private const val GAO_LAT = 16.2666
    private const val GAO_LON = -0.0447
    private const val KEY_PLACE = "place"

    /** Grande ville de chaque fuseau horaire (même liste que index.html). */
    private val FUSEAUX: Map<String, DoubleArray> = mapOf(
        "Africa/Bamako" to doubleArrayOf(16.27, -0.04),
        "Africa/Niamey" to doubleArrayOf(13.51, 2.11),
        "Africa/Ouagadougou" to doubleArrayOf(12.37, -1.52),
        "Africa/Porto-Novo" to doubleArrayOf(6.50, 2.60),
        "Africa/Lome" to doubleArrayOf(6.13, 1.22),
        "Africa/Accra" to doubleArrayOf(5.60, -0.19),
        "Africa/Lagos" to doubleArrayOf(6.52, 3.38),
        "Africa/Casablanca" to doubleArrayOf(33.57, -7.59),
        "Africa/Algiers" to doubleArrayOf(36.75, 3.06),
        "Africa/Abidjan" to doubleArrayOf(5.36, -4.01),
        "Africa/Khartoum" to doubleArrayOf(15.50, 32.56),
        "Africa/Ndjamena" to doubleArrayOf(12.13, 15.06),
        "Africa/Dakar" to doubleArrayOf(14.69, -17.44),
        "Africa/Nouakchott" to doubleArrayOf(18.08, -15.98),
        "Africa/Conakry" to doubleArrayOf(9.64, -13.58),
        "Africa/Freetown" to doubleArrayOf(8.48, -13.23),
        "Africa/Monrovia" to doubleArrayOf(6.30, -10.80),
        "Africa/Banjul" to doubleArrayOf(13.45, -16.58),
        "Africa/Bissau" to doubleArrayOf(11.86, -15.60),
        "Africa/Tripoli" to doubleArrayOf(32.89, 13.19),
        "Africa/Tunis" to doubleArrayOf(36.81, 10.18),
        "Africa/Cairo" to doubleArrayOf(30.04, 31.24),
        "Africa/Douala" to doubleArrayOf(4.05, 9.70),
        "Africa/Libreville" to doubleArrayOf(0.39, 9.45),
        "Africa/Kinshasa" to doubleArrayOf(-4.32, 15.31),
        "Africa/Nairobi" to doubleArrayOf(-1.29, 36.82),
        "Africa/Johannesburg" to doubleArrayOf(-26.20, 28.05),
        "Europe/Paris" to doubleArrayOf(48.86, 2.35),
        "Europe/Brussels" to doubleArrayOf(50.85, 4.35),
        "Europe/Madrid" to doubleArrayOf(40.42, -3.70),
        "Europe/Rome" to doubleArrayOf(41.90, 12.50),
        "Europe/Berlin" to doubleArrayOf(52.52, 13.40),
        "Europe/London" to doubleArrayOf(51.51, -0.13),
        "Europe/Lisbon" to doubleArrayOf(38.72, -9.14),
        "Europe/Amsterdam" to doubleArrayOf(52.37, 4.90),
        "Europe/Zurich" to doubleArrayOf(47.37, 8.54),
        "Europe/Istanbul" to doubleArrayOf(41.01, 28.98),
        "Asia/Riyadh" to doubleArrayOf(24.71, 46.68),
        "Asia/Dubai" to doubleArrayOf(25.20, 55.27),
        "Asia/Qatar" to doubleArrayOf(25.29, 51.53),
        "Asia/Shanghai" to doubleArrayOf(31.23, 121.47),
        "Asia/Kolkata" to doubleArrayOf(28.61, 77.21),
        "America/New_York" to doubleArrayOf(40.71, -74.01),
        "America/Toronto" to doubleArrayOf(43.65, -79.38),
        "America/Montreal" to doubleArrayOf(45.50, -73.57),
        "America/Chicago" to doubleArrayOf(41.88, -87.63),
        "America/Denver" to doubleArrayOf(39.74, -104.99),
        "America/Los_Angeles" to doubleArrayOf(34.05, -118.24),
        "America/Sao_Paulo" to doubleArrayOf(-23.55, -46.63)
    )
    private const val ISL_EPOCH = 1948439.5
    private const val PREFS = "ayneha"
    private const val KEY_ADJUST = "lunarAdjust"

    data class LunarDate(val day: Int, val month: Int, val year: Int)

    fun getAdjust(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_ADJUST, 0)

    /** Position donnée par le bouton « position » de la page ("lat,lon"), ou "". */
    fun getPlace(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_PLACE, "") ?: ""

    fun setPlace(context: Context, lat: Double, lon: Double) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_PLACE, "$lat,$lon").apply()
    }

    /** Lieu du coucher du soleil : position enregistrée, sinon ville du fuseau horaire, sinon Gao. */
    fun place(context: Context): DoubleArray {
        val v = getPlace(context).split(',')
        if (v.size == 2) {
            val lat = v[0].toDoubleOrNull(); val lon = v[1].toDoubleOrNull()
            if (lat != null && lon != null) return doubleArrayOf(lat, lon)
        }
        val tz = TimeZone.getDefault()
        FUSEAUX[tz.id]?.let { return it }
        val h = tz.getOffset(System.currentTimeMillis()) / 3600000.0
        return if (h == 0.0) doubleArrayOf(GAO_LAT, GAO_LON) else doubleArrayOf(15.0, h * 15)
    }

    fun setAdjust(context: Context, value: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putInt(KEY_ADJUST, value.coerceIn(-2, 2)).apply()
    }

    private fun gregToJd(y: Int, m: Int, d: Int): Double {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear(); set(y, m - 1, d)
        }
        return cal.timeInMillis / 86400000.0 + 2440587.5
    }

    private const val MOIS_JD0 = 2414785.5
    private const val MOIS_Y0 = 1317
    private const val MOIS_N = 3732
    private const val MOIS_HEX =
        "b5556a9754b725752ba9572ab5555aa5d52da95d49ea4ed26d555aa5b52b6a5751ba8bb49b54d6a5ad4b5a55d49ea2dd15e52ea96d4ada95b4aba93b45ba2baa5b52b6956b2ad94ed26e8aec56d2aea56b2ad555b29b94bb15b4aba55b4ab5556a96d4aea96d46e9574a75555aaad52da95b51ba8dd45b94daa6b54b6a56d2aea2f516e8b6a96d4b5a95b4aba53d45da2dd15d52da95b4ab6956c9764af45752b6956d2ad555b25b92bc95d4ada55d2ad5556aab54b6a5752769374ab6556aaad52da95d4ada4dd26d935aa6b5556a5752b693749764b725b54b6a56d4ada4dd25e92ec96d4ada96b4ab695749764bb25b52b695b4ab5955d26d92dc95e4aea56d2ad555aaab54bb25792ba95b4ab655aaaad54ea96d4aea56d26e936caad555aa5b52ba95b49ba4bb25b54d6a9ad4aea56d26e92f49754b6a56d52da95b49ba4bb25d92da95b4ab5956d2ae92ec96e4aea5752b5956aaaba4bb25d92bc95d4ada55b2ab5556c96d4aea5752b6936caad555aa6b52ba95d49da4dd26d555aa9b54b6a57527693749764b6a5ad52da95d4ada4dd25d92da96d4ada95ad2b693749764b725752b6956caada55d25d92ec95d4aea56b2ab5556c9b64b725752ba95b4ab5556aa6d52dc95d4aea4ed2ad555aaab5"

    /** MOIS_DEBUT[i] = jour julien du 1er jour du i-ème mois du tableau. */
    private val MOIS_DEBUT: DoubleArray by lazy {
        val t = DoubleArray(MOIS_N + 1)
        t[0] = MOIS_JD0
        for (i in 0 until MOIS_N) {
            val bit = (Character.digit(MOIS_HEX[i shr 2], 16) shr (3 - (i and 3))) and 1
            t[i + 1] = t[i] + if (bit == 1) 30 else 29
        }
        t
    }

    private fun islToJdA(y: Int, m: Int, d: Int): Double =
        d + ceil(29.5 * (m - 1)) + (y - 1) * 354 + floor((3 + 11 * y) / 30.0) + ISL_EPOCH - 1

    private fun jdToIslA(jd: Double): LunarDate {
        val y = floor((30 * (jd - ISL_EPOCH) + 10646) / 10631).toInt()
        val m = min(12, (ceil((jd - (29 + islToJdA(y, 1, 1))) / 29.5) + 1).toInt())
        val d = (jd - islToJdA(y, m, 1) + 1).toInt()
        return LunarDate(d, m, y)
    }

    private fun jdToIsl(jdIn: Double): LunarDate {
        val jd = floor(jdIn) + 0.5
        val t = MOIS_DEBUT
        if (jd < t[0] || jd >= t[MOIS_N]) return jdToIslA(jd)
        var lo = 0
        var hi = MOIS_N - 1
        while (lo < hi) {
            val mid = (lo + hi + 1) shr 1
            if (t[mid] <= jd) lo = mid else hi = mid - 1
        }
        return LunarDate((jd - t[lo] + 1).toInt(), lo % 12 + 1, MOIS_Y0 + lo / 12)
    }

    /** Date lunaire correspondant à la journée grégorienne y-m-d (avant le coucher du soleil). */
    fun lunarOf(y: Int, m: Int, d: Int, adjust: Int): LunarDate {
        val r = jdToIsl(gregToJd(y, m, d) - adjust)
        return LunarDate(r.day, r.month, r.year + LUNAR_YEAR_OFFSET)
    }

    /** Coucher du soleil au lieu (lat, lon), en millisecondes (UTC). */
    fun sunsetMillis(y: Int, m: Int, d: Int, lat: Double = GAO_LAT, lon: Double = GAO_LON): Long {
        val rad = PI / 180
        val n = (gregToJd(y, m, d) + 0.5 - 2451545.0).roundToLong()
        val js = n - lon / 360
        val mm = (357.5291 + 0.98560028 * js) % 360
        val c = 1.9148 * sin(mm * rad) + 0.02 * sin(2 * mm * rad) + 0.0003 * sin(3 * mm * rad)
        val l = (mm + c + 180 + 102.9372) % 360
        val jt = 2451545.0 + js + 0.0053 * sin(mm * rad) - 0.0069 * sin(2 * l * rad)
        val dec = asin(sin(l * rad) * sin(23.4397 * rad))
        val cw = (sin(-0.833 * rad) - sin(lat * rad) * sin(dec)) / (cos(lat * rad) * cos(dec))
        val w = acos(cw.coerceIn(-1.0, 1.0)) / rad
        return ((jt + w / 360 - 2440587.5) * 86400000).toLong()
    }

    /** Date lunaire « maintenant » : après le coucher du soleil, c'est déjà le jour suivant. */
    fun lunarNow(context: Context, now: Calendar = Calendar.getInstance()): LunarDate {
        val adjust = getAdjust(context)
        val y = now.get(Calendar.YEAR)
        val m = now.get(Calendar.MONTH) + 1
        val d = now.get(Calendar.DAY_OF_MONTH)
        val pl = place(context)
        if (now.timeInMillis >= sunsetMillis(y, m, d, pl[0], pl[1])) {
            val t = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
            return lunarOf(t.get(Calendar.YEAR), t.get(Calendar.MONTH) + 1, t.get(Calendar.DAY_OF_MONTH), adjust)
        }
        return lunarOf(y, m, d, adjust)
    }

    /** Phase de la lune : 0 = nouvelle lune, 0,5 = pleine lune. */
    fun moonPhase(millis: Long): Double {
        val syn = 29.530588853
        val jd = millis / 86400000.0 + 2440587.5
        var a = (jd - 2451550.26) % syn
        if (a < 0) a += syn
        return a / syn
    }
}
