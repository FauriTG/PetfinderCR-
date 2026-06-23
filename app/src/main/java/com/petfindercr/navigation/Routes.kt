package com.petfindercr.navigation

sealed class Routes(val route: String) {
    object Splash : Routes("splash")
    object Login : Routes("login")
    object Register : Routes("register")
    object ForgotPassword : Routes("forgot_password")
    object Home : Routes("home")
    object ReportList : Routes("report_list")
    object CreateReport : Routes("create_report")
    object EditReport : Routes("edit_report/{reportId}") {
        fun withId(id: Long) = "edit_report/$id"
    }
    object ReportDetail : Routes("report_detail/{reportId}") {
        fun withId(id: Long) = "report_detail/$id"
    }
    object Map : Routes("map")
    object Profile : Routes("profile")
    object MyReports : Routes("my_reports")
    object AiMatches : Routes("ai_matches")
}
