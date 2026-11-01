// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS

package com.maigus.aynehahorloge.widget

import android.content.Context
import android.graphics.*
import androidx.core.content.res.ResourcesCompat
import com.maigus.aynehahorloge.AynehaGlyphs
import com.maigus.aynehahorloge.R
import com.maigus.aynehahorloge.SonghayCalendar
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min

/**
 * Dessine le contenu des widgets dans une image, avec la vraie police AYNEHA.
 *
 * Les widgets Android (RemoteViews) n'appliquent pas une police personnalisée
 * de façon fiable : le texte est donc dessiné ici puis posé dans un ImageView.
 *
 * Vrai RTL : le texte reste dans l'ordre de lecture, et chaque glyphe est
 * posé un par un en partant de la DROITE. Aucune inversion de chaîne.
 * L'image prend la taille réelle du widget : elle grandit quand on l'agrandit.
 */
object GlyphBitmap {

    private const val INK = 0xFFF3ECE0.toInt()
    private const val INK_DIM = 0xFFB9C0CF.toInt()
    private const val ORANGE = 0xFFFF8A3D.toInt()
    private const val MOON_LIT = 0xFFF6E7C8.toInt()
    private const val MOON_DARK = 0xFF22324F.toInt()

    private var cachedTypeface: Typeface? = null

    private fun typeface(context: Context): Typeface =
        cachedTypeface ?: ResourcesCompat.getFont(context, R.font.ayneha_pro)!!.also { cachedTypeface = it }

    private fun glyphs(text: String): List<String> {
        val out = ArrayList<String>()
        var i = 0
        while (i < text.length) {
            val cp = text.codePointAt(i)
            out.add(String(Character.toChars(cp)))
            i += Character.charCount(cp)
        }
        return out
    }

    private fun paint(context: Context, size: Float, color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = typeface(context); textSize = size; this.color = color
    }

    private fun widthOf(p: Paint, text: String): Float =
        glyphs(text).sumOf { (p.measureText(it) + p.textSize * 0.04f).toDouble() }.toFloat()

    /** Réduit la taille du texte pour qu'il tienne dans [maxW]. */
    private fun fit(p: Paint, text: String, maxW: Float) {
        val w = widthOf(p, text)
        if (w > maxW && w > 0f) p.textSize = p.textSize * maxW / w
    }

    /** Écrit [text] centré sur [cx], glyphe par glyphe, de droite à gauche. */
    private fun drawRtl(c: Canvas, p: Paint, text: String, cx: Float, baseline: Float) {
        var x = cx + widthOf(p, text) / 2f
        for (gl in glyphs(text)) {
            val w = p.measureText(gl) + p.textSize * 0.04f
            x -= w
            c.drawText(gl, x, baseline, p)
        }
    }

    fun drawMoon(c: Canvas, cx: Float, cy: Float, r: Float, phase: Double) {
        val pDark = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = MOON_DARK }
        val pLit = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = MOON_LIT }
        c.drawCircle(cx, cy, r, pDark)
        val k = cos(2 * Math.PI * phase)
        val rx = (abs(k) * r).toFloat()
        val waxing = phase < 0.5
        val path = Path()
        path.moveTo(cx, cy - r)
        path.arcTo(RectF(cx - r, cy - r, cx + r, cy + r), -90f, if (waxing) 180f else -180f)
        val inner = if (waxing) (if (k > 0) -180f else 180f) else (if (k > 0) 180f else -180f)
        path.arcTo(RectF(cx - rx, cy - r, cx + rx, cy + r), 90f, inner)
        path.close()
        c.drawPath(path, pLit)
    }

    /** Taille en pixels raisonnable pour un widget (limite mémoire des widgets). */
    private fun size(context: Context, wDp: Int, hDp: Int): Pair<Int, Int> {
        val d = context.resources.displayMetrics.density
        var w = (wDp.coerceAtLeast(60) * d)
        var h = (hDp.coerceAtLeast(40) * d)
        val scale = min(1f, 900f / maxOf(w, h))
        w *= scale; h *= scale
        return Pair(w.toInt(), h.toInt())
    }

    /** Petit widget : guuru + heure. */
    fun renderSmall(context: Context, wDp: Int, hDp: Int, now: Calendar): Bitmap {
        val (w, h) = size(context, wDp, hDp)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val pad = w * 0.08f
        val time = AynehaGlyphs.timeHM(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE))

        val pLabel = paint(context, h * 0.20f, INK_DIM)
        fit(pLabel, AynehaGlyphs.LABEL_GUURU, w - 2 * pad)
        val pTime = paint(context, h * 0.50f, ORANGE)
        fit(pTime, time, w - 2 * pad)

        val total = pLabel.textSize * 1.1f + pTime.textSize
        val top = (h - total) / 2f
        drawRtl(c, pLabel, AynehaGlyphs.LABEL_GUURU, w / 2f, top + pLabel.textSize * 0.85f)
        drawRtl(c, pTime, time, w / 2f, top + pLabel.textSize * 1.1f + pTime.textSize * 0.85f)
        return bmp
    }

    /** Grand widget : heure, jour, lune, date lunaire et jiiri. */
    fun renderLarge(context: Context, wDp: Int, hDp: Int, now: Calendar): Bitmap {
        val (w, h) = size(context, wDp, hDp)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val pad = w * 0.07f
        val inner = w - 2 * pad

        val lun = SonghayCalendar.lunarNow(context, now)
        val time = AynehaGlyphs.timeHM(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE))
        val dayName = AynehaGlyphs.DAYS_FULL[now.get(Calendar.DAY_OF_WEEK) - 1]
        // Ordre songhay : le mois, puis le jour (le mois se lit en premier, à droite).
        val lunarText = AynehaGlyphs.LUNAR_MONTHS[lun.month - 1] + " " + AynehaGlyphs.num(lun.day)
        val yearText = AynehaGlyphs.num(lun.year)
        // Hauteurs relatives des 4 lignes.
        val pTime = paint(context, h * 0.27f, ORANGE).also { fit(it, time, inner) }
        val pDay = paint(context, h * 0.14f, INK).also { fit(it, dayName, inner * 0.62f) }
        val pLunar = paint(context, h * 0.15f, ORANGE).also { fit(it, lunarText, inner) }
        val pYear = paint(context, h * 0.12f, INK_DIM).also { fit(it, yearText, inner) }

        val gap = h * 0.045f
        val moonR = pDay.textSize * 0.55f
        val rowDay = maxOf(pDay.textSize, moonR * 2)
        val total = pTime.textSize + rowDay + pLunar.textSize + pYear.textSize + gap * 3
        var y = (h - total) / 2f

        drawRtl(c, pTime, time, w / 2f, y + pTime.textSize * 0.85f)
        y += pTime.textSize + gap

        // Ligne : jour (à droite) et lune (à gauche).
        val dayW = widthOf(pDay, dayName)
        val rowW = dayW + moonR * 2 + w * 0.05f
        val rowRight = w / 2f + rowW / 2f
        drawRtl(c, pDay, dayName, rowRight - dayW / 2f, y + rowDay / 2f + pDay.textSize * 0.32f)
        drawMoon(c, rowRight - rowW + moonR, y + rowDay / 2f, moonR, SonghayCalendar.moonPhase(now.timeInMillis))
        y += rowDay + gap

        drawRtl(c, pLunar, lunarText, w / 2f, y + pLunar.textSize * 0.85f)
        y += pLunar.textSize + gap
        drawRtl(c, pYear, yearText, w / 2f, y + pYear.textSize * 0.85f)
        return bmp
    }
}
