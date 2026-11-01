// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS

package com.maigus.aynehahorloge

/**
 * Glyphes AYNEHA (Plan 16, police ayneha_pro.ttf), identiques à index.html.
 * Tout texte est gardé dans l'ordre LOGIQUE (ordre de lecture) : c'est le
 * dessin (GlyphBitmap) qui place le premier glyphe à droite. Aucune inversion.
 */
object AynehaGlyphs {

    /** g("8B 9C") -> U+10FF8B U+10FF9C */
    private fun g(s: String): String = s.split(' ').joinToString("") {
        String(Character.toChars(0x10FF00 + it.toInt(16)))
    }

    private val DIGITS = listOf("7C", "7D", "7E", "7F", "80", "81", "82", "83", "84", "85").map { g(it) }

    val LABEL_GUURU = g("98 AB 8F A0")   // guuru (heure)
    val LABEL_HAN = g("9A 9C BC")        // han (jour)
    val LABEL_HADU = g("9A A2 8B A0")    // hãdu (mois)
    val LABEL_HADO = g("9A A2 8B 9F")    // hãdo (lune)
    val LABEL_JIIRI = g("94 A6 8F 9E")   // jiiri (année)

    /** Index 0 = dimanche (Calendar.DAY_OF_WEEK - 1) : fahãtedji, mahãtedji, bohãtedji,
     *  woyhãtedji, tohãtedji, zĩbuza, duhãtedji. Semaine : samedi -> vendredi. */
    val DAYS_FULL = listOf(
        "89 9C 9A A2 8C 9D 95 9E", "88 9C 9A A2 8C 9D 95 9E", "86 9F 9A A2 8C 9D 95 9E",
        "8A 9F CC 9A A2 8C 9D 95 9E", "8C 9F 9A A2 8C 9D 95 9E", "91 A7 86 A0 91 9C",
        "8B A0 9A A2 8C 9D 95 9E"
    ).map { g(it) }

    /** Les 12 mois songhay (lunaires), index 0 = 1er mois du jiiri : dadaw-beeri,
     *  dadaw-kayna, gaani, gaani-bãda, bĩkuna, bĩbina, woyzena, cekɔnna, meehaw,
     *  meefeeri, hunãzam, cibsi. */
    val LUNAR_MONTHS = listOf(
        "8B 9C 8B 9C B6 86 A3 8F 9E", "8B 9C 8B 9C B6 99 9C CC 8D 9C", "98 A1 8D 9E",
        "98 A1 8D 9E 86 A2 8B 9C", "86 A7 99 A0 8D 9C", "86 A7 86 9E 8D 9C",
        "8A 9F CC 91 9D 8D 9C", "93 9D 99 AA BB 9C", "88 A3 9A 9C B6",
        "88 A3 89 A3 8F 9E", "9A A0 8D A2 91 9C B2", "93 9E AE 90 9E"
    ).map { g(it) }

    /** Nombre en chiffres AYNEHA, ordre logique (12 -> glyphe 1, glyphe 2). */
    fun num(n: Int): String = n.toString().map { DIGITS[it - '0'] }.joinToString("")

    fun num2(n: Int): String = num(n).let { if (n < 10) DIGITS[0] + it else it }

    fun timeHM(hour: Int, minute: Int): String = num2(hour) + ":" + num2(minute)
}
