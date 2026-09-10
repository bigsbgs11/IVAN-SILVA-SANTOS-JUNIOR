package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val db: AppDatabase,
    val userPreferences: UserPreferences
) {
    val allTransactions: Flow<List<TransactionEntity>> = db.transactionDao().getAllTransactions()
    val allBudgets: Flow<List<CategoryBudgetEntity>> = db.categoryBudgetDao().getAllBudgets()
    val allFixedBills: Flow<List<FixedBillEntity>> = db.fixedBillDao().getAllFixedBills()
    val allCreditCards: Flow<List<CreditCardEntity>> = db.creditCardDao().getAllCreditCards()
    val allGoals: Flow<List<GoalEntity>> = db.goalDao().getAllGoals()
    val allCustomCategories: Flow<List<CustomCategoryEntity>> = db.customCategoryDao().getAllCustomCategories()

    fun getTransactionsInRange(start: Long, end: Long): Flow<List<TransactionEntity>> =
        db.transactionDao().getTransactionsInRange(start, end)

    fun getTransactionsByType(type: String): Flow<List<TransactionEntity>> =
        db.transactionDao().getTransactionsByType(type)

    fun getTransactionsByCard(cardId: Long): Flow<List<TransactionEntity>> =
        db.transactionDao().getTransactionsByCard(cardId)

    suspend fun insertTransaction(transaction: TransactionEntity): Long =
        db.transactionDao().insertTransaction(transaction)

    suspend fun updateTransaction(transaction: TransactionEntity) =
        db.transactionDao().updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: TransactionEntity) =
        db.transactionDao().deleteTransaction(transaction)

    suspend fun deleteTransactionById(id: Long) =
        db.transactionDao().deleteTransactionById(id)

    suspend fun insertOrUpdateBudget(budget: CategoryBudgetEntity) =
        db.categoryBudgetDao().insertOrUpdateBudget(budget)

    suspend fun deleteBudget(budget: CategoryBudgetEntity) =
        db.categoryBudgetDao().deleteBudget(budget)

    suspend fun insertFixedBill(bill: FixedBillEntity) =
        db.fixedBillDao().insertFixedBill(bill)

    suspend fun updateFixedBill(bill: FixedBillEntity) =
        db.fixedBillDao().updateFixedBill(bill)

    suspend fun deleteFixedBill(bill: FixedBillEntity) =
        db.fixedBillDao().deleteFixedBill(bill)

    suspend fun deleteFixedBillById(id: Long) =
        db.fixedBillDao().deleteFixedBillById(id)

    suspend fun insertCreditCard(card: CreditCardEntity) =
        db.creditCardDao().insertCreditCard(card)

    suspend fun updateCreditCard(card: CreditCardEntity) =
        db.creditCardDao().updateCreditCard(card)

    suspend fun deleteCreditCard(card: CreditCardEntity) =
        db.creditCardDao().deleteCreditCard(card)

    suspend fun deleteCreditCardById(id: Long) =
        db.creditCardDao().deleteCreditCardById(id)

    suspend fun insertGoal(goal: GoalEntity) =
        db.goalDao().insertGoal(goal)

    suspend fun updateGoal(goal: GoalEntity) =
        db.goalDao().updateGoal(goal)

    suspend fun deleteGoal(goal: GoalEntity) =
        db.goalDao().deleteGoal(goal)

    suspend fun deleteGoalById(id: Long) =
        db.goalDao().deleteGoalById(id)

    suspend fun insertCustomCategory(category: CustomCategoryEntity) =
        db.customCategoryDao().insertCategory(category)

    suspend fun deleteCustomCategory(category: CustomCategoryEntity) =
        db.customCategoryDao().deleteCategory(category)
}
