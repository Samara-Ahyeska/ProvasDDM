package com.example.prova5.data.model

data class ExpenseCategory(
    val name: String,
    val title: String,
    val icon: String,
    val hasBudget: Boolean = true
) {
    companion object {
        val SUPERMERCADO = ExpenseCategory("SUPERMERCADO", "Supermercado / Feira", "🛒", true)
        val COMBUSTIVEL = ExpenseCategory("COMBUSTIVEL", "Combustível / Transporte", "⛽", true)
        val FARMACIA = ExpenseCategory("FARMACIA", "Farmácia / Saúde", "💊", true)

        val defaultList = listOf(SUPERMERCADO, COMBUSTIVEL, FARMACIA)
    }
}

data class PaymentMethod(
    val name: String,
    val label: String
) {
    companion object {
        val PIX = PaymentMethod("PIX", "Pix")
        val DINHEIRO = PaymentMethod("DINHEIRO", "Dinheiro")
        val CREDITO = PaymentMethod("CREDITO", "Cartão de Crédito")
        val DEBITO = PaymentMethod("DEBITO", "Cartão de Débito")

        val defaultList = listOf(PIX, DINHEIRO, CREDITO, DEBITO)
    }
}

data class ExpenseItem(
    val id: String,
    val amount: Double,
    val category: ExpenseCategory,
    val description: String,
    val paymentMethod: PaymentMethod,
    val paymentDetails: String = "",
    val timestamp: Long = System.currentTimeMillis()
)