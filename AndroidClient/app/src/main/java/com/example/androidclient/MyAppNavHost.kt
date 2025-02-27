package com.example.androidclient

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.androidclient.auth.LoginScreen
import com.example.androidclient.core.data.UserPreferences
import com.example.androidclient.core.data.remote.Api
import com.example.androidclient.core.ui.UserPreferencesViewModel
import com.example.androidclient.todo.ui.task.PhotoScreen
import com.example.androidclient.todo.ui.task.TaskScreen
import com.example.androidclient.todo.ui.tasks.TasksScreen

val tasksRoute = "tasks"
val authRoute = "auth"

@Composable
fun MyAppNavHost() {
    val navController = rememberNavController()
    val onCloseTask = {
        Log.d("MyAppNavHost", "navigate back to list")
        navController.popBackStack()
    }
    val userPreferencesViewModel =
        viewModel<UserPreferencesViewModel>(factory = UserPreferencesViewModel.Factory)
    val userPreferencesUiState by userPreferencesViewModel.uiState.collectAsStateWithLifecycle(
        initialValue = UserPreferences()
    )
    val myAppViewModel = viewModel<MyAppViewModel>(factory = MyAppViewModel.Factory)
    NavHost(
        navController = navController,
        startDestination = authRoute
    ) {
        composable(tasksRoute) {
            TasksScreen(
                onTaskClick = {
                    Log.d("MyAppNavHost", "navigate to task $it")
                    navController.navigate("$tasksRoute/$it")
                },
                onAddTask = {
                    Log.d("MyAppNavHost", "navigate to new task")
                    navController.navigate("$tasksRoute-new")
                },
                onLogout = {
                    Log.d("MyAppNavHost", "logout")
                    myAppViewModel.logout()
                    Api.tokenInterceptor.token = null
                    navController.navigate(authRoute) {
                        popUpTo(0)
                    }
                })
        }
        composable(
            route = "$tasksRoute/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) {
            TaskScreen(
                taskId = it.arguments?.getInt("id"),
                onClose = { onCloseTask() }
            )
        }
        composable(route = "$tasksRoute-new") {
            TaskScreen(
                taskId = null,
                onClose = { onCloseTask() }
            )
        }
        composable(route = authRoute) {
            LoginScreen(
                onClose = {
                    Log.d("MyAppNavHost", "navigate to list")
                    navController.navigate(tasksRoute)
                }
            )
        }
    }
    LaunchedEffect(userPreferencesUiState.token) {
        if (userPreferencesUiState.token.isNotEmpty()) {
            Log.d("MyAppNavHost", "Launched effect navigate to tasks")
            Api.tokenInterceptor.token = userPreferencesUiState.token
            myAppViewModel.setToken(userPreferencesUiState.token)
            navController.navigate(tasksRoute) {
                popUpTo(0)
            }
        }
    }
}