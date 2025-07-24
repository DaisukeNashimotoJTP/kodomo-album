package com.example.kodomo_album

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.kodomo_album.core.util.UiEvent
import com.example.kodomo_album.data.repository.AuthRepository
import com.example.kodomo_album.presentation.auth.LoginScreen
import com.example.kodomo_album.presentation.auth.SignUpScreen
import com.example.kodomo_album.presentation.auth.PasswordResetScreen
import com.example.kodomo_album.presentation.profile.ProfileScreen
import com.example.kodomo_album.presentation.child.ChildListScreen
import com.example.kodomo_album.presentation.child.AddEditChildScreen
import com.example.kodomo_album.presentation.media.MediaUploadScreen
import com.example.kodomo_album.domain.usecase.child.GetChildrenUseCase
import com.example.kodomo_album.ui.theme.KodomoalbumTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var authRepository: AuthRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KodomoalbumTheme {
                val navController = rememberNavController()
                val currentUser by authRepository.getCurrentUser().collectAsState(initial = null)
                
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = if (currentUser != null) "profile" else "login"
                    ) {
                        composable("login") {
                            LoginScreen(
                                onNavigate = { uiEvent ->
                                    when (uiEvent) {
                                        is UiEvent.Navigate -> {
                                            when (uiEvent.route) {
                                                "profile" -> navController.navigate("profile") {
                                                    popUpTo("login") { inclusive = true }
                                                }
                                                "signup" -> navController.navigate("signup")
                                                "password_reset" -> navController.navigate("password_reset")
                                                else -> navController.navigate(uiEvent.route)
                                            }
                                        }
                                    }
                                },
                                onShowSnackbar = { message ->
                                    // Handle snackbar display - could implement SnackbarHost here
                                }
                            )
                        }
                        composable("signup") {
                            SignUpScreen(
                                onNavigate = { uiEvent ->
                                    when (uiEvent.route) {
                                        "profile" -> navController.navigate("profile") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                        else -> navController.navigate(uiEvent.route)
                                    }
                                },
                                onNavigateUp = {
                                    navController.popBackStack()
                                },
                                onShowSnackbar = { message ->
                                    // Handle snackbar display
                                }
                            )
                        }
                        composable("password_reset") {
                            PasswordResetScreen(
                                onNavigateUp = {
                                    navController.popBackStack()
                                },
                                onShowSnackbar = { message ->
                                    // Handle snackbar display
                                }
                            )
                        }
                        composable("profile") {
                            ProfileScreen(
                                onNavigate = { uiEvent ->
                                    when (uiEvent) {
                                        is UiEvent.Navigate -> {
                                            when (uiEvent.route) {
                                                "login" -> navController.navigate("login") {
                                                    popUpTo(0) { inclusive = true }
                                                }
                                                "children" -> navController.navigate("children")
                                                else -> navController.navigate(uiEvent.route)
                                            }
                                        }
                                    }
                                },
                                onShowSnackbar = { message ->
                                    // Handle snackbar display
                                }
                            )
                        }
                        composable("children") {
                            ChildListScreen(
                                userId = currentUser?.id ?: "",
                                onAddChildClick = {
                                    navController.navigate("add_child")
                                },
                                onEditChildClick = { child ->
                                    navController.navigate("edit_child/${child.id}")
                                },
                                onAddMediaClick = {
                                    navController.navigate("media_upload")
                                }
                            )
                        }
                        composable("add_child") {
                            AddEditChildScreen(
                                userId = currentUser?.id ?: "",
                                childId = null,
                                childToEdit = null,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable(
                            "edit_child/{childId}",
                            arguments = listOf(navArgument("childId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val childId = backStackEntry.arguments?.getString("childId") ?: ""
                            AddEditChildScreen(
                                userId = currentUser?.id ?: "",
                                childId = childId,
                                childToEdit = null,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("media_upload") {
                            val childManagementViewModel: com.example.kodomo_album.presentation.child.ChildManagementViewModel = hiltViewModel()
                            val children by childManagementViewModel.children.collectAsState()
                            
                            LaunchedEffect(currentUser?.id) {
                                currentUser?.id?.let { userId ->
                                    childManagementViewModel.loadChildren(userId)
                                }
                            }
                            
                            MediaUploadScreen(
                                children = children,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}