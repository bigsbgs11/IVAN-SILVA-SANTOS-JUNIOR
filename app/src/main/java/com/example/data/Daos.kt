package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getTransactionsByType(type: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date >= :startTimestamp AND date <= :endTimestamp ORDER BY date DESC")
    fun getTransactionsInRange(startTimestamp: Long, endTimestamp: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE cardId = :cardId ORDER BY date DESC")
    fun getTransactionsByCard(cardId: Long): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)
}

@Dao
interface CategoryBudgetDao {
    @Query("SELECT * FROM category_budgets")
    fun getAllBudgets(): Flow<List<CategoryBudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBudget(budget: CategoryBudgetEntity): Long

    @Delete
    suspend fun deleteBudget(budget: CategoryBudgetEntity)

    @Query("DELETE FROM category_budgets WHERE categoryName = :categoryName")
    suspend fun deleteBudgetByCategory(categoryName: String)
}

@Dao
interface FixedBillDao {
    @Query("SELECT * FROM fixed_bills ORDER BY dueDay ASC")
    fun getAllFixedBills(): Flow<List<FixedBillEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFixedBill(bill: FixedBillEntity): Long

    @Update
    suspend fun updateFixedBill(bill: FixedBillEntity)

    @Delete
    suspend fun deleteFixedBill(bill: FixedBillEntity)

    @Query("DELETE FROM fixed_bills WHERE id = :id")
    suspend fun deleteFixedBillById(id: Long)
}

@Dao
interface CreditCardDao {
    @Query("SELECT * FROM credit_cards")
    fun getAllCreditCards(): Flow<List<CreditCardEntity>>

    @Query("SELECT * FROM credit_cards WHERE id = :id")
    suspend fun getCreditCardById(id: Long): CreditCardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreditCard(card: CreditCardEntity): Long

    @Update
    suspend fun updateCreditCard(card: CreditCardEntity)

    @Delete
    suspend fun deleteCreditCard(card: CreditCardEntity)

    @Query("DELETE FROM credit_cards WHERE id = :id")
    suspend fun deleteCreditCardById(id: Long)
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM financial_goals")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    @Query("DELETE FROM financial_goals WHERE id = :id")
    suspend fun deleteGoalById(id: Long)
}

@Dao
interface CustomCategoryDao {
    @Query("SELECT * FROM custom_categories WHERE type = :type")
    fun getCategoriesByType(type: String): Flow<List<CustomCategoryEntity>>

    @Query("SELECT * FROM custom_categories")
    fun getAllCustomCategories(): Flow<List<CustomCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CustomCategoryEntity): Long

    @Delete
    suspend fun deleteCategory(category: CustomCategoryEntity)
}
