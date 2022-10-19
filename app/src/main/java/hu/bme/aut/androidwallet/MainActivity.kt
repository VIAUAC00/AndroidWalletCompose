package hu.bme.aut.androidwallet

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.MoneyOff
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import hu.bme.aut.androidwallet.ui.theme.AndroidWalletTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidWalletTheme {
                MainScreen()
            }
        }
    }
}

val MainPadding = 8.dp

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = {
            SnackbarHost(
                // Paddings are useful, so that the snackbar
                // is always visible, ie. when the keyboard shown.
                // TODO: insert padding from README.md
                modifier = Modifier,
                hostState = snackbarHostState
            )
        },
        topBar = {
            TopAppBar(
                title = { WalletTitle() },
                actions = { AppBarActionMenu(viewModel) }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // You should always use the paddings, Scaffold
                // gives you in some way or another
                .padding(
                    bottom = it.calculateBottomPadding(),
                    top = it.calculateTopPadding()
                )
        ) {
            TransactionAdder(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MainPadding),
                snackbarHostState = snackbarHostState
            )
            TransactionList()
        }
    }
}

@Composable
fun WalletTitle() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(MainPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = stringResource(R.string.app_name))
        Icon(
            imageVector = Icons.Rounded.Wallet,
            contentDescription = stringResource(R.string.wallet_icon)
        )
    }
}

@Composable
fun AppBarActionMenu(
    viewModel: MainViewModel = viewModel()
) {
    var isExpended by remember { mutableStateOf(false) }
    IconButton(onClick = { isExpended = !isExpended }) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = stringResource(R.string.more_options)
        )
    }
    DropdownMenu(
        expanded = isExpended,
        onDismissRequest = { isExpended = !isExpended }
    ) {
        DropdownMenuItem(
            text = {
                Text(stringResource(R.string.delete_all))
            },
            onClick = {
                viewModel.transactions.clear()
                isExpended = !isExpended
            }
        )
    }
}

@Composable
fun TransactionAdder(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
    context: Context = LocalContext.current,
    viewModel: MainViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MainPadding)
    ) {
        var name by remember { mutableStateOf("") }
        var worth by remember { mutableStateOf("") }
        var wasNameValidated by remember { mutableStateOf(false) }
        var wasWorthValidated by remember { mutableStateOf(false) }
        val isNameWrong = name.isBlank() && wasNameValidated
        val isWorthWrong = worth.isBlank() && wasWorthValidated
        Row(
            horizontalArrangement = Arrangement.spacedBy(MainPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WalletTextField(
                label = { Text(text = stringResource(R.string.transaction_name)) },
                modifier = Modifier.weight(2f),
                value = name,
                onValueChange = { name = it },
                isError = isNameWrong,
                keyboardType = KeyboardType.Text,
                onAddTransaction = {
                    viewModel.transactions.addTransactionWithValidation(
                        context = context,
                        coroutineScope = coroutineScope,
                        name = name,
                        worth = worth,
                        currency = viewModel.currency,
                        onNameValidated = { wasNameValidated = true },
                        onWorthValidated = { wasWorthValidated = true },
                        snackbarHostState = snackbarHostState
                    )
                }
            )
            // TODO: Put here the WalletTextField to input the transaction's worth.
            //  The above implementation of WalletTextField is a good example.
            Text(
                modifier = Modifier.offset(y = 4.dp),
                text = viewModel.currency,
                style = MaterialTheme.typography.labelLarge
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(
                space = MainPadding,
                alignment = Alignment.End
            ),
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.summary) +
                    ": " + viewModel.transactions.sumOf { it.worth } +
                    " " + viewModel.currency,
                style = MaterialTheme.typography.labelLarge
            )
            Button(
                onClick = {
                    viewModel.transactions.addTransactionWithValidation(
                        context = context,
                        coroutineScope = coroutineScope,
                        name = name,
                        worth = worth,
                        currency = viewModel.currency,
                        onNameValidated = { wasNameValidated = true },
                        onWorthValidated = { wasWorthValidated = true },
                        snackbarHostState = snackbarHostState
                    )
                }
            ) {
                Text(text = stringResource(R.string.save))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = TextFieldDefaults.outlinedShape,
    colors: TextFieldColors = TextFieldDefaults.outlinedTextFieldColors(),
    focusManager: FocusManager = LocalFocusManager.current,
    onAddTransaction: () -> Unit = {}
) {
    OutlinedTextField(
        label = label,
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        isError = isError,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        ),
        keyboardActions = KeyboardActions(
            onGo = {
                focusManager.clearFocus()
                onAddTransaction()
            },
            onDone = {
                focusManager.clearFocus()
                onAddTransaction()
            }
        ),
        enabled = enabled,
        interactionSource = interactionSource,
        trailingIcon = trailingIcon,
        colors = colors,
        leadingIcon = leadingIcon,
        maxLines = maxLines,
        placeholder = placeholder,
        textStyle = textStyle,
        readOnly = readOnly,
        shape = shape,
        visualTransformation = visualTransformation
    )
}

fun String.canBeDouble() = isNotBlank() && toDoubleOrNull() != null

fun validateTransaction(
    name: String,
    worth: String,
    customValidationRule: () -> Boolean = { true },
    onValidTransaction: () -> Unit = {},
    onInvalidTransaction: () -> Unit = {}
) {
    if (name.isNotBlank() &&
        worth.canBeDouble() &&
        customValidationRule()
    ) {
        onValidTransaction()
    } else {
        onInvalidTransaction()
    }
}

/**
 * Adds a [Transaction] if the [name] and [worth] is valid.
 * If not, a snackbar pops up with an error message.
 */
fun SnapshotStateList<Transaction>.addTransactionWithValidation(
    context: Context,
    coroutineScope: CoroutineScope,
    name: String,
    worth: String,
    currency: String,
    onNameValidated: () -> Unit = {},
    onWorthValidated: () -> Unit = {},
    snackbarHostState: SnackbarHostState
) {
    onNameValidated()
    onWorthValidated()
    validateTransaction(
        name = name,
        worth = worth,
        onValidTransaction = {
            this.addTransaction(
                name = name,
                worth = worth,
                currency = currency
            )
        },
        onInvalidTransaction = {
            coroutineScope.launch {
                if (snackbarHostState.currentSnackbarData == null) {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.please_enter_data_on_both_fields),
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    )
}

fun SnapshotStateList<Transaction>.addTransaction(
    name: String,
    worth: String,
    currency: String
) {
    add(
        Transaction(
            name = name,
            worth = worth.toDoubleOrNull() ?: 0.0,
            currency = currency
        )
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionList(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel()
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = MainPadding)
            .clip(
                RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp
                )
            ),
        verticalArrangement = Arrangement.spacedBy(MainPadding),
        contentPadding = PaddingValues(vertical = MainPadding)
    ) {
        val items = viewModel.transactions
        items(items) {
            // Animated Visibility does not work for showing an item
            // and disappearing of an item.
            // Though it is coming in a future update!
            AnimatedVisibility(
                visible = items.contains(it),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(modifier = Modifier.animateItemPlacement()) {
                    TransactionCard(
                        item = it,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionCard(
    modifier: Modifier = Modifier,
    item: Transaction
) {
    ElevatedCard(
        modifier = modifier
    ) {
        val (imageVector, color) = if (item.isExpense) {
            Icons.Rounded.MoneyOff to MaterialTheme.colorScheme.error
        } else {
            Icons.Rounded.AttachMoney to MaterialTheme.colorScheme.primary
        }
        // TODO: create the content of the Card
    }
}
