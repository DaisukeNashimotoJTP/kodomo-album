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
import com.example.kodomo_album.presentation.diary.DiaryListScreen
import com.example.kodomo_album.presentation.diary.DiaryCreateEditScreen
import com.example.kodomo_album.presentation.diary.DiaryDetailScreen
import com.example.kodomo_album.presentation.diary.MediaSelectionScreen
import com.example.kodomo_album.presentation.growth.GrowthRecordScreen
import com.example.kodomo_album.presentation.growth.GrowthChartScreen
import com.example.kodomo_album.presentation.growth.GrowthSummaryScreen
import com.example.kodomo_album.presentation.milestone.MilestoneListScreen
import com.example.kodomo_album.presentation.milestone.MilestoneInputScreen
import com.example.kodomo_album.presentation.event.EventListScreen
import com.example.kodomo_album.presentation.event.EventInputScreen
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
                                },
                                onViewDiaryClick = { childId ->
                                    navController.navigate("diary_list/$childId")
                                },
                                onViewGrowthClick = { childId ->
                                    navController.navigate("growth_record/$childId")
                                },
                                onViewMilestoneClick = { childId ->
                                    navController.navigate("milestone_list/$childId")
                                },
                                onViewEventClick = { childId ->
                                    navController.navigate("event_list/$childId")
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
                        
                        // Diary screens
                        composable(
                            "diary_list/{childId}",
                            arguments = listOf(navArgument("childId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val childId = backStackEntry.arguments?.getString("childId") ?: ""
                            DiaryListScreen(
                                childId = childId,
                                onNavigateToCreate = {
                                    navController.navigate("diary_create/$childId")
                                },
                                onNavigateToDetail = { diaryId ->
                                    navController.navigate("diary_detail/$diaryId")
                                }
                            )
                        }
                        
                        composable(
                            "diary_create/{childId}",
                            arguments = listOf(navArgument("childId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val childId = backStackEntry.arguments?.getString("childId") ?: ""
                            DiaryCreateEditScreen(
                                childId = childId,
                                diaryId = null,
                                onNavigateUp = {
                                    navController.popBackStack()
                                },
                                onSelectMedia = {
                                    navController.navigate("media_selection/$childId")
                                }
                            )
                        }
                        
                        composable(
                            "diary_edit/{childId}/{diaryId}",
                            arguments = listOf(
                                navArgument("childId") { type = NavType.StringType },
                                navArgument("diaryId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val childId = backStackEntry.arguments?.getString("childId") ?: ""
                            val diaryId = backStackEntry.arguments?.getString("diaryId") ?: ""
                            DiaryCreateEditScreen(
                                childId = childId,
                                diaryId = diaryId,
                                onNavigateUp = {
                                    navController.popBackStack()
                                },
                                onSelectMedia = {
                                    navController.navigate("media_selection/$childId")
                                }
                            )
                        }
                        
                        composable(
                            "diary_detail/{diaryId}",
                            arguments = listOf(navArgument("diaryId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val diaryId = backStackEntry.arguments?.getString("diaryId") ?: ""
                            DiaryDetailScreen(
                                diaryId = diaryId,
                                onNavigateUp = {
                                    navController.popBackStack()
                                },
                                onNavigateToEdit = { editDiaryId ->
                                    // We need childId to navigate to edit, so we'll need to get it from the diary
                                    // For now, we'll just go back - this can be improved later
                                    navController.popBackStack()
                                },
                                onNavigateToMediaDetail = { mediaId ->
                                    // Navigate to media detail if exists
                                }
                            )
                        }
                        
                        composable(
                            "media_selection/{childId}",
                            arguments = listOf(navArgument("childId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val childId = backStackEntry.arguments?.getString("childId") ?: ""
                            MediaSelectionScreen(
                                childId = childId,
                                selectedMediaIds = emptyList(), // This should be passed from the diary screen
                                onMediaSelected = { mediaIds ->
                                    // Handle selected media - navigate back with result
                                    navController.popBackStack()
                                },
                                onNavigateUp = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        
                        // Family Sharing Routes
                        composable("family_management") {
                            com.example.kodomoalbum.presentation.ui.sharing.FamilyManagementScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateToInvitePartner = {
                                    navController.navigate("invite_partner")
                                },
                                onNavigateToInvitations = {
                                    navController.navigate("invitation_list")
                                },
                                onNavigateToSharedContent = {
                                    navController.navigate("shared_content")
                                }
                            )
                        }
                        
                        composable("invite_partner") {
                            com.example.kodomoalbum.presentation.ui.sharing.InvitePartnerScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        
                        composable("invitation_list") {
                            com.example.kodomoalbum.presentation.ui.sharing.InvitationListScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        
                        composable("shared_content") {
                            com.example.kodomoalbum.presentation.ui.sharing.SharedContentScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        
                        // Growth Record screens
                        composable(
                            "growth_record/{childId}",
                            arguments = listOf(navArgument("childId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val childId = backStackEntry.arguments?.getString("childId") ?: ""
                            GrowthRecordScreen(
                                childId = childId,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateToChart = { childId ->
                                    navController.navigate("growth_chart/$childId")
                                },
                                onNavigateToSummary = { childId ->
                                    navController.navigate("growth_summary/$childId")
                                }
                            )
                        }
                        
                        composable(
                            "growth_chart/{childId}",
                            arguments = listOf(navArgument("childId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val childId = backStackEntry.arguments?.getString("childId") ?: ""
                            GrowthChartScreen(
                                childId = childId,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        
                        composable(
                            "growth_summary/{childId}",
                            arguments = listOf(navArgument("childId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val childId = backStackEntry.arguments?.getString("childId") ?: ""
                            GrowthSummaryScreen(
                                childId = childId,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onExportData = {
                                    // TODO: Implement data export functionality
                                }
                            )
                        }
                        
                        // Milestone screens
                        composable(
                            "milestone_list/{childId}",
                            arguments = listOf(navArgument("childId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val childId = backStackEntry.arguments?.getString("childId") ?: ""
                            MilestoneListScreen(
                                childId = childId,
                                onNavigateUp = {
                                    navController.popBackStack()
                                },
                                onNavigateToAdd = {
                                    navController.navigate("milestone_input/$childId")
                                }
                            )
                        }
                        
                        composable(
                            "milestone_input/{childId}",
                            arguments = listOf(navArgument("childId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val childId = backStackEntry.arguments?.getString("childId") ?: ""
                            MilestoneInputScreen(
                                childId = childId,
                                onNavigateUp = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        
                        // Event screens
                        composable(
                            "event_list/{childId}",
                            arguments = listOf(navArgument("childId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val childId = backStackEntry.arguments?.getString("childId") ?: ""
                            EventListScreen(
                                childId = childId,
                                onNavigateUp = {
                                    navController.popBackStack()
                                },
                                onNavigateToAdd = {
                                    navController.navigate("event_input/$childId")
                                }
                            )
                        }
                        
                        composable(
                            "event_input/{childId}",
                            arguments = listOf(navArgument("childId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val childId = backStackEntry.arguments?.getString("childId") ?: ""
                            EventInputScreen(
                                childId = childId,
                                onNavigateUp = {
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