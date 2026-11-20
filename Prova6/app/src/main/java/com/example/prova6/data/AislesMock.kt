package com.example.prova6.data

import com.example.prova6.model.AisleGroup
import com.example.prova6.model.AisleProductTemplate
import com.example.prova6.model.MarketAisleCard

object AislesMock {

    fun initialAisles(): List<MarketAisleCard> = listOf(
        // Alimentos e bebidas
        MarketAisleCard(
            id = "hortifruti",
            name = "Hortifrúti",
            description = "Frutas, legumes e verduras frescos.",
            group = AisleGroup.FOOD_AND_DRINKS
        ),
        MarketAisleCard(
            id = "padaria_confeitaria",
            name = "Padaria e confeitaria",
            description = "Pães, bolos, biscoitos e itens de fabricação própria ou frescos.",
            group = AisleGroup.FOOD_AND_DRINKS
        ),
        MarketAisleCard(
            id = "acougue_peixaria",
            name = "Açougue e peixaria",
            description = "Carnes bovinas, suínas, aves, peixes e frutos do mar.",
            group = AisleGroup.FOOD_AND_DRINKS
        ),
        MarketAisleCard(
            id = "frios_laticinios",
            name = "Frios e laticínios",
            description = "Queijos, presuntos, mortadelas, embutidos, leite, manteiga, requeijão e iogurtes.",
            group = AisleGroup.FOOD_AND_DRINKS
        ),
        MarketAisleCard(
            id = "congelados",
            name = "Congelados",
            description = "Pratos prontos, massas, polpas, sorvetes e carnes congeladas.",
            group = AisleGroup.FOOD_AND_DRINKS
        ),
        MarketAisleCard(
            id = "mercearia",
            name = "Mercearia (seca e líquida)",
            description = "Arroz, feijão, massas, farinhas, óleos, molhos, enlatados, temperos, cafés, doces e biscoitos.",
            group = AisleGroup.FOOD_AND_DRINKS
        ),
        MarketAisleCard(
            id = "bebidas_adega",
            name = "Bebidas e adega",
            description = "Água, sucos, refrigerantes, energéticos, cervejas, vinhos e destilados.",
            group = AisleGroup.FOOD_AND_DRINKS
        ),
        MarketAisleCard(
            id = "rotisserie",
            name = "Rotisserie",
            description = "Refeições prontas, assados e pratos preparados na loja.",
            group = AisleGroup.FOOD_AND_DRINKS
        ),
        MarketAisleCard(
            id = "saudaveis",
            name = "Saudáveis / naturais / funcionais",
            description = "Itens integrais, sem glúten, sem lactose, orgânicos ou suplementos.",
            group = AisleGroup.FOOD_AND_DRINKS
        ),

        // Não alimentos
        MarketAisleCard(
            id = "higiene_perfumaria",
            name = "Higiene e perfumaria",
            description = "Sabonetes, shampoos, cremes dentais, desodorantes, fraldas e itens de cuidados pessoais.",
            group = AisleGroup.NON_FOOD
        ),
        MarketAisleCard(
            id = "limpeza",
            name = "Limpeza",
            description = "Detergentes, sabão em pó, amaciantes, desinfetantes, panos, vassouras e descartáveis.",
            group = AisleGroup.NON_FOOD
        ),
        MarketAisleCard(
            id = "bazar_utilidades",
            name = "Bazar e utilidades domésticas",
            description = "Panelas, potes, talheres, copos, itens de organização, jardinagem ou papelaria.",
            group = AisleGroup.NON_FOOD
        ),
        MarketAisleCard(
            id = "pet_shop",
            name = "Pet shop",
            description = "Rações, petiscos, areia sanitária e acessórios para animais.",
            group = AisleGroup.NON_FOOD
        )
    )

    fun initialProducts(): List<AisleProductTemplate> = listOf(
        AisleProductTemplate("p1", "hortifruti", "Tomate", "2 un", 7.49),
        AisleProductTemplate("p2", "hortifruti", "Banana nanica", "1 dz", 5.90),
        AisleProductTemplate("p3", "padaria_confeitaria", "Pão de forma", "1 un", 8.50),
        AisleProductTemplate("p4", "acougue_peixaria", "Peito de frango", "1 kg", 19.90),
        AisleProductTemplate("p5", "frios_laticinios", "Leite integral", "6 un", 5.29),
        AisleProductTemplate("p6", "frios_laticínios", "Queijo muçarela", "200 g", 9.80),
        AisleProductTemplate("p7", "congelados", "Polpa de fruta", "400 g", 8.90),
        AisleProductTemplate("p8", "mercearia", "Arroz 5 kg", "1 un", 29.90),
        AisleProductTemplate("p9", "mercearia", "Café torrado e moído", "500 g", 18.50),
        AisleProductTemplate("p10", "bebidas_adega", "Suco de uva integral", "1,5 L", 14.90),
        AisleProductTemplate("p11", "rotisserie", "Frango assado inteiro", "1 un", 36.00),
        AisleProductTemplate("p12", "saudaveis", "Pão integral sem glúten", "1 un", 16.40),
        AisleProductTemplate("p13", "higiene_perfumaria", "Creme dental", "2 un", 6.50),
        AisleProductTemplate("p14", "limpeza", "Detergente líquido", "500 ml", 2.89),
        AisleProductTemplate("p15", "bazar_utilidades", "Pote organizador hermético", "2 un", 22.00),
        AisleProductTemplate("p16", "pet_shop", "Ração seca", "3 kg", 44.90)
    )
}