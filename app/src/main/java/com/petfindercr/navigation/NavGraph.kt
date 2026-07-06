package com.petfindercr.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.petfindercr.ui.ai.AiMatchesScreen
import com.petfindercr.ui.chat.ChatScreen
import com.petfindercr.ui.chat.InboxScreen
import com.petfindercr.ui.forgot.ForgotPasswordScreen
import com.petfindercr.ui.home.HomeScreen
import com.petfindercr.ui.login.LoginScreen
import com.petfindercr.ui.notifications.NotificationsScreen
import com.petfindercr.ui.map.MapScreen
import com.petfindercr.ui.profile.MyReportsScreen
import com.petfindercr.ui.profile.ProfileScreen
import com.petfindercr.ui.profile.PublicProfileScreen
import com.petfindercr.ui.register.RegisterScreen
import com.petfindercr.ui.report.CreateReportScreen
import com.petfindercr.ui.report.EditReportScreen
import com.petfindercr.ui.report.QuickReportScreen
import com.petfindercr.ui.report.ReportDetailScreen
import com.petfindercr.ui.report.ReportListScreen
import com.petfindercr.ui.splash.SplashScreen

@Composable
fun NavGraph(navController: NavHostController, startDestination: String = Routes.Splash.route) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Routes.Register.route) },
                onNavigateToForgot = { navController.navigate(Routes.ForgotPassword.route) }
            )
        }

        composable(Routes.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Routes.ForgotPassword.route) {
            ForgotPasswordScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.Home.route) {
            HomeScreen(
                onNavigateToDetail = { id -> navController.navigate(Routes.ReportDetail.withId(id)) },
                onNavigateToCreate = { navController.navigate(Routes.CreateReport.route) },
                onNavigateToMap = { navController.navigate(Routes.Map.route) },
                onNavigateToProfile = { navController.navigate(Routes.Profile.route) },
                onNavigateToList = { navController.navigate(Routes.ReportList.route) },
                onNavigateToAiMatches = { navController.navigate(Routes.AiMatches.route) },
                onNavigateToQuickReport = { navController.navigate(Routes.QuickReport.route) },
                onNavigateToInbox = { navController.navigate(Routes.Inbox.route) },
                onNavigateToNotifications = { navController.navigate(Routes.Notifications.route) }
            )
        }

        composable(Routes.AiMatches.route) {
            AiMatchesScreen(
                onBack = { navController.popBackStack() },
                onNavigateToDetail = { id -> navController.navigate(Routes.ReportDetail.withId(id)) }
            )
        }

        composable(Routes.ReportList.route) {
            ReportListScreen(
                onNavigateToDetail = { id -> navController.navigate(Routes.ReportDetail.withId(id)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.CreateReport.route) {
            CreateReportScreen(
                onSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.QuickReport.route) {
            QuickReportScreen(
                onSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.EditReport.route,
            arguments = listOf(navArgument("reportId") { type = NavType.LongType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getLong("reportId") ?: return@composable
            EditReportScreen(
                reportId = reportId,
                onSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.ReportDetail.route,
            arguments = listOf(navArgument("reportId") { type = NavType.LongType })
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getLong("reportId") ?: return@composable
            ReportDetailScreen(
                reportId = reportId,
                onEdit = { id -> navController.navigate(Routes.EditReport.withId(id)) },
                onOpenChat = { receptorId, nombre -> navController.navigate(Routes.Chat.create(receptorId, nombre)) },
                onOpenProfile = { uid -> navController.navigate(Routes.PublicProfile.create(uid)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.Notifications.route) {
            NotificationsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.Inbox.route) {
            InboxScreen(
                onOpenChat = { receptorId, nombre -> navController.navigate(Routes.Chat.create(receptorId, nombre)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.PublicProfile.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            PublicProfileScreen(
                userId = userId,
                onOpenChat = { receptorId, nombre -> navController.navigate(Routes.Chat.create(receptorId, nombre)) },
                onOpenReport = { id -> navController.navigate(Routes.ReportDetail.withId(id)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.Chat.route,
            arguments = listOf(
                navArgument("receptorId") { type = NavType.StringType },
                navArgument("receptorNombre") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val receptorId = backStackEntry.arguments?.getString("receptorId") ?: return@composable
            val receptorNombre = backStackEntry.arguments?.getString("receptorNombre") ?: "Chat"
            ChatScreen(
                receptorId = receptorId,
                receptorNombre = receptorNombre,
                onOpenProfile = { uid -> navController.navigate(Routes.PublicProfile.create(uid)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.Map.route) {
            MapScreen(
                onNavigateToDetail = { id -> navController.navigate(Routes.ReportDetail.withId(id)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.Profile.route) {
            ProfileScreen(
                onNavigateToMyReports = { navController.navigate(Routes.MyReports.route) },
                onLogout = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.MyReports.route) {
            MyReportsScreen(
                onNavigateToDetail = { id -> navController.navigate(Routes.ReportDetail.withId(id)) },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
