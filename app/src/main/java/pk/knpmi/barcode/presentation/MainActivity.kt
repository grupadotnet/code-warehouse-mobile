package pk.knpmi.barcode.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import dagger.hilt.android.AndroidEntryPoint
import pk.knpmi.barcode.presentation.test_screen.TestScreen
import pk.knpmi.barcode.presentation.ui.theme.ProjectBarcodeTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProjectBarcodeTheme {
                Surface {
                    TestScreen()
                }
            }
        }
    }
}