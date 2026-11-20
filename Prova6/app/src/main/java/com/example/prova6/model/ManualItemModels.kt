package com.example.prova6.model

import java.util.Locale

enum class MeasurementCategory(val label: String) {
    WEIGHT("Peso"),
    VOLUME("Volume"),
    QUANTITY("Quantidade");

    fun units(): List<MeasurementUnit> = when (this) {
        WEIGHT -> listOf(
            MeasurementUnit(
                symbol = "g",
                fullName = "Grama",
                hint = "Embalagens menores ou itens fracionados (ex.: 100g de presunto, 500g de café)",
                defaultAmount = 100.0,
                step = 50.0,
                allowDecimal = false
            ),
            MeasurementUnit(
                symbol = "kg",
                fullName = "Quilograma",
                hint = "Pacotes maiores ou itens pesados na balança (ex.: 1kg de feijão, 5kg de arroz)",
                defaultAmount = 1.0,
                step = 0.5,
                allowDecimal = true
            ),
            MeasurementUnit(
                symbol = "mg",
                fullName = "Miligrama",
                hint = "Medidas pequenas / tabelas nutricionais ou dosagens específicas",
                defaultAmount = 500.0,
                step = 100.0,
                allowDecimal = false
            )
        )

        VOLUME -> listOf(
            MeasurementUnit(
                symbol = "ml",
                fullName = "Mililitro",
                hint = "Embalagens menores de líquidos (ex.: 350ml, 500ml)",
                defaultAmount = 350.0,
                step = 50.0,
                allowDecimal = false
            ),
            MeasurementUnit(
                symbol = "L",
                fullName = "Litro",
                hint = "Volumes maiores de líquidos (ex.: caixa de leite 1L, suco 1,5L, amaciante 2L)",
                defaultAmount = 1.0,
                step = 0.5,
                allowDecimal = true
            )
        )

        QUANTITY -> listOf(
            MeasurementUnit(
                symbol = "un",
                fullName = "Unidade",
                hint = "Itens avulsos ou contados individualmente (ex.: 1 abacaxi, 1 barra de sabão)",
                defaultAmount = 1.0,
                step = 1.0,
                allowDecimal = false
            ),
            MeasurementUnit(
                symbol = "dz",
                fullName = "Dúzia",
                hint = "Equivalente a 12 unidades, comum para ovos e frutas (ex.: banana, laranja)",
                defaultAmount = 1.0,
                step = 1.0,
                allowDecimal = false
            ),
            MeasurementUnit(
                symbol = "m",
                fullName = "Metro",
                hint = "Papel alumínio, filme plástico, papel manteiga ou rolos",
                defaultAmount = 1.0,
                step = 1.0,
                allowDecimal = true
            ),
            MeasurementUnit(
                symbol = "cm",
                fullName = "Centímetro",
                hint = "Medidas curtas de corte ou fita/filme",
                defaultAmount = 50.0,
                step = 10.0,
                allowDecimal = false
            )
        )
    }
}

data class MeasurementUnit(
    val symbol: String,
    val fullName: String,
    val hint: String,
    val defaultAmount: Double,
    val step: Double,
    val allowDecimal: Boolean
) {
    fun formatAmount(amount: Double): String {
        val locale = Locale.forLanguageTag("pt-BR")
        return if (!allowDecimal || amount % 1.0 == 0.0) {
            amount.toInt().toString()
        } else {
            String.format(locale, "%.1f", amount)
        }
    }

    fun formatQuantityLabel(amount: Double): String {
        return "${formatAmount(amount)} $symbol"
    }
}

data class MarketAisle(
    val id: String,
    val label: String,
    val sectionHeaderTitle: (Int) -> String
) {
    companion object {
        val all = listOf(
            MarketAisle("hortifruti", "Hortifrúti / Feira") { "HORTIFRÚTI / FEIRA · $it" },
            MarketAisle("padaria", "Padaria & Matinais") { "PADARIA & MATINAIS · $it" },
            MarketAisle("frios", "Frios & Laticínios") { "FRIOS & LATICÍNIOS · $it" },
            MarketAisle("mercearia", "Mercearia Seca") { "MERCEARIA SECA · $it" },
            MarketAisle("bebidas", "Bebidas") { "BEBIDAS · $it" },
            MarketAisle("higiene", "Higiene & Limpeza") { "HIGIENE & LIMPEZA · $it" }
        )
    }
}