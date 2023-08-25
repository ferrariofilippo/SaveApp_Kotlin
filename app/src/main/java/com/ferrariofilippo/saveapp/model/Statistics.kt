package com.ferrariofilippo.saveapp.model

data class Statistics(
    var totalIncome: Double,
    var totalExpenses: Double,
    var yearlySubscriptionExpenses: Double,
    var subscriptionsPaidYTD: Double,
    var expensesPerType: MutableList<Double>,
    var expensesPerMonth: MutableList<Double>,
    var expensesPerYear: Map<Int, Double>,
    var incomePerYear: Map<Int, Double>
);
