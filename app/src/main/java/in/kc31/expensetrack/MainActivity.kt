package `in`.kc31.expensetrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.kc31.expensetrack.ui.WhereMyBuckGoesScreen
import `in`.kc31.expensetrack.ui.WhereMyBuckGoesViewModel
import `in`.kc31.expensetrack.ui.theme.ExpenseTrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExpenseTrackTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val viewModel: WhereMyBuckGoesViewModel = viewModel()
                    WhereMyBuckGoesScreen(viewModel = viewModel)
                }
            }
        }
    }
}