package com.fatemeh.nudge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.vector.ImageVector
import com.fatemeh.nudge.data.*
import com.fatemeh.nudge.ui.NudgeTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dao = AppDb.get(this).dao()
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                NudgeTheme { NudgeApp(dao) }
            }
        }
    }
}

private data class Tab(val title: String, val icon: ImageVector)

@Composable
fun NudgeApp(dao: AppDao) {
    val tabs = listOf(
        Tab("دستیار", Icons.Default.AutoAwesome),
        Tab("کارها", Icons.Default.CheckCircle),
        Tab("عادت‌ها", Icons.Default.Repeat),
        Tab("هدف‌ها", Icons.Default.Flag),
        Tab("تقویم", Icons.Default.CalendarMonth)
    )
    var sel by remember { mutableIntStateOf(0) }
    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { i, t ->
                    NavigationBarItem(selected = sel == i, onClick = { sel = i },
                        icon = { Icon(t.icon, null) }, label = { Text(t.title) })
                }
            }
        }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when (sel) {
                0 -> AssistantScreen(dao)
                1 -> TasksScreen(dao)
                2 -> HabitsScreen(dao)
                3 -> GoalsScreen(dao)
                else -> Placeholder("تقویم (مرحله ۲)")
            }
        }
    }
}

@Composable fun Placeholder(text: String) =
    Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) { Text(text) }

@Composable
fun AddDialog(title: String, onDismiss: () -> Unit, onOk: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { OutlinedTextField(text, { text = it }, singleLine = true) },
        confirmButton = { TextButton({ if (text.isNotBlank()) onOk(text.trim()) }) { Text("افزودن") } },
        dismissButton = { TextButton(onDismiss) { Text("لغو") } }
    )
}

@Composable
fun AssistantScreen(dao: AppDao) {
    val msgs by dao.chat().collectAsState(emptyList())
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Text("سلام فاطمه! 🌱", style = MaterialTheme.typography.headlineSmall)
        Text("امروز چه کاری می‌خواهی انجام بدهی؟")
        LazyColumn(Modifier.weight(1f).padding(vertical = 8.dp)) {
            items(msgs) { m ->
                Card(Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (m.fromUser) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface)) {
                    Text(m.text, Modifier.padding(10.dp))
                }
            }
        }
        Row {
            OutlinedTextField(input, { input = it }, Modifier.weight(1f), placeholder = { Text("به دستیارت پیام بده...") })
            IconButton({
                if (input.isNotBlank()) {
                    val t = input; input = ""
                    scope.launch {
                        dao.add(ChatMessage(fromUser = true, text = t))
                        // مرحله ۴: اینجا به Gemini وصل می‌شود
                        dao.add(ChatMessage(fromUser = false, text = "هنوز به هوش مصنوعی وصل نیستم؛ در مرحله ۴ وصل می‌شوم 💚"))
                    }
                }
            }) { Icon(Icons.Default.Send, null) }
        }
    }
}

@Composable
fun TasksScreen(dao: AppDao) {
    val list by dao.tasks().collectAsState(emptyList())
    val scope = rememberCoroutineScope()
    var show by remember { mutableStateOf(false) }
    Scaffold(floatingActionButton = { FloatingActionButton({ show = true }) { Icon(Icons.Default.Add, null) } }) { p ->
        LazyColumn(Modifier.padding(p).padding(12.dp)) {
            items(list) { t ->
                Card(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                    Row(Modifier.padding(8.dp)) {
                        Checkbox(t.status == 1, { scope.launch { dao.update(t.copy(status = if (it) 1 else 0)) } })
                        Text(t.title, Modifier.weight(1f).align(androidx.compose.ui.Alignment.CenterVertically))
                        IconButton({ scope.launch { dao.delete(t) } }) { Icon(Icons.Default.Delete, null) }
                    }
                }
            }
        }
    }
    if (show) AddDialog("کار جدید", { show = false }) { scope.launch { dao.add(Task(title = it)) }; show = false }
}

@Composable
fun HabitsScreen(dao: AppDao) {
    val list by dao.habits().collectAsState(emptyList())
    val scope = rememberCoroutineScope()
    var show by remember { mutableStateOf(false) }
    Scaffold(floatingActionButton = { FloatingActionButton({ show = true }) { Icon(Icons.Default.Add, null) } }) { p ->
        LazyColumn(Modifier.padding(p).padding(12.dp)) {
            items(list) { h ->
                Card(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                    Row(Modifier.padding(8.dp)) {
                        Text("${h.title}  🔥${h.streak}", Modifier.weight(1f).align(androidx.compose.ui.Alignment.CenterVertically))
                        TextButton({
                            val today = System.currentTimeMillis() / 86_400_000
                            if (h.lastDoneDay != today) scope.launch { dao.update(h.copy(streak = h.streak + 1, lastDoneDay = today)) }
                        }) { Text("انجام شد") }
                        IconButton({ scope.launch { dao.delete(h) } }) { Icon(Icons.Default.Delete, null) }
                    }
                }
            }
        }
    }
    if (show) AddDialog("عادت جدید", { show = false }) { scope.launch { dao.add(Habit(title = it)) }; show = false }
}

@Composable
fun GoalsScreen(dao: AppDao) {
    val list by dao.goals().collectAsState(emptyList())
    val scope = rememberCoroutineScope()
    var show by remember { mutableStateOf(false) }
    Scaffold(floatingActionButton = { FloatingActionButton({ show = true }) { Icon(Icons.Default.Add, null) } }) { p ->
        LazyColumn(Modifier.padding(p).padding(12.dp)) {
            items(list) { g ->
                Card(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                    Column(Modifier.padding(10.dp)) {
                        Row {
                            Text(g.title, Modifier.weight(1f))
                            Text("${g.progress}%")
                            IconButton({ scope.launch { dao.delete(g) } }) { Icon(Icons.Default.Delete, null) }
                        }
                        Slider(g.progress / 100f, { scope.launch { dao.update(g.copy(progress = (it * 100).toInt())) } })
                    }
                }
            }
        }
    }
    if (show) AddDialog("هدف جدید", { show = false }) { scope.launch { dao.add(Goal(title = it)) }; show = false }
}
