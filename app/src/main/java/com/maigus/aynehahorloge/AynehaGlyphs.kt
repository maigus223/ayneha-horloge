// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS

package com.maigus.aynehahorloge

import java.util.Calendar

/**
 * Table des glyphes AYNEHA (zone d'usage privé Unicode) et fonctions de
 * conversion, reprises à l'identique de horloge.html pour garantir un
 * affichage cohérent entre l'application et les widgets.
 */
object AynehaGlyphs {

    private val DIGITS = arrayOf(
        "\uDBFF\uDF7C", "\uDBFF\uDF7D", "\uDBFF\uDF7E", "\uDBFF\uDF7F", "\uDBFF\uDF80",
        "\uDBFF\uDF81", "\uDBFF\uDF82", "\uDBFF\uDF83", "\uDBFF\uDF84", "\uDBFF\uDF85"
    )

    val LABEL_GUURU = "\uDBFF\uDF98\uDBFF\uDFAB\uDBFF\uDF8F\uDBFF\uDFA0" // heure
    val LABEL_HAN = "\uDBFF\uDF9A\uDBFF\uDF9C\uDBFF\uDFBC"          // jour
    val LABEL_HADU = "\uDBFF\uDF9A\uDBFF\uDFA2\uDBFF\uDF8B\uDBFF\uDFA0"   // mois

    val DAYS_FULL = arrayOf(
        "\uDBFF\uDF89\uDBFF\uDF9C\uDBFF\uDF9A\uDBFF\uDFA2\uDBFF\uDF8C\uDBFF\uDF9D\uDBFF\uDF95\uDBFF\uDF9E", // Dimanche
        "\uDBFF\uDF88\uDBFF\uDF9C\uDBFF\uDF9A\uDBFF\uDFA2\uDBFF\uDF8C\uDBFF\uDF9D\uDBFF\uDF95\uDBFF\uDF9E", // Lundi
        "\uDBFF\uDF86\uDBFF\uDF9F\uDBFF\uDF9A\uDBFF\uDFA2\uDBFF\uDF8C\uDBFF\uDF9D\uDBFF\uDF95\uDBFF\uDF9E", // Mardi
        "\uDBFF\uDF8A\uDBFF\uDF9F\uDBFF\uDFCC\uDBFF\uDF9A\uDBFF\uDFA2\uDBFF\uDF8C\uDBFF\uDF9D\uDBFF\uDF95\uDBFF\uDF9E", // Mercredi
        "\uDBFF\uDF8C\uDBFF\uDF9F\uDBFF\uDF9A\uDBFF\uDFA2\uDBFF\uDF8C\uDBFF\uDF9D\uDBFF\uDF95\uDBFF\uDF9E", // Jeudi
        "\uDBFF\uDF91\uDBFF\uDFA7\uDBFF\uDF86\uDBFF\uDFA0\uDBFF\uDF91\uDBFF\uDF9C",             // Vendredi
        "\uDBFF\uDF8B\uDBFF\uDFA0\uDBFF\uDF9A\uDBFF\uDFA2\uDBFF\uDF8C\uDBFF\uDF9D\uDBFF\uDF95\uDBFF\uDF9E"  // Samedi
    )

    val DAYS_SHORT = arrayOf(
        "\uDBFF\uDF89\uDBFF\uDF9C\uDBFF\uDF9A",
        "\uDBFF\uDF88\uDBFF\uDF9C\uDBFF\uDF9A",
        "\uDBFF\uDF86\uDBFF\uDF9F\uDBFF\uDF9A",
        "\uDBFF\uDF8A\uDBFF\uDF9F\uDBFF\uDFCC",
        "\uDBFF\uDF8C\uDBFF\uDF9F\uDBFF\uDF9A",
        "\uDBFF\uDF91\uDBFF\uDFA7\uDBFF\uDF86",
        "\uDBFF\uDF8B\uDBFF\uDFA0\uDBFF\uDF9A"
    )

    val MONTHS = arrayOf(
        "\uDBFF\uDF8B\uDBFF\uDF9C\uDBFF\uDF8B\uDBFF\uDF9C\uDBFF\uDFB6\uDBFF\uDF86\uDBFF\uDFA3\uDBFF\uDF8F\uDBFF\uDF9E",       // Janvier
        "\uDBFF\uDF8B\uDBFF\uDF9C\uDBFF\uDF8B\uDBFF\uDF9C\uDBFF\uDFB6\uDBFF\uDF99\uDBFF\uDF9C\uDBFF\uDFCC\uDBFF\uDF8D\uDBFF\uDF9C", // Février
        "\uDBFF\uDF98\uDBFF\uDFA1\uDBFF\uDF8D\uDBFF\uDF9E",                                     // Mars
        "\uDBFF\uDF98\uDBFF\uDFA1\uDBFF\uDF8D\uDBFF\uDF9E\uDBFF\uDF86\uDBFF\uDFA2\uDBFF\uDF8B\uDBFF\uDF9C",             // Avril
        "\uDBFF\uDF86\uDBFF\uDFA7\uDBFF\uDF99\uDBFF\uDFA0\uDBFF\uDF8D\uDBFF\uDF9C",                         // Mai
        "\uDBFF\uDF86\uDBFF\uDFA7\uDBFF\uDF86\uDBFF\uDF9E\uDBFF\uDF8D\uDBFF\uDF9C",                         // Juin
        "\uDBFF\uDF8A\uDBFF\uDF9F\uDBFF\uDFCC\uDBFF\uDF91\uDBFF\uDF9D\uDBFF\uDF8D\uDBFF\uDF9C",                   // Juillet
        "\uDBFF\uDF93\uDBFF\uDF9D\uDBFF\uDF99\uDBFF\uDFAA\uDBFF\uDFBB\uDBFF\uDF9C",                         // Août
        "\uDBFF\uDF88\uDBFF\uDFA3\uDBFF\uDF9A\uDBFF\uDF9C\uDBFF\uDFB6",                               // Septembre
        "\uDBFF\uDF88\uDBFF\uDFA3\uDBFF\uDF89\uDBFF\uDFA3\uDBFF\uDF8F\uDBFF\uDF9E",                         // Octobre
        "\uDBFF\uDF9A\uDBFF\uDFA0\uDBFF\uDF8D\uDBFF\uDFA2\uDBFF\uDF91\uDBFF\uDF9C\uDBFF\uDFB2",                   // Novembre
        "\uDBFF\uDF93\uDBFF\uDF9E\uDBFF\uDFAE\uDBFF\uDF90\uDBFF\uDF9E"                                // Décembre
    )

    /** Convertit un nombre en chiffres AYNEHA, dans l'ordre naturel (ex: 12 -> glyphe1 glyphe2). */
    fun toAy(n: Int): String = n.toString().map { DIGITS[it - '0'] }.joinToString("")

    /** Convertit un nombre 0-59 en 2 chiffres AYNEHA inversés (ex: 35 -> glyphes de "53"). */
    fun toAy2Rev(n: Int): String {
        val s = n.toString().padStart(2, '0')
        val rev = "" + s[1] + s[0]
        return toAy(rev.toInt())
    }

    /** Année songhay = année grégorienne + 2001, chiffres inversés puis convertis. */
    fun yearGlyphs(gregorianYear: Int): String {
        val songhayYear = gregorianYear + 2001
        val rev = songhayYear.toString().reversed()
        return rev.map { DIGITS[it - '0'] }.joinToString("")
    }

    /** Heure:minute en glyphes AYNEHA, format identique à l'horloge du site (secondes en tête côté web,
     *  mais pour le widget — mis à jour à la minute — on affiche heures : minutes). */
    fun timeGlyphsHM(cal: Calendar): String {
        val h = toAy2Rev(cal.get(Calendar.HOUR_OF_DAY))
        val m = toAy2Rev(cal.get(Calendar.MINUTE))
        return "$m:$h" // ordre repris du sens de lecture RTL utilisé sur horloge.html (mm:hh affiché)
    }

    /** Calendar.DAY_OF_WEEK va de 1 (dimanche) à 7 (samedi) -> index 0..6 pour nos tableaux. */
    fun dayIndex(cal: Calendar): Int = cal.get(Calendar.DAY_OF_WEEK) - 1

    fun monthIndex(cal: Calendar): Int = cal.get(Calendar.MONTH)
}
