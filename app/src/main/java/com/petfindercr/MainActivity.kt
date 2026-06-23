package com.petfindercr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.petfindercr.navigation.NavGraph
import com.petfindercr.ui.theme.PetFinderTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PetFinderTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
