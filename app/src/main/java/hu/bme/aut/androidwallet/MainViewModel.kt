package hu.bme.aut.androidwallet

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    var currency by mutableStateOf("HUF")
        private set

    var transactions = mutableStateListOf(
        Transaction(
            name = "Default Income",
            worth = 1.0,
            currency = currency
        ),
        Transaction(
            name = "Default Expense",
            worth = -1.0,
            currency = currency
        )
    )
        private set
}
