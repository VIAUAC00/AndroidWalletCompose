package hu.bme.aut.androidwallet

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val worth: Double,
    val currency: String
) : Parcelable {
    val isExpense: Boolean get() = worth < 0.0
}
