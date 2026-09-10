package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("bgs_na_linha_prefs", Context.MODE_PRIVATE)

    private val _monthlyIncome = MutableStateFlow(prefs.getFloat(KEY_MONTHLY_INCOME, 0.0f).toDouble())
    val monthlyIncome: StateFlow<Double> = _monthlyIncome.asStateFlow()

    private val _savingsGoal = MutableStateFlow(prefs.getFloat(KEY_SAVINGS_GOAL, 0.0f).toDouble())
    val savingsGoal: StateFlow<Double> = _savingsGoal.asStateFlow()

    private val _isFirstLaunchDone = MutableStateFlow(prefs.getBoolean(KEY_FIRST_LAUNCH, false))
    val isFirstLaunchDone: StateFlow<Boolean> = _isFirstLaunchDone.asStateFlow()

    private val _isPinEnabled = MutableStateFlow(prefs.getBoolean(KEY_PIN_ENABLED, false))
    val isPinEnabled: StateFlow<Boolean> = _isPinEnabled.asStateFlow()

    private val _pinCode = MutableStateFlow(prefs.getString(KEY_PIN_CODE, "") ?: "")
    val pinCode: StateFlow<String> = _pinCode.asStateFlow()

    private val _userName = MutableStateFlow(prefs.getString(KEY_USER_NAME, "Usuário") ?: "Usuário")
    val userName: StateFlow<String> = _userName.asStateFlow()

    fun setMonthlyIncome(income: Double) {
        prefs.edit().putFloat(KEY_MONTHLY_INCOME, income.toFloat()).apply()
        _monthlyIncome.value = income
    }

    fun setSavingsGoal(goal: Double) {
        prefs.edit().putFloat(KEY_SAVINGS_GOAL, goal.toFloat()).apply()
        _savingsGoal.value = goal
    }

    fun setFirstLaunchDone(done: Boolean) {
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, done).apply()
        _isFirstLaunchDone.value = done
    }

    fun setPin(enabled: Boolean, pin: String) {
        prefs.edit()
            .putBoolean(KEY_PIN_ENABLED, enabled)
            .putString(KEY_PIN_CODE, pin)
            .apply()
        _isPinEnabled.value = enabled
        _pinCode.value = pin
    }

    fun setUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name).apply()
        _userName.value = name
    }

    companion object {
        private const val KEY_MONTHLY_INCOME = "key_monthly_income"
        private const val KEY_SAVINGS_GOAL = "key_savings_goal"
        private const val KEY_FIRST_LAUNCH = "key_first_launch"
        private const val KEY_PIN_ENABLED = "key_pin_enabled"
        private const val KEY_PIN_CODE = "key_pin_code"
        private const val KEY_USER_NAME = "key_user_name"
    }
}
