package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AiCoachScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GoalsScreen
import com.example.ui.screens.HabitsScreen
import com.example.ui.screens.RoutineScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudyScreen
import com.example.ui.viewmodel.LifeTrackUiState
import com.example.ui.viewmodel.LifeTrackViewModel
import com.example.ui.viewmodel.StudyTimerMode
import com.example.util.DateUtils
import kotlinx.coroutines.launch

object LifeTrackRoutes {
    const val DASHBOARD = "dashboard"
    const val ROUTINE = "routine"
    const val HABITS = "habits"
    const val STUDY = "study"
    const val GOALS = "goals"
    const val CALENDAR = "calendar"
    const val ANALYTICS = "analytics"
    const val AI_COACH = "ai_coach"
    const val SETTINGS = "settings"
}

data class NavDestinationItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val ALL_DESTINATIONS = listOf(
    NavDestinationItem(LifeTrackRoutes.DASHBOARD, "Dashboard", Icons.Rounded.Dashboard),
    NavDestinationItem(LifeTrackRoutes.ROUTINE, "Daily Routine", Icons.Rounded.Checklist),
    NavDestinationItem(LifeTrackRoutes.HABITS, "Habit Tracker", Icons.Rounded.TrackChanges),
    NavDestinationItem(LifeTrackRoutes.STUDY, "Study Timer", Icons.Rounded.Timer),
    NavDestinationItem(LifeTrackRoutes.GOALS, "Goals", Icons.Rounded.Flag),
    NavDestinationItem(LifeTrackRoutes.CALENDAR, "Calendar", Icons.Rounded.CalendarMonth),
    NavDestinationItem(LifeTrackRoutes.ANALYTICS, "Analytics", Icons.Rounded.BarChart),
    NavDestinationItem(LifeTrackRoutes.AI_COACH, "AI Assistant", Icons.Rounded.AutoAwesome),
    NavDestinationItem(LifeTrackRoutes.SETTINGS, "Profile & Settings", Icons.Rounded.Settings)
)

private val BOTTOM_DESTINATIONS = listOf(
    NavDestinationItem(LifeTrackRoutes.DASHBOARD, "Home", Icons.Rounded.Dashboard),
    NavDestinationItem(LifeTrackRoutes.ROUTINE, "Routine", Icons.Rounded.Checklist),
    NavDestinationItem(LifeTrackRoutes.STUDY, "Study", Icons.Rounded.Timer),
    NavDestinationItem(LifeTrackRoutes.CALENDAR, "Calendar", Icons.Rounded.CalendarMonth),
    NavDestinationItem(LifeTrackRoutes.AI_COACH, "AI Coach", Icons.Rounded.AutoAwesome)
)

@Composable
fun LifeTrackApp(
    viewModel: LifeTrackViewModel,
    uiState: LifeTrackUiState
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: LifeTrackRoutes.DASHBOARD
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    val navigateTo: (String) -> Unit = { route ->
        if (route != currentRoute) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 840.dp

        if (isWideScreen) {
            PermanentNavigationDrawer(
                drawerContent = {
                    PermanentDrawerSheet(
                        modifier = Modifier.width(268.dp),
                        drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ) {
                        SidebarContent(
                            uiState = uiState,
                            currentRoute = currentRoute,
                            onSelectRoute = navigateTo
                        )
                    }
                }
            ) {
                MainScaffold(
                    navController = navController,
                    viewModel = viewModel,
                    uiState = uiState,
                    currentRoute = currentRoute,
                    showMenuButton = false,
                    showBottomBar = false,
                    snackbarHostState = snackbarHostState,
                    onOpenDrawer = {},
                    onNavigate = navigateTo
                )
            }
        } else {
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            val scope = rememberCoroutineScope()

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        modifier = Modifier.width(292.dp),
                        drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ) {
                        SidebarContent(
                            uiState = uiState,
                            currentRoute = currentRoute,
                            onSelectRoute = { route ->
                                scope.launch { drawerState.close() }
                                navigateTo(route)
                            }
                        )
                    }
                }
            ) {
                MainScaffold(
                    navController = navController,
                    viewModel = viewModel,
                    uiState = uiState,
                    currentRoute = currentRoute,
                    showMenuButton = true,
                    showBottomBar = true,
                    snackbarHostState = snackbarHostState,
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onNavigate = navigateTo
                )
            }
        }
    }
}

