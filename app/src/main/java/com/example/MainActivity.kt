package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.data.ClubRepository
import com.example.data.TucumanBBDatabase
import com.example.data.model.ActivityEntity
import com.example.data.model.MemberEntity
import com.example.data.model.MemberWithDebtAlert
import com.example.data.model.PaymentEntity
import com.example.ui.ClubViewModel
import com.example.ui.ClubViewModelFactory
import com.example.ui.DebtFilter
import com.example.ui.components.AddEditActivityDialog
import com.example.ui.components.AddEditMemberDialog
import com.example.ui.components.DebtDetailDialog
import com.example.ui.components.RegisterPaymentDialog
import com.example.ui.components.ViewReceiptDialog
import com.example.ui.components.shareTextIntent
import com.example.ui.screens.ActivitiesScreen
import com.example.ui.screens.ClubConfigScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MembersScreen
import com.example.ui.screens.PaymentsHistoryScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TucumanNavy
import com.example.ui.theme.TucumanOrange
import com.example.ui.theme.TucumanRed

enum class ClubTab(val label: String, val icon: ImageVector) {
    DASHBOARD("Inicio", Icons.Default.Dashboard),
    MEMBERS("Socios", Icons.Default.People),
    ACTIVITIES("Actividades", Icons.Default.SportsBasketball),
    PAYMENTS("Recibos", Icons.Default.ReceiptLong),
    CONFIG("Club & Guía", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: ClubViewModel by viewModels {
        val database = TucumanBBDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = ClubRepository(database.clubDao())
        ClubViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ClubApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubApp(viewModel: ClubViewModel) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(ClubTab.DASHBOARD) }

    // Dialog state
    var showAddActivityDialog by remember { mutableStateOf(false) }
    var activityToEdit by remember { mutableStateOf<ActivityEntity?>(null) }

    var showAddMemberDialog by remember { mutableStateOf(false) }
    var memberToEdit by remember { mutableStateOf<Pair<MemberEntity, List<Long>>?>(null) }

    var memberPaying by remember { mutableStateOf<MemberWithDebtAlert?>(null) }
    var viewingReceiptData by remember { mutableStateOf<Triple<MemberEntity, ActivityEntity, PaymentEntity>?>(null) }
    var viewingDebtDetail by remember { mutableStateOf<MemberWithDebtAlert?>(null) }

    val rawMembersWithDebt by viewModel.rawMembersWithDebt.collectAsStateWithLifecycle()
    val activities by viewModel.activities.collectAsStateWithLifecycle()
    val period by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val clubSettings by viewModel.clubSettings.collectAsStateWithLifecycle()

    val debtorsCount = remember(rawMembersWithDebt) {
        rawMembersWithDebt.count { it.hasDebt }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .padding(2.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_club_logo),
                                contentDescription = "Logo Tucumán BB",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            clubSettings.clubName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = TucumanNavy
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("club_bottom_navigation")
            ) {
                ClubTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            if (tab == ClubTab.MEMBERS && debtorsCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = TucumanRed) {
                                            Text(
                                                "$debtorsCount",
                                                color = Color.White,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                ) {
                                    Icon(tab.icon, contentDescription = tab.label)
                                }
                            } else {
                                Icon(tab.icon, contentDescription = tab.label)
                            }
                        },
                        label = {
                            Text(
                                tab.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TucumanNavy,
                            selectedTextColor = TucumanNavy,
                            indicatorColor = TucumanOrange.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                ClubTab.DASHBOARD -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToMembers = { filter ->
                            viewModel.setDebtFilter(filter)
                            selectedTab = ClubTab.MEMBERS
                        },
                        onNavigateToActivities = {
                            selectedTab = ClubTab.ACTIVITIES
                        },
                        onOpenRegisterPayment = { memberWithDebt ->
                            memberPaying = memberWithDebt
                        },
                        onOpenNewMember = {
                            showAddMemberDialog = true
                        },
                        onOpenNewActivity = {
                            showAddActivityDialog = true
                        },
                        onViewReceipt = { member, activity, payment ->
                            viewingReceiptData = Triple(member, activity, payment)
                        }
                    )
                }

                ClubTab.MEMBERS -> {
                    MembersScreen(
                        viewModel = viewModel,
                        onOpenAddMember = { showAddMemberDialog = true },
                        onOpenEditMember = { member, activityIds ->
                            memberToEdit = Pair(member, activityIds)
                        },
                        onOpenRegisterPayment = { memberWithDebt ->
                            memberPaying = memberWithDebt
                        },
                        onOpenDebtDetail = { memberWithDebt ->
                            viewingDebtDetail = memberWithDebt
                        }
                    )
                }

                ClubTab.ACTIVITIES -> {
                    ActivitiesScreen(
                        viewModel = viewModel,
                        onOpenAddActivity = { showAddActivityDialog = true },
                        onOpenEditActivity = { act -> activityToEdit = act }
                    )
                }

                ClubTab.PAYMENTS -> {
                    PaymentsHistoryScreen(
                        viewModel = viewModel,
                        onViewReceipt = { member, activity, payment ->
                            viewingReceiptData = Triple(member, activity, payment)
                        }
                    )
                }

                ClubTab.CONFIG -> {
                    ClubConfigScreen(viewModel = viewModel)
                }
            }
        }
    }

    // --- DIÁLOGOS GLOBALES ---

    // Agregar Nueva Actividad
    if (showAddActivityDialog) {
        AddEditActivityDialog(
            initialActivity = null,
            onDismiss = { showAddActivityDialog = false },
            onConfirm = { newActivity ->
                viewModel.addActivity(newActivity)
                showAddActivityDialog = false
            }
        )
    }

    // Editar Actividad
    activityToEdit?.let { activity ->
        AddEditActivityDialog(
            initialActivity = activity,
            onDismiss = { activityToEdit = null },
            onConfirm = { updated ->
                viewModel.updateActivity(updated)
                activityToEdit = null
            }
        )
    }

    // Agregar Nuevo Socio
    if (showAddMemberDialog) {
        AddEditMemberDialog(
            initialMember = null,
            initialSelectedActivityIds = emptyList(),
            availableActivities = activities,
            onDismiss = { showAddMemberDialog = false },
            onConfirm = { newMember, activityIds ->
                viewModel.addMember(newMember, activityIds)
                showAddMemberDialog = false
            }
        )
    }

    // Editar Socio
    memberToEdit?.let { (member, activityIds) ->
        AddEditMemberDialog(
            initialMember = member,
            initialSelectedActivityIds = activityIds,
            availableActivities = activities,
            onDismiss = { memberToEdit = null },
            onConfirm = { updatedMember, updatedActivityIds ->
                viewModel.updateMember(updatedMember, updatedActivityIds)
                memberToEdit = null
            }
        )
    }

    // Registrar Pago de Cuota
    memberPaying?.let { memberWithDebt ->
        RegisterPaymentDialog(
            memberWithDebt = memberWithDebt,
            period = period,
            onDismiss = { memberPaying = null },
            onConfirm = { activityId, amount, method, notes ->
                viewModel.registerFeePayment(
                    memberId = memberWithDebt.member.id,
                    activityId = activityId,
                    amount = amount,
                    paymentMethod = method,
                    notes = notes
                )
                memberPaying = null
            }
        )
    }

    // Ver Comprobante Oficial y Compartir
    viewingReceiptData?.let { (member, activity, payment) ->
        ViewReceiptDialog(
            member = member,
            activity = activity,
            payment = payment,
            settings = clubSettings,
            onDismiss = { viewingReceiptData = null }
        )
    }

    // Ver Detalle de Alerta de Deuda y Avisar por WhatsApp
    viewingDebtDetail?.let { memberWithDebt ->
        DebtDetailDialog(
            memberWithDebt = memberWithDebt,
            period = period,
            onDismiss = { viewingDebtDetail = null },
            onRegisterPayment = {
                memberPaying = memberWithDebt
            },
            onSendAlertMessage = {
                val msg = viewModel.generateDebtAlertMessage(memberWithDebt)
                shareTextIntent(
                    context = context,
                    text = msg,
                    title = "Recordatorio de Cuota ${clubSettings.clubName}"
                )
            }
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Club Tucumán BB: $name", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme { Greeting("Tucumán BB") }
}
