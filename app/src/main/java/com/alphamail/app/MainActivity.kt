@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.alphamail.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AlphaMailApp() }
    }
}

private enum class MailTheme { DARK, LIGHT, READABLE }
private enum class HomeView { ALL, THREADS, SENDERS }
private enum class Page { HOME, ATTACHMENTS, SEARCH, SETTINGS }

private data class Mail(
    val id: Int, val sender: String, val address: String, val subject: String,
    val snippet: String, val body: String, val time: String, val thread: String
)

private val sampleMails = listOf(
    Mail(1, "ABC Company", "accounts@abc.example", "Invoice payment confirmation",
        "Dear Sir, your invoice has been processed...",
        "Dear Sir,\n\nYour invoice has been processed. The payment will be credited to your account within three working days.\n\nRegards,\nAccounts Team",
        "10:30", "invoice-1"),
    Mail(2, "Manager", "manager@company.example", "Meeting schedule",
        "Please find the revised meeting schedule...",
        "Please find the revised meeting schedule attached. Kindly confirm your availability.\n\nRegards,\nManager",
        "09:45", "meeting-1"),
    Mail(3, "ABC Company", "accounts@abc.example", "Invoice payment confirmation",
        "Thank you for your confirmation...",
        "Thank you for your confirmation. Please let us know if any further details are required.",
        "Yesterday", "invoice-1"),
    Mail(4, "Newsletter", "news@example.com", "This week's updates",
        "Here are the latest updates...",
        "Here are the latest updates from our team. Read the full newsletter on our website.",
        "Yesterday", "news-1")
)

