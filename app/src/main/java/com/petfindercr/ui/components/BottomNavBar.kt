package com.petfindercr.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class NavItem(val label: String, val icon: ImageVector, val route: String)

private val items = listOf(
    NavItem("Inicio",    Icons.Default.Home,        "home"),
    NavItem("Mapa",      Icons.Default.Map,          "map"),
    NavItem("Reportar",  Icons.Default.Add,          "create_report"),
    NavItem("IA",        Icons.Default.AutoAwesome,  "ai_matches"),
    NavItem("Perfil",    Icons.Default.Person,       "profile")
)

@Composable
fun PetFinderBottomBar(currentRoute: String, onNavigate: (String) -> Unit) {
    Surface(
        color = Color.White,
        shadowElevation = 12.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isCenter = item.route == "create_report"
                val isSelected = currentRoute == item.route

                if (isCenter) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .offset(y = (-4).dp)
                            .shadow(elevation = 8.dp, shape = CircleShape)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { onNavigate(item.route) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Reportar",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                } else {
                    NavBarItem(item = item, isSelected = isSelected) { onNavigate(item.route) }
                }
            }
        }
    }
}

@Composable
private fun NavBarItem(item: NavItem, isSelected: Boolean, onClick: () -> Unit) {
    val color by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF94A3B8),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "navColor"
    )
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = item.label,
            color = color,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

// Keep backward-compat alias for any other screen that might use it
data class BottomNavItem(val label: String, val icon: ImageVector, val route: String)
