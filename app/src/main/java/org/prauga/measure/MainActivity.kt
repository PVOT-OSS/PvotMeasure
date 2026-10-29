package org.prauga.measure

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.prauga.pvot.designsystem.platform.PvotBaseActivity
import com.prauga.pvot.designsystem.theme.PvotAppTheme

class MainActivity : PvotBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPvotContent {
            PvotAppTheme {
                MeasureAppNavBar()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MeasureAppPreview() {
    PvotAppTheme {
        MeasureAppNavBar()
    }
}
