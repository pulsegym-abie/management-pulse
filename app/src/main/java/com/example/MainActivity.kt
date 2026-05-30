package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.data.AppDatabase
import com.example.data.PreferenceManager
import com.example.data.TaskRepository
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.TaskViewModel
import com.example.viewmodel.TaskViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()

        // Core Dependency Injection Injection (Constructor Injection)
        val database = AppDatabase.getDatabase(applicationContext)
        val preferenceManager = PreferenceManager(applicationContext)
        val repository = TaskRepository(database.taskDao())
        
        val viewModel: TaskViewModel by viewModels {
            TaskViewModelFactory(repository, preferenceManager)
        }

        setContent {
            MyApplicationTheme {
                val currentDivision by viewModel.currentDivision.collectAsState()

                if (currentDivision.isEmpty()) {
                    // Lock app behind Login Screen
                    LoginScreen(viewModel = viewModel)
                } else {
                    // Render Main App Workspace Scaffold
                    MainWorkspace(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainWorkspace(viewModel: TaskViewModel) {
    var selectedScreenIndex by remember { mutableIntStateOf(0) }

    val navigationItems = listOf(
        NavigationItemInfo("Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "dashboard_tab"),
        NavigationItemInfo("Kalender", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth, "calendar_tab"),
        NavigationItemInfo("Laporan", Icons.Filled.InsertChart, Icons.Outlined.InsertChart, "reports_tab"),
        NavigationItemInfo("Integrasi", Icons.Filled.CloudSync, Icons.Outlined.CloudSync, "integration_tab")
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.navigationBars),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("app_navigation_bar")
            ) {
                navigationItems.forEachIndexed { index, item ->
                    val isSelected = selectedScreenIndex == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedScreenIndex = index },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.activeIcon else item.inactiveIcon,
                                contentDescription = item.title
                            )
                        },
                        label = {
                            Text(text = item.title)
                        },
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedScreenIndex) {
                0 -> DashboardScreen(viewModel = viewModel)
                1 -> CalendarScreen(viewModel = viewModel)
                2 -> ReportsScreen(viewModel = viewModel)
                3 -> IntegrationScreen(viewModel = viewModel)
            }
        }
    }
}

data class NavigationItemInfo(
    val title: String,
    val activeIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val inactiveIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
)
