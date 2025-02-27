package com.example.androidclient.todo.ui.tasks

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidclient.R
import com.example.androidclient.todo.data.Task

typealias OnTaskFn = (id: Int?) -> Unit

@Composable
fun TaskList(
    taskList: List<Task>,
    onTaskClick: OnTaskFn,
    modifier: Modifier
) {
    Log.d("TaskList", "recompose")
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        items(taskList) {
            ExpandableTaskCard(
                task = it,
                onTaskClick = { onTaskClick(it.id) }
            )
        }
    }
}
//
//@Composable
//fun TaskDetail(task: Task, onTaskClick: OnTaskFn) {
//    Row (
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(8.dp)
//    ) {
//        Text(
//            text = AnnotatedString(task.title),
//            style = TextStyle(
//                fontSize = 24.sp,
//                color = Color.Cyan
//            ),
//            modifier = Modifier
//                .fillMaxWidth()
//                .clickable { task.id.let(onTaskClick) }
//        )
//    }
//}