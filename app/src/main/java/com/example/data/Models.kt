package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // "EXPENSE" or "INCOME"
    val category: String,
    val date: Long, // Epoch timestamp in ms
    val paymentMethod: String = "Pix", // Dinheiro, Pix, Débito, Crédito, Boleto, Transferência, Outros
    val note: String = "",
    val isFixed: Boolean = false, // Despesa fixa ou variável
    val cardId: Long? = null
)

@Entity(tableName = "category_budgets")
data class CategoryBudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryName: String,
    val monthlyLimit: Double
)

@Entity(tableName = "fixed_bills")
data class FixedBillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: Double,
    val dueDay: Int, // 1..31
    val isMonthlyRecurrent: Boolean = true,
    val category: String = "Contas",
    val isPaid: Boolean = false,
    val lastPaidMonth: String = "" // "YYYY-MM"
)

@Entity(tableName = "credit_cards")
data class CreditCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val creditLimit: Double,
    val closingDay: Int, // 1..31
    val dueDay: Int, // 1..31
    val colorHex: String = "#1D6FBE"
)

@Entity(tableName = "financial_goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetAmount: Double,
    val savedAmount: Double = 0.0,
    val deadline: String = "" // e.g. "Dezembro 2026"
)

@Entity(tableName = "custom_categories")
data class CustomCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // "EXPENSE" or "INCOME"
    val iconName: String = "default"
)
