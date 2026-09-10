package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.CategoryBudgetEntity
import com.example.data.CreditCardEntity
import com.example.data.CustomCategoryEntity
import com.example.data.FinancialConstants
import com.example.data.FixedBillEntity
import com.example.data.GoalEntity
import com.example.data.TransactionEntity
import com.example.data.UserPreferences
import com.example.util.Formatters
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class CategoryBudgetStatus(
    val category: String,
    val limit: Double,
    val spent: Double,
    val remaining: Double,
    val percentage: Double, // 0..100+
    val isNearLimit: Boolean, // >= 80% and <= 100%
    val isExceeded: Boolean // > 100%
)

data class FinancialSummary(
    val currentBalance: Double,
    val monthIncome: Double,
    val monthExpense: Double,
    val availableToSpend: Double,
    val savingsGoal: Double,
    val totalSaved: Double,
    val savingsProgress: Double
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val userPrefs = UserPreferences(application)
    val repository = AppRepository(db, userPrefs)

    init {
        NotificationHelper.initChannel(application)
    }

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBudgets: StateFlow<List<CategoryBudgetEntity>> = repository.allBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFixedBills: StateFlow<List<FixedBillEntity>> = repository.allFixedBills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCreditCards: StateFlow<List<CreditCardEntity>> = repository.allCreditCards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGoals: StateFlow<List<GoalEntity>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCustomCategories: StateFlow<List<CustomCategoryEntity>> = repository.allCustomCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyIncome: StateFlow<Double> = userPrefs.monthlyIncome
    val savingsGoal: StateFlow<Double> = userPrefs.savingsGoal
    val isFirstLaunchDone: StateFlow<Boolean> = userPrefs.isFirstLaunchDone
    val isPinEnabled: StateFlow<Boolean> = userPrefs.isPinEnabled
    val pinCode: StateFlow<String> = userPrefs.pinCode

    private val _isAppUnlocked = MutableStateFlow(!userPrefs.isPinEnabled.value)
    val isAppUnlocked: StateFlow<Boolean> = _isAppUnlocked.asStateFlow()

    // Financial Summary Flow
    val financialSummary: StateFlow<FinancialSummary> = combine(
        allTransactions,
        monthlyIncome,
        savingsGoal,
        allGoals
    ) { txList, income, goal, goalsList ->
        var totalInc = 0.0
        var totalExp = 0.0
        var mInc = 0.0
        var mExp = 0.0

        for (tx in txList) {
            if (tx.type == "INCOME") {
                totalInc += tx.amount
                if (Formatters.isThisMonth(tx.date)) {
                    mInc += tx.amount
                }
            } else {
                totalExp += tx.amount
                if (Formatters.isThisMonth(tx.date)) {
                    mExp += tx.amount
                }
            }
        }

        val totalSavedInGoals = goalsList.sumOf { it.savedAmount }
        val balance = totalInc - totalExp
        // Valor disponível = Renda mensal - Despesas - Valor reservado para metas
        val effectiveIncome = if (income > 0) income else mInc
        val available = (effectiveIncome - mExp - totalSavedInGoals).coerceAtLeast(0.0)

        val progress = if (goal > 0) {
            ((totalSavedInGoals / goal) * 100.0).coerceIn(0.0, 100.0)
        } else 0.0

        FinancialSummary(
            currentBalance = balance,
            monthIncome = mInc,
            monthExpense = mExp,
            availableToSpend = available,
            savingsGoal = goal,
            totalSaved = totalSavedInGoals,
            savingsProgress = progress
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        FinancialSummary(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
    )

    // Category Budgets Status Flow
    val categoryBudgetsStatus: StateFlow<List<CategoryBudgetStatus>> = combine(
        allBudgets,
        allTransactions
    ) { budgets, transactions ->
        budgets.map { budget ->
            val spent = transactions
                .filter { it.type == "EXPENSE" && it.category.equals(budget.categoryName, ignoreCase = true) && Formatters.isThisMonth(it.date) }
                .sumOf { it.amount }
            val remaining = budget.monthlyLimit - spent
            val percentage = if (budget.monthlyLimit > 0) (spent / budget.monthlyLimit) * 100.0 else 0.0
            CategoryBudgetStatus(
                category = budget.categoryName,
                limit = budget.monthlyLimit,
                spent = spent,
                remaining = remaining,
                percentage = percentage,
                isNearLimit = percentage in 80.0..100.0,
                isExceeded = percentage > 100.0
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun unlockApp(enteredPin: String): Boolean {
        return if (enteredPin == pinCode.value) {
            _isAppUnlocked.value = true
            true
        } else {
            false
        }
    }

    fun lockApp() {
        if (isPinEnabled.value) {
            _isAppUnlocked.value = false
        }
    }

    fun setPin(enabled: Boolean, pin: String) {
        userPrefs.setPin(enabled, pin)
        _isAppUnlocked.value = true
    }

    fun setMonthlyIncome(income: Double) {
        userPrefs.setMonthlyIncome(income)
    }

    fun setSavingsGoal(goal: Double) {
        userPrefs.setSavingsGoal(goal)
    }

    fun completeFirstLaunch(income: Double, goal: Double) {
        userPrefs.setMonthlyIncome(income)
        userPrefs.setSavingsGoal(goal)
        userPrefs.setFirstLaunchDone(true)

        // Add default budgets for common categories if none exist
        viewModelScope.launch {
            if (income > 0) {
                repository.insertOrUpdateBudget(CategoryBudgetEntity(categoryName = "Alimentação", monthlyLimit = income * 0.25))
                repository.insertOrUpdateBudget(CategoryBudgetEntity(categoryName = "Moradia", monthlyLimit = income * 0.30))
                repository.insertOrUpdateBudget(CategoryBudgetEntity(categoryName = "Transporte", monthlyLimit = income * 0.15))
                repository.insertOrUpdateBudget(CategoryBudgetEntity(categoryName = "Lazer", monthlyLimit = income * 0.10))
            }
        }
    }

    fun addTransaction(
        title: String,
        amount: Double,
        type: String,
        category: String,
        date: Long,
        paymentMethod: String,
        note: String = "",
        isFixed: Boolean = false,
        cardId: Long? = null
    ) {
        viewModelScope.launch {
            repository.insertTransaction(
                TransactionEntity(
                    title = title,
                    amount = amount,
                    type = type,
                    category = category,
                    date = date,
                    paymentMethod = paymentMethod,
                    note = note,
                    isFixed = isFixed,
                    cardId = cardId
                )
            )

            // Check budget notifications
            if (type == "EXPENSE") {
                checkBudgetNotification(category, amount)
            }
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransactionById(id)
        }
    }

    fun saveBudget(category: String, limit: Double) {
        viewModelScope.launch {
            repository.insertOrUpdateBudget(CategoryBudgetEntity(categoryName = category, monthlyLimit = limit))
        }
    }

    fun deleteBudget(budget: CategoryBudgetEntity) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
        }
    }

    fun addFixedBill(name: String, amount: Double, dueDay: Int, isMonthly: Boolean, category: String) {
        viewModelScope.launch {
            repository.insertFixedBill(
                FixedBillEntity(
                    name = name,
                    amount = amount,
                    dueDay = dueDay,
                    isMonthlyRecurrent = isMonthly,
                    category = category
                )
            )
        }
    }

    fun toggleFixedBillPaid(bill: FixedBillEntity) {
        viewModelScope.launch {
            repository.updateFixedBill(bill.copy(isPaid = !bill.isPaid))
        }
    }

    fun deleteFixedBill(id: Long) {
        viewModelScope.launch {
            repository.deleteFixedBillById(id)
        }
    }

    fun addCreditCard(name: String, limit: Double, closingDay: Int, dueDay: Int, colorHex: String) {
        viewModelScope.launch {
            repository.insertCreditCard(
                CreditCardEntity(
                    name = name,
                    creditLimit = limit,
                    closingDay = closingDay,
                    dueDay = dueDay,
                    colorHex = colorHex
                )
            )
        }
    }

    fun deleteCreditCard(id: Long) {
        viewModelScope.launch {
            repository.deleteCreditCardById(id)
        }
    }

    fun addGoal(name: String, targetAmount: Double, initialSaved: Double, deadline: String) {
        viewModelScope.launch {
            repository.insertGoal(
                GoalEntity(
                    name = name,
                    targetAmount = targetAmount,
                    savedAmount = initialSaved,
                    deadline = deadline
                )
            )
        }
    }

    fun addToGoalSaved(goal: GoalEntity, addedAmount: Double) {
        viewModelScope.launch {
            val updated = goal.copy(savedAmount = (goal.savedAmount + addedAmount).coerceAtMost(goal.targetAmount))
            repository.updateGoal(updated)
        }
    }

    fun deleteGoal(id: Long) {
        viewModelScope.launch {
            repository.deleteGoalById(id)
        }
    }

    fun addCustomCategory(name: String, type: String) {
        viewModelScope.launch {
            repository.insertCustomCategory(CustomCategoryEntity(name = name, type = type))
        }
    }

    private fun checkBudgetNotification(category: String, justSpent: Double) {
        val budget = allBudgets.value.find { it.categoryName.equals(category, ignoreCase = true) } ?: return
        val currentSpent = allTransactions.value
            .filter { it.type == "EXPENSE" && it.category.equals(category, ignoreCase = true) && Formatters.isThisMonth(it.date) }
            .sumOf { it.amount } + justSpent

        val percent = (currentSpent / budget.monthlyLimit) * 100.0
        if (percent > 100.0) {
            NotificationHelper.sendNotification(
                getApplication(),
                1001,
                "Limite Ultrapassado!",
                "Você ultrapassou o limite planejado para $category."
            )
        } else if (percent >= 80.0) {
            NotificationHelper.sendNotification(
                getApplication(),
                1002,
                "Atenção ao Orçamento",
                "Você está perto do limite de gastos para $category (${percent.toInt()}%)."
            )
        }
    }
}
