// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS

package com.maigus.aynehahorloge.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.TypedValue
import androidx.core.content.res.ResourcesCompat
import com.maigus.aynehahorloge.R

/**
 * RemoteViews (App Widgets) ne permet PAS d'appliquer une police personnalisée
 * (android:fontFamily) sur un TextView de façon fiable : selon la version
 * d'Android et le launcher (MIUI, etc.), l'attribut est simplement ignoré à
 * l'inflation du widget. Le TextView retombe alors sur la police système, qui
 * ne connaît pas les codepoints de la zone d'usage privé utilisés par AYNEHA
 * -> les glyphes s'affichent comme des carrés vides ("tofu").
 *
 * La solution robuste et universellement supportée par RemoteViews consiste à
 * dessiner nous-mêmes le texte (avec la vraie police AYNEHA) sur un Bitmap,
 * puis à poser ce Bitmap sur un ImageView du widget via setImageViewBitmap.
 */
object GlyphBitmap {

    private var cachedTypeface: Typeface? = null

    private fun typeface(context: Context): Typeface {
        return cachedTypeface ?: ResourcesCompat.getFont(context, R.font.ayneha_pro)!!.also {
            cachedTypeface = it
        }
    }

    /**
     * Rend [text] en glyphes AYNEHA sur un bitmap à fond transparent.
     * @param textSizeSp taille de police en sp (comme dans le layout XML d'origine)
     * @param color couleur ARGB (ex: 0xFFE0AC47.toInt())
     * @param letterSpacingEm espacement inter-lettres en "em", comme android:letterSpacing
     * @param rtl AYNEHA est une écriture RTL. Le site web l'affiche correctement
     *   car la page entière a `direction:rtl` en CSS (le navigateur réordonne
     *   alors les glyphes). Un Canvas.drawText ne connaît pas cette notion et
     *   dessine toujours dans l'ordre du texte, de gauche à droite : sans ce
     *   paramètre, les mots (jour, mois...) s'affichent donc à l'envers dans
     *   le widget. On corrige simplement en inversant l'ordre des glyphes
     *   avant de dessiner.
     */
    fun render(
        context: Context,
        text: String,
        textSizeSp: Float,
        color: Int,
        letterSpacingEm: Float = 0f,
        rtl: Boolean = false
    ): Bitmap {
        // StringBuilder.reverse() préserve les paires de substitution (plan 16)
        val text = if (rtl) StringBuilder(text).reverse().toString() else text
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface(context)
            this.color = color
            this.textSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, textSizeSp, context.resources.displayMetrics
            )
            this.letterSpacing = letterSpacingEm
        }

        val bounds = android.graphics.Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        val width = paint.measureText(text).let { if (it <= 0f) 1f else it }.let {
            it + Math.abs(bounds.left) // marge de sécurité si le premier glyphe déborde à gauche
        }.let { Math.ceil(it.toDouble()).toInt() + 2 }
        val height = (bounds.height() + 4).coerceAtLeast(1)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        // baseline = haut du bitmap + hauteur du texte au-dessus de la baseline
        val baseline = -bounds.top.toFloat() + 2f
        canvas.drawText(text, -bounds.left.toFloat(), baseline, paint)
        return bitmap
    }
}