@Composable
private fun SidebarContent(
    uiState: LifeTrackUiState,
    currentRoute: String,
    onSelectRoute: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 20.dp)
    ) {
        // Brand Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "LT",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "LifeTrack",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Smart Routine OS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
        Spacer(modifier = Modifier.height(12.dp))

        ALL_DESTINATIONS.forEach { dest ->
            val selected = currentRoute == dest.route
            NavigationDrawerItem(
                label = {
                    Text(
                        text = dest.label,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                selected = selected,
                onClick = { onSelectRoute(dest.route) },
                icon = {
                    Icon(
                        imageVector = dest.icon,
                        contentDescription = dest.label
                    )
                },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .testTag("drawer_${dest.route}")
            )
        }

        Spacer(modifier = Modifier.height(18.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
        Spacer(modifier = Modifier.height(14.dp))

        // Sidebar User Summary Footer
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            onClick = { onSelectRoute(LifeTrackRoutes.SETTINGS) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val initials = uiState.settings.userName
                    .split(" ")
                    .filter { it.isNotBlank() }
                    .take(2)
                    .joinToString("") { it.first().uppercase() }
                    .ifEmpty { "LT" }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = uiState.settings.userName,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = uiState.settings.userRole,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScaffold(
    navController: NavHostController,
    viewModel: LifeTrackViewModel,
    uiState: LifeTrackUiState,
    currentRoute: String,
    showMenuButton: Boolean,
    showBottomBar: Boolean,
    snackbarHostState: SnackbarHostState,
    onOpenDrawer: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val title = ALL_DESTINATIONS.firstOrNull { it.route == currentRoute }?.label ?: "LifeTrack"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                navigationIcon = {
                    if (showMenuButton) {
                        IconButton(
                            onClick = onOpenDrawer,
                            modifier = Modifier.testTag("open_sidebar_button")
                        ) {
                            Icon(Icons.Rounded.Menu, contentDescription = "Open navigation sidebar")
                        }
                    }
                },
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    if (uiState.timer.isRunning) {
                        val secs = if (uiState.timer.mode == StudyTimerMode.STOPWATCH) {
                            uiState.timer.elapsedSeconds
                        } else {
                            uiState.timer.remainingSeconds
                        }
                        Surface(
                            onClick = { onNavigate(LifeTrackRoutes.STUDY) },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Timer,
                                    contentDescription = "Active focus timer",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = DateUtils.formatDurationSeconds(secs),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                    IconButton(
                        onClick = { onNavigate(LifeTrackRoutes.SETTINGS) },
                        modifier = Modifier.testTag("topbar_settings_button")
                    ) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Profile and settings")
                    }
                }
            )
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    BOTTOM_DESTINATIONS.forEach { dest ->
                        val selected = currentRoute == dest.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = { onNavigate(dest.route) },
                            icon = {
                                Icon(
                                    imageVector = dest.icon,
                                    contentDescription = dest.label
                                )
                            },
                            label = { Text(dest.label, maxLines = 1) },
                            modifier = Modifier.testTag("bottom_nav_${dest.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = LifeTrackRoutes.DASHBOARD,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(LifeTrackRoutes.DASHBOARD) {
                DashboardScreen(
                    uiState = uiState,
                    onToggleRoutine = viewModel::toggleRoutineCompletion,
                    onToggleDarkMode = viewModel::toggleDarkMode,
                    onToggleTimer = viewModel::toggleTimerRunning,
                    onNavigateToRoute = onNavigate
                )
            }
            composable(LifeTrackRoutes.ROUTINE) {
                RoutineScreen(
                    uiState = uiState,
                    onSelectDate = viewModel::selectRoutineDate,
                    onToggleRoutine = viewModel::toggleRoutineCompletion,
                    onSaveRoutine = viewModel::saveRoutine,
                    onDeleteRoutine = viewModel::deleteRoutine
                )
            }
            composable(LifeTrackRoutes.HABITS) {
                HabitsScreen(
                    uiState = uiState,
                    onToggleHabitDate = viewModel::toggleHabitDate,
                    onSaveHabit = viewModel::saveHabit,
                    onDeleteHabit = viewModel::deleteHabit
                )
            }
            composable(LifeTrackRoutes.STUDY) {
                StudyScreen(
                    uiState = uiState,
                    onSelectMode = viewModel::selectTimerMode,
                    onSetSubject = viewModel::setTimerSubject,
                    onToggleTimer = viewModel::toggleTimerRunning,
                    onResetTimer = viewModel::resetTimer,
                    onCompleteAndLogNow = viewModel::completeAndLogTimerNow,
                    onLogManualSession = viewModel::logManualStudySession,
                    onDeleteSession = viewModel::deleteStudySession
                )
            }
            composable(LifeTrackRoutes.GOALS) {
                GoalsScreen(
                    uiState = uiState,
                    onSaveGoal = viewModel::saveGoal,
                    onIncrementGoal = viewModel::incrementGoal,
                    onDeleteGoal = viewModel::deleteGoal
                )
            }
            composable(LifeTrackRoutes.CALENDAR) {
                CalendarScreen(
                    uiState = uiState,
                    onSelectDate = viewModel::selectCalendarDate,
                    onToggleRoutine = viewModel::toggleRoutineCompletion,
                    onOpenRoutinePlannerForDate = { dateKey ->
                        viewModel.selectRoutineDate(dateKey)
                        onNavigate(LifeTrackRoutes.ROUTINE)
                    }
                )
            }
            composable(LifeTrackRoutes.ANALYTICS) {
                AnalyticsScreen(uiState = uiState)
            }
            composable(LifeTrackRoutes.AI_COACH) {
                AiCoachScreen(
                    uiState = uiState,
                    onAskPrompt = viewModel::askAiCoach,
                    onClearChat = viewModel::clearAiConversation
                )
            }
            composable(LifeTrackRoutes.SETTINGS) {
                SettingsScreen(
                    uiState = uiState,
                    onToggleDarkMode = viewModel::toggleDarkMode,
                    onSetNotifications = viewModel::setNotificationsEnabled,
                    onUpdateProfile = viewModel::updateProfile,
                    onUpdateStudyTargets = viewModel::updateStudyTargets,
                    onResetWorkspace = viewModel::resetStarterWorkspace,
                    onClearWorkspace = viewModel::clearAllWorkspaceData
                )
            }
        }
    }
}
