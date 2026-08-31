package com.example.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.data.ComplaintDetail
import com.example.data.ComplaintDocument
import com.example.data.ComplaintEntry
import com.example.data.Converters
import com.example.ui.theme.*

// Routes
object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val CATEGORIES = "categories"
    const val FORM = "form/{categoryId}"
}

val categoriesMap = mapOf(
    "cm_window" to Pair("CM Window", "🏛️"),
    "acs_complaints" to Pair("ACS Complaints", "⚡"),
    "pm_portal" to Pair("PM Portal", "🇮🇳"),
    "smgt_complaint" to Pair("SMGT Complaint", "🔧"),
    "samadhaan_shivir" to Pair("Samadhaan Shivir", "🤝")
)

@Composable
fun AppNavigation(viewModel: AppViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onGoToCategories = { navController.navigate(Routes.CATEGORIES) },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.CATEGORIES) {
            val allComplaints by viewModel.allComplaints.collectAsStateWithLifecycle()
            CategoriesScreen(
                allComplaints = allComplaints,
                onCategoryClick = { catId -> navController.navigate("form/$catId") },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.FORM) { backStackEntry ->
            val catId = backStackEntry.arguments?.getString("categoryId") ?: ""
            val allComplaints by viewModel.allComplaints.collectAsStateWithLifecycle()
            val categoryComplaints = allComplaints.filter { it.categoryKey == catId }.sortedByDescending { it.createdAt }

            FormScreen(
                categoryId = catId,
                complaints = categoryComplaints,
                onBack = { navController.popBackStack() },
                onSubmit = { no, status, text, docs ->
                    viewModel.insertComplaint(catId, no, status, text, docs)
                },
                onResolve = { viewModel.resolveComplaint(it) },
                onDelete = { viewModel.deleteComplaint(it.id) },
                onAddDetail = { entry, text, docs ->
                    viewModel.addDetail(entry, text, docs)
                }
            )
        }
    }
}

