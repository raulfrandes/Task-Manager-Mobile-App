package com.example.androidclient.todo.ui.tasks

import android.widget.Space
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.androidclient.todo.data.Task

@Composable
fun ExpandableTaskCard(
    task: Task,
    onTaskClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .animateContentSize(animationSpec = tween(durationMillis = 300))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            if (isExpanded) {
                Spacer(modifier = Modifier.padding(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTaskClick() }
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Description: ${task.description}",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Justify
                    )
                    Spacer(modifier = Modifier.padding(8.dp))
                    Text(
                        text = "Priority: ${task.priority}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.padding(8.dp))
                    Text(
                        text = "Due Date: ${task.dueDate.split("T").getOrNull(0) ?: task.dueDate}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}