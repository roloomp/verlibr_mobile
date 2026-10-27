package com.example.verlibr.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.verlibr.R
import com.example.verlibr.ui.theme.Cream
import com.example.verlibr.ui.theme.Fog
import com.example.verlibr.ui.theme.Stone
import com.example.verlibr.ui.theme.Taupe
import com.example.verlibr.ui.viewmodels.SearchSort

@Composable
fun SearchField(value: String, onValueChange: (String) -> Unit) {
    val focus = LocalFocusManager.current
    val shape = RoundedCornerShape(6.dp)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        singleLine = true,
        textStyle = TextStyle(fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
        decorationBox = { inner ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .background(Fog)
                    .border(1.dp, Stone, shape)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = "Строка, цитата, настроение или автор..",
                            color = Stone,
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            maxLines = 1
                        )
                    }
                    inner()
                }
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = "Поиск",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    )
}

@Composable
fun SortFilterBar(
    sort: SearchSort,
    onSortChange: (SearchSort) -> Unit,
    activeFilters: Int,
    onFiltersClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(Modifier.weight(1f)) {
            Text("Сортировать по", fontSize = 12.sp, modifier = Modifier.outlined().padding(8.dp, 6.dp))
            SortDropdown(sort, onSortChange)
        }
        Text(
            text = "Фильтры ($activeFilters)",
            fontSize = 12.sp,
            modifier = Modifier
                .outlined(MaterialTheme.colorScheme.primary)
                .clickable(onClick = onFiltersClick)
                .padding(10.dp, 8.dp)
        )
    }
}

@Composable
private fun SortDropdown(sort: SearchSort, onSelect: (SearchSort) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .outlined()
                .clickable { open = true }
                .padding(8.dp, 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(sort.label, fontSize = 12.sp)
            Icon(
                painter = painterResource(R.drawable.ic_chevron),
                contentDescription = null,
                modifier = Modifier
                    .size(12.dp)
                    .rotate(180f)
            )
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            SearchSort.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label, fontSize = 14.sp) },
                    onClick = {
                        onSelect(option)
                        open = false
                    }
                )
            }
        }
    }
}

private fun Modifier.outlined(color: Color = Color.Black) =
    this.clip(RoundedCornerShape(6.dp)).border(1.dp, color, RoundedCornerShape(6.dp))

/** Раскрывающаяся секция фильтра: плашка Taupe + содержимое в рамке. */
@Composable
fun FilterSection(
    title: String,
    selectedCount: Int = 0,
    initiallyExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Taupe)
                .clickable { expanded = !expanded }
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedCount > 0) "$title ($selectedCount)" else title,
                color = Cream,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                painter = painterResource(R.drawable.ic_chevron),
                contentDescription = null,
                tint = Cream,
                modifier = Modifier
                    .size(12.dp)
                    .rotate(if (expanded) 0f else 180f)
            )
        }
        AnimatedVisibility(expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Taupe)
                    .padding(8.dp),
                content = content
            )
        }
    }
    Spacer(Modifier.height(12.dp))
}

@Composable
fun CheckRow(text: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            modifier = Modifier.padding(8.dp),
            colors = CheckboxDefaults.colors(checkedColor = Taupe, checkmarkColor = Cream)
        )
        Text(text, fontSize = 14.sp)
    }
}

@Composable
fun SheetButton(text: String, filled: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (filled) accent else Color.Transparent)
            .border(1.5.dp, accent, shape)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            color = if (filled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
        )
    }
}