@Composable
private fun AlphaMailApp() {
    var theme by remember { mutableStateOf(MailTheme.DARK) }
    var view by remember { mutableStateOf(HomeView.ALL) }
    var page by remember { mutableStateOf(Page.HOME) }
    var selectedMail by remember { mutableStateOf<Mail?>(null) }
    var expandedPreviews by remember { mutableStateOf(setOf<Int>()) }
    var selectedThread by remember { mutableStateOf<String?>(null) }
    val colors = palette(theme)

    MaterialTheme(colorScheme = colors) {
        Surface(Modifier.fillMaxSize(), color = colors.background) {
            if (selectedMail != null) {
                OriginalMailScreen(selectedMail!!, colors) { selectedMail = null }
            } else if (selectedThread != null) {
                ThreadScreen(selectedThread!!, colors, onBack = { selectedThread = null },
                    onOpenMail = { selectedMail = it })
            } else {
                Scaffold(
                    containerColor = colors.background,
                    topBar = {
                        TopAppBar(
                            title = { Text("Alphamail", fontWeight = FontWeight.SemiBold) },
                            navigationIcon = {
                                IconButton(onClick = { page = Page.SETTINGS }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                                }
                            },
                            actions = {
                                IconButton(onClick = { page = Page.SEARCH }) {
                                    Icon(Icons.Default.Search, contentDescription = "Search")
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = colors.surface, titleContentColor = colors.onSurface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(containerColor = colors.surface) {
                            NavigationBarItem(selected = page == Page.HOME, onClick = { page = Page.HOME },
                                icon = { Icon(Icons.Default.Home, null) }, label = { Text("Mail") })
                            NavigationBarItem(selected = page == Page.ATTACHMENTS, onClick = { page = Page.ATTACHMENTS },
                                icon = { Icon(Icons.Default.AttachFile, null) }, label = { Text("Files") })
                            NavigationBarItem(selected = false, onClick = { /* compose placeholder */ },
                                icon = { Icon(Icons.Default.Edit, null) }, label = { Text("Compose") })
                            NavigationBarItem(selected = page == Page.SEARCH, onClick = { page = Page.SEARCH },
                                icon = { Icon(Icons.Default.Search, null) }, label = { Text("Search") })
                            NavigationBarItem(selected = page == Page.SETTINGS, onClick = { page = Page.SETTINGS },
                                icon = { Icon(Icons.Default.Settings, null) }, label = { Text("Settings") })
                        }
                    }
                ) { padding ->
                    when (page) {
                        Page.HOME -> HomeScreen(view, { view = it }, expandedPreviews,
                            { id -> expandedPreviews = if (id in expandedPreviews) expandedPreviews - id else expandedPreviews + id },
                            colors, onOpenThread = { selectedThread = it }, onOpenMail = { selectedMail = it })
                        Page.SETTINGS -> SettingsScreen(theme, { theme = it }, colors)
                        Page.ATTACHMENTS -> SimplePage("Attachments", "Attachment gallery and file-type filters will be added in the next milestone.", colors)
                        Page.SEARCH -> SimplePage("Search", "Search across messages, senders and subjects will be added in the next milestone.", colors)
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(
    view: HomeView, onView: (HomeView) -> Unit, expanded: Set<Int>, onToggle: (Int) -> Unit,
    c: ColorScheme, onOpenThread: (String) -> Unit, onOpenMail: (Mail) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(HomeView.ALL to "All emails", HomeView.THREADS to "Conversations", HomeView.SENDERS to "Senders")
                .forEach { (item, label) ->
                    val selected = view == item
                    Surface(
                        modifier = Modifier.weight(1f).clickable { onView(item) },
                        color = if (selected) c.primaryContainer else c.surface,
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text(label, modifier = Modifier.padding(vertical = 11.dp, horizontal = 3.dp),
                            color = if (selected) c.onPrimaryContainer else c.onSurface,
                            fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1)
                    }
                }
        }
        when (view) {
            HomeView.ALL -> LazyColumn {
                items(sampleMails) { mail ->
                    MailRow(mail, expanded.contains(mail.id), { onToggle(mail.id) }, c,
                        onOpen = { onOpenMail(mail) }, showPreviewButton = true)
                }
            }
            HomeView.THREADS -> {
                val threads = sampleMails.groupBy { it.thread }.values.map { it.first() }
                LazyColumn { items(threads) { mail ->
                    MailRow(mail.copy(snippet = "${sampleMails.count { it.thread == mail.thread }} messages · ${mail.snippet}"),
                        false, {}, c, onOpen = { onOpenThread(mail.thread) }, showPreviewButton = false)
                } }
            }
            HomeView.SENDERS -> {
                val senders = sampleMails.groupBy { it.address }.values.map { it.first() }
                LazyColumn { items(senders) { mail ->
                    MailRow(mail.copy(snippet = "${sampleMails.count { it.address == mail.address }} emails · ${mail.snippet}"),
                        false, {}, c, onOpen = { onOpenThread(mail.thread) }, showPreviewButton = false)
                } }
            }
        }
    }
}

@Composable
private fun MailRow(mail: Mail, expanded: Boolean, toggle: () -> Unit, c: ColorScheme,
                    onOpen: () -> Unit, showPreviewButton: Boolean) {
    Column(Modifier.fillMaxWidth().clickable { onOpen() }.padding(horizontal = 14.dp, vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).background(c.secondaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                Text(mail.sender.take(1).uppercase(), color = c.onSecondaryContainer, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Text(mail.sender, color = c.primary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(mail.subject, color = c.onSurface, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(mail.snippet, color = c.onSurfaceVariant, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(mail.time, color = c.onSurfaceVariant, fontSize = 10.sp)
                if (showPreviewButton) {
                    TextButton(onClick = toggle, contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)) {
                        Text(if (expanded) "Hide" else "Preview", fontSize = 11.sp)
                    }
                }
            }
        }
        if (expanded) {
            Surface(Modifier.fillMaxWidth().padding(start = 53.dp, top = 8.dp),
                color = c.surfaceVariant, shape = RoundedCornerShape(10.dp)) {
                Text(mail.body, Modifier.padding(12.dp), color = c.onSurface, fontSize = 13.sp, lineHeight = 19.sp)
            }
        }
        HorizontalDivider(color = c.outlineVariant.copy(alpha = 0.35f))
    }
}

@Composable
private fun ThreadScreen(thread: String, c: ColorScheme, onBack: () -> Unit, onOpenMail: (Mail) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().background(c.surface).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
            Text("Conversation", color = c.onSurface, fontWeight = FontWeight.SemiBold)
        }
        LazyColumn(Modifier.weight(1f).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(sampleMails.filter { it.thread == thread }) { mail ->
                val incoming = mail.id % 2 == 1
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (incoming) Arrangement.Start else Arrangement.End) {
                    Surface(Modifier.widthIn(max = 310.dp).clickable { onOpenMail(mail) },
                        color = if (incoming) c.surfaceVariant else c.primaryContainer,
                        shape = RoundedCornerShape(15.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text(mail.sender, fontSize = 11.sp, color = c.primary, fontWeight = FontWeight.Bold)
                            Text(mail.subject, fontSize = 13.sp, color = c.onSurface, fontWeight = FontWeight.SemiBold)
                            Text(mail.snippet, fontSize = 12.sp, color = c.onSurfaceVariant, maxLines = 3)
                            Text("Tap to open original email", fontSize = 10.sp, color = c.onSurfaceVariant)
                            Text(mail.time, Modifier.align(Alignment.End), fontSize = 10.sp, color = c.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OriginalMailScreen(mail: Mail, c: ColorScheme, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().background(c.surface).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
            Text("Original email", color = c.onSurface, fontWeight = FontWeight.SemiBold)
        }
        Column(Modifier.padding(18.dp)) {
            Text(mail.subject, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = c.onSurface)
            Text("${mail.sender} · ${mail.address} · ${mail.time}", fontSize = 12.sp, color = c.onSurfaceVariant)
            HorizontalDivider(Modifier.padding(vertical = 14.dp))
            Text(mail.body, color = c.onSurface, fontSize = 15.sp, lineHeight = 23.sp)
        }
    }
}

@Composable
private fun SettingsScreen(theme: MailTheme, onTheme: (MailTheme) -> Unit, c: ColorScheme) {
    Column(Modifier.fillMaxSize().padding(18.dp)) {
        Text("Settings", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = c.onSurface)
        Spacer(Modifier.height(18.dp))
        Text("Appearance", color = c.primary, fontWeight = FontWeight.SemiBold)
        listOf(MailTheme.DARK to "Dark", MailTheme.LIGHT to "Light", MailTheme.READABLE to "Readable (warm tint)")
            .forEach { (value, label) ->
                Row(Modifier.fillMaxWidth().clickable { onTheme(value) }.padding(vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = theme == value, onClick = { onTheme(value) })
                    Text(label, color = c.onSurface)
                }
            }
        HorizontalDivider(Modifier.padding(vertical = 12.dp))
        Text("Accounts", color = c.primary, fontWeight = FontWeight.SemiBold)
        Text("Gmail, Yahoo, Outlook and IMAP/SMTP account connection will be implemented in the mail-engine milestone.",
            color = c.onSurfaceVariant, fontSize = 13.sp)
    }
}

@Composable
private fun SimplePage(title: String, message: String, c: ColorScheme) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = c.onSurface)
        Spacer(Modifier.height(10.dp))
        Text(message, color = c.onSurfaceVariant)
    }
}

private fun palette(theme: MailTheme): ColorScheme = when (theme) {
    MailTheme.DARK -> darkColorScheme(
        primary = Color(0xFF65B7D4), background = Color(0xFF17191D), surface = Color(0xFF22252A),
        surfaceVariant = Color(0xFF30343A), onSurface = Color(0xFFE8EAED),
        onSurfaceVariant = Color(0xFFB0B6BE), primaryContainer = Color(0xFF244653),
        onPrimaryContainer = Color(0xFFD6F4FF), secondaryContainer = Color(0xFF35444C),
        onSecondaryContainer = Color(0xFFE1F3F8), outlineVariant = Color(0xFF555B62)
    )
    MailTheme.LIGHT -> lightColorScheme(
        primary = Color(0xFF287D9C), background = Color(0xFFF7F8FA), surface = Color.White,
        surfaceVariant = Color(0xFFECEFF2), onSurface = Color(0xFF20252A),
        onSurfaceVariant = Color(0xFF5D6670), primaryContainer = Color(0xFFD5EFF8),
        onPrimaryContainer = Color(0xFF123B4A), secondaryContainer = Color(0xFFE0EAF0),
        onSecondaryContainer = Color(0xFF263943)
    )
    MailTheme.READABLE -> lightColorScheme(
        primary = Color(0xFF79572A), background = Color(0xFFF5E9CE), surface = Color(0xFFF8EED9),
        surfaceVariant = Color(0xFFEDE0C3), onSurface = Color(0xFF403421),
        onSurfaceVariant = Color(0xFF6B5A40), primaryContainer = Color(0xFFE6D2A8),
        onPrimaryContainer = Color(0xFF44351D), secondaryContainer = Color(0xFFE8D8B8),
        onSecondaryContainer = Color(0xFF4C3B24)
    )
}