@Composable
fun MainBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(DarkSurface, DarkBackground, DarkSurfaceVariant),
                    radius = 1500f
                )
            )
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    MainBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .border(1.dp, BorderColor, RoundedCornerShape(32.dp)),
                color = Color.White.copy(alpha = 0.06f)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Administration Login",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentYellow,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "DHBVN · SECURE ACCESS",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    OutlinedTextField(
                        value = userId,
                        onValueChange = { userId = it },
                        label = { Text("USER ID") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentYellow,
                            unfocusedBorderColor = BorderColor,
                            focusedContainerColor = Color.White.copy(alpha = 0.05f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("PASSWORD") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentYellow,
                            unfocusedBorderColor = BorderColor,
                            focusedContainerColor = Color.White.copy(alpha = 0.05f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Button(
                        onClick = {
                            if (userId == "DHBVN" && password == "DHBVN") {
                                onLoginSuccess()
                            } else {
                                showError = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                    ) {
                        Text("🔐 Login", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    if (showError) {
                        Text(
                            text = "❌ Invalid ID or Password",
                            color = DangerRed,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }

            Text(
                text = "✦ Developed by Rakesh Kumar ✦",
                color = AccentYellow,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 32.dp)
            )
        }
    }
}

@Composable
fun DashboardScreen(onGoToCategories: () -> Unit, onLogout: () -> Unit) {
    MainBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🏛️ Administration Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "DHBVN · COMPLAINT MANAGEMENT SYSTEM",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            Button(
                onClick = onGoToCategories,
                modifier = Modifier.fillMaxWidth().height(80.dp),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentYellow, contentColor = Color.Black)
            ) {
                Text("📋 Running Complaint Status", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
            ) {
                Text("🚪 Logout", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun CategoriesScreen(
    allComplaints: List<ComplaintEntry>,
    onCategoryClick: (String) -> Unit,
    onBack: () -> Unit
) {
    MainBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📂 Select Category",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                OutlinedButton(onClick = onBack) {
                    Text("← Dashboard")
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(categoriesMap.keys.toList()) { key ->
                    val pair = categoriesMap[key]!!
                    val count = allComplaints.count { it.categoryKey == key }
                    CategoryCard(
                        title = pair.first,
                        icon = pair.second,
                        count = count,
                        onClick = { onCategoryClick(key) }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryCard(title: String, icon: String, count: Int, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, BorderColor, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.04f)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 40.sp, modifier = Modifier.padding(end = 16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
            Surface(
                color = AccentYellow.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "$count entries",
                    color = AccentYellow,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    categoryId: String,
    complaints: List<ComplaintEntry>,
    onBack: () -> Unit,
    onSubmit: (String, String, String, List<ComplaintDocument>) -> Unit,
    onResolve: (ComplaintEntry) -> Unit,
    onDelete: (ComplaintEntry) -> Unit,
    onAddDetail: (ComplaintEntry, String, List<ComplaintDocument>) -> Unit
) {
    val categoryName = categoriesMap[categoryId]?.first ?: "Category"
    val categoryIcon = categoriesMap[categoryId]?.second ?: "📂"

    var complaintNo by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Pending") }
    var complaintText by remember { mutableStateOf("") }
    var selectedDocs by remember { mutableStateOf<List<ComplaintDocument>>(emptyList()) }
    val statuses = listOf("Pending", "In Progress", "Under Review", "Resolved")

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        val docs = uris.map { ComplaintDocument(uri = it.toString(), name = "File", type = "image") }
        selectedDocs = selectedDocs + docs
    }

    MainBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$categoryIcon $categoryName",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                OutlinedButton(onClick = onBack) {
                    Text("← Categories")
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Form Card
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White.copy(alpha = 0.04f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            OutlinedTextField(
                                value = complaintNo,
                                onValueChange = { complaintNo = it },
                                label = { Text("Complaint No.") },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                shape = RoundedCornerShape(12.dp)
                            )
                            
                            // A simple row for Status selection instead of Dropdown for brevity
                            Text("Status", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                statuses.forEach { s ->
                                    FilterChip(
                                        selected = status == s,
                                        onClick = { status = s },
                                        label = { Text(s) }
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = complaintText,
                                onValueChange = { complaintText = it },
                                label = { Text("Complaint Details") },
                                modifier = Modifier.fillMaxWidth().height(120.dp).padding(vertical = 16.dp),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Upload Documents", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                                Button(onClick = { filePicker.launch("image/*") }) {
                                    Text("📁 Choose Image")
                                }
                            }

                            if (selectedDocs.isNotEmpty()) {
                                Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    selectedDocs.forEach { doc ->
                                        AsyncImage(
                                            model = Uri.parse(doc.uri),
                                            contentDescription = null,
                                            modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (complaintNo.isNotBlank() && complaintText.isNotBlank()) {
                                            onSubmit(complaintNo, status, complaintText, selectedDocs)
                                            complaintNo = ""
                                            complaintText = ""
                                            status = "Pending"
                                            selectedDocs = emptyList()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                                ) {
                                    Text("✅ Submit")
                                }
                                OutlinedButton(
                                    onClick = {
                                        complaintNo = ""
                                        complaintText = ""
                                        status = "Pending"
                                        selectedDocs = emptyList()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("↺ Reset")
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "📋 Entries (${complaints.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(complaints) { entry ->
                    EntryCard(
                        entry = entry,
                        onResolve = { onResolve(entry) },
                        onDelete = { onDelete(entry) },
                        onAddDetail = { text, docs -> onAddDetail(entry, text, docs) }
                    )
                }
            }
        }
    }
}

@Composable
fun EntryCard(
    entry: ComplaintEntry,
    onResolve: () -> Unit,
    onDelete: () -> Unit,
    onAddDetail: (String, List<ComplaintDocument>) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    val docs = remember(entry.documentsJson) {
        Converters.docAdapter.fromJson(entry.documentsJson) ?: emptyList()
    }
    val details = remember(entry.detailsJson) {
        Converters.detailAdapter.fromJson(entry.detailsJson) ?: emptyList()
    }
    
    val containerColor = if (entry.resolved) PrimaryTeal.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.04f)
    val borderColor = if (entry.resolved) PrimaryTeal.copy(alpha = 0.2f) else BorderColor

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📌 ${entry.complaintNo}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Surface(
                        color = if (entry.resolved) PrimaryTeal.copy(alpha = 0.15f) else AccentYellow.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = if (entry.resolved) "${entry.status} ✅" else entry.status,
                            color = if (entry.resolved) PrimaryTeal else AccentYellow,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Button(
                    onClick = { isExpanded = !isExpanded },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isExpanded) "📋 Hide Details" else "📋 Details")
                }
            }

            // Body
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                    Divider(color = BorderColor)
                    
                    // Complaint Text
                    Surface(
                        color = Color.White.copy(alpha = 0.03f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp)) {
                            Box(modifier = Modifier.width(4.dp).height(24.dp).background(AccentYellow))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "📝 ${entry.complaintText}", color = TextSecondary)
                        }
                    }

                    // Documents
                    if (docs.isNotEmpty()) {
                        Text(
                            text = "ATTACHED DOCUMENTS",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            docs.forEach { doc ->
                                AsyncImage(
                                    model = Uri.parse(doc.uri),
                                    contentDescription = null,
                                    modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    // Details History
                    if (details.isNotEmpty()) {
                        Text(
                            text = "DETAIL HISTORY (${details.size})",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                        details.forEach { detail ->
                            Surface(
                                color = Color.White.copy(alpha = 0.03f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            ) {
                                Row(modifier = Modifier.padding(12.dp)) {
                                    Box(modifier = Modifier.width(4.dp).height(24.dp).background(PurpleDark))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(text = detail.text, color = TextPrimary)
                                        if (detail.documents.isNotEmpty()) {
                                            Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                detail.documents.forEach { doc ->
                                                    AsyncImage(
                                                        model = Uri.parse(doc.uri),
                                                        contentDescription = null,
                                                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Add Detail Form
                    var newDetailText by remember { mutableStateOf("") }
                    var newDetailDocs by remember { mutableStateOf<List<ComplaintDocument>>(emptyList()) }
                    val detailFilePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
                        val newDocs = uris.map { ComplaintDocument(uri = it.toString(), name = "File", type = "image") }
                        newDetailDocs = newDetailDocs + newDocs
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.03f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            OutlinedTextField(
                                value = newDetailText,
                                onValueChange = { newDetailText = it },
                                placeholder = { Text("Add more detail...") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { detailFilePicker.launch("image/*") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                                ) {
                                    Text("📎 " + if (newDetailDocs.isNotEmpty()) "${newDetailDocs.size} file(s)" else "Upload")
                                }
                                Button(
                                    onClick = {
                                        if (newDetailText.isNotBlank()) {
                                            onAddDetail(newDetailText, newDetailDocs)
                                            newDetailText = ""
                                            newDetailDocs = emptyList()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PurpleDark)
                                ) {
                                    Text("➕ Add Detail")
                                }
                            }
                        }
                    }

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onDelete,
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                        ) {
                            Text("🗑️ Delete")
                        }
                        if (!entry.resolved) {
                            Button(
                                onClick = onResolve,
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                            ) {
                                Text("✅ Resolve")
                            }
                        }
                    }
                }
            }
        }
    }
}
