package com.example.verlibr.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.verlibr.R
import com.example.verlibr.data.AuthorPreview
import com.example.verlibr.data.PoemPreview
import com.example.verlibr.ui.theme.Cream
import com.example.verlibr.ui.theme.Fog
import com.example.verlibr.ui.theme.Stone
import com.example.verlibr.ui.theme.Taupe

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader(
    onLogoClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.clickable(onClick = onLogoClick)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_logo),
                    contentDescription = "На главную",
                    modifier = Modifier.size(40.dp)
                )
                Text("Verlibr", fontSize = 32.sp, fontWeight = FontWeight.Light)
            }
        },
        actions = {
            Surface(
                onClick = onLoginClick,
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Text(
                    text = "Вход",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

class BottomTab(val route: String, val label: String, @DrawableRes val icon: Int)

@Composable
fun BottomBar(
    tabs: List<BottomTab>,
    currentRoute: String?,
    onTabClick: (BottomTab) -> Unit
) {
    NavigationBar(containerColor = Taupe) {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = { onTabClick(tab) },
                icon = {
                    Icon(
                        painter = painterResource(tab.icon),
                        contentDescription = tab.label,
                        modifier = Modifier.size(40.dp)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Cream,
                    unselectedIconColor = Cream.copy(alpha = 0.55f),
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun SearchPill(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        shape = RoundedCornerShape(6.dp),
        color = Fog,
        border = BorderStroke(1.dp, Stone)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Строка, цитата, настроение или автор..",
                modifier = Modifier.weight(1f),
                color = Stone,
                fontSize = 13.sp,
                fontStyle = FontStyle.Italic,
                maxLines = 1
            )
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = "Поиск",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/** Карточка с заголовком и строками, разделёнными линиями. */
@Composable
fun <T> SectionCard(
    title: String,
    items: List<T>,
    row: @Composable (T) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
            .padding(horizontal = 26.dp, vertical = 16.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Center
        )
        items.forEachIndexed { index, item ->
            if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            row(item)
        }
    }
}

@Composable
fun PoemRow(poem: PoemPreview, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(poem.title, fontSize = 16.sp)
            Text(poem.author, fontSize = 12.sp)
        }
        Text(poem.year?.toString().orEmpty(), fontSize = 16.sp)
    }
}

@Composable
fun AuthorRow(author: AuthorPreview, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Заглушка вместо аватара
        Spacer(
            Modifier
                .size(40.dp)
                .background(Taupe, CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
        )
        Spacer(Modifier.width(38.dp))
        Column {
            Text(author.name, fontSize = 16.sp)
            Text(author.years, fontSize = 12.sp)
        }
    }
}
