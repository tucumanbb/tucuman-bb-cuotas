package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.ActivityEntity
import com.example.data.model.ClubSettingsEntity
import com.example.data.model.MemberEntity
import com.example.data.model.MemberWithDebtAlert
import com.example.data.model.PaymentEntity
import com.example.ui.ClubViewModel
import com.example.ui.DebtFilter
import com.example.ui.components.formatCurrency
import com.example.ui.components.shareTextIntent
import com.example.ui.theme.TucumanGreen
import com.example.ui.theme.TucumanNavy
import com.example.ui.theme.TucumanOrange
import com.example.ui.theme.TucumanRed
import com.example.ui.theme.TucumanSky
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: ClubViewModel,
    onNavigateToMembers: (DebtFilter) -> Unit,
    onNavigateToActivities: () -> Unit,
    onOpenRegisterPayment: (MemberWithDebtAlert) -> Unit,
    onOpenNewMember: () -> Unit,
    onOpenNewActivity: () -> Unit,
    onViewReceipt: (MemberEntity, ActivityEntity, PaymentEntity) -> Unit
) {
    val context = LocalContext.current
    val period by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val stats by viewModel.monthlyStats.collectAsStateWithLifecycle()
    val settings by viewModel.clubSettings.collectAsStateWithLifecycle()
    val membersWithDebt by viewModel.rawMembersWithDebt.collectAsStateWithLifecycle()
    val activities by viewModel.activities.collectAsStateWithLifecycle()
    val allPayments by viewModel.allPayments.collectAsStateWithLifecycle()

    val debtors = remember(membersWithDebt) {
        membersWithDebt.filter { it.hasDebt }
    }

    val activitiesMap = remember(activities) {
        activities.associateBy { it.id }
    }

    val membersMap = remember(membersWithDebt) {
        membersWithDebt.associate { it.member.id to it.member }
    }

    val recentPayments = remember(allPayments) {
        allPayments.take(6)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header con Banner del Club Tucumán BB
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_club_hero),
                        contentDescription = "Club Tucumán BB Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        TucumanNavy.copy(alpha = 0.88f)
                                    )
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .padding(2.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.img_club_logo),
                                        contentDescription = "Escudo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        settings.clubName,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 19.sp
                                    )
                                    Text(
                                        "Gestión de Cuotas & Actividades",
                                        color = Color(0xFFE2E8F0),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selector de Período (Mes y Año)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.previousMonth() },
                        modifier = Modifier.testTag("prev_month_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Mes Anterior")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            period.displayString,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { viewModel.nextMonth() },
                        modifier = Modifier.testTag("next_month_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Mes Siguiente")
                    }
                }
            }
        }

        // Tarjetas de Métricas / KPI del Mes
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Cobrado
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = TucumanGreen.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = TucumanGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cobrado", fontSize = 12.sp, color = TucumanGreen, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            formatCurrency(stats.totalCollected),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = TucumanGreen
                        )
                        Text(
                            "${stats.upToDateCount} socios al día",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Deuda pendiente
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToMembers(DebtFilter.WITH_DEBT) },
                    colors = CardDefaults.cardColors(containerColor = TucumanRed.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = TucumanRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Por Cobrar", fontSize = 12.sp, color = TucumanRed, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            formatCurrency(stats.totalPendingDebt),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = TucumanRed
                        )
                        Text(
                            "${stats.debtorsCount} con deuda pendiente",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ALERTA DE DEUDA DESTACADA
        item {
            if (debtors.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = TucumanRed.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, TucumanRed.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = TucumanRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "ALERTAS DE CUOTAS PENDIENTES",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TucumanRed,
                                    fontSize = 13.sp
                                )
                            }
                            Text(
                                "${debtors.size} socios",
                                fontWeight = FontWeight.Bold,
                                color = TucumanRed,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Los siguientes socios deben cuotas de actividades en ${period.displayString}:",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            debtors.take(3).forEach { debtor ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(debtor.member.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        val owedNames = debtor.owedActivities.joinToString(", ") { it.name }
                                        Text(
                                            "Debe: $owedNames",
                                            fontSize = 11.sp,
                                            color = TucumanRed,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            formatCurrency(debtor.totalOwed),
                                            fontWeight = FontWeight.Bold,
                                            color = TucumanRed,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Button(
                                            onClick = { onOpenRegisterPayment(debtor) },
                                            colors = ButtonDefaults.buttonColors(containerColor = TucumanGreen),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text("Cobrar", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }

                        if (debtors.size > 3) {
                            Spacer(modifier = Modifier.height(6.dp))
                            TextButton(
                                onClick = { onNavigateToMembers(DebtFilter.WITH_DEBT) },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Ver todos los ${debtors.size} socios deudores →", fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = TucumanGreen.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = TucumanGreen,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "¡Todas las cuotas al día!",
                                fontWeight = FontWeight.Bold,
                                color = TucumanGreen
                            )
                            Text(
                                "No hay deudas pendientes registradas en ${period.displayString}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Botones de Acceso Rápido
        item {
            Text("Acciones Rápidas", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenNewMember,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_new_member_button")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Nuevo Socio", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onOpenNewActivity,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_new_activity_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Actividad", fontSize = 12.sp)
                }
            }
        }

        // Historial Reciente de Pagos
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Últimos Pagos Registrados",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "${recentPayments.size} pagos",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (recentPayments.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Text(
                        "Aún no hay pagos registrados en este período.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(recentPayments) { payment ->
                val member = membersMap[payment.memberId]
                val activity = activitiesMap[payment.activityId]

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (member != null && activity != null) {
                                onViewReceipt(member, activity, payment)
                            }
                        },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                member?.fullName ?: "Socio #${payment.memberId}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    activity?.name ?: "Actividad",
                                    fontSize = 12.sp,
                                    color = TucumanSky,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(" • ", fontSize = 12.sp)
                                Text(payment.paymentMethod, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                payment.receiptNumber,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                formatCurrency(payment.amountPaid),
                                fontWeight = FontWeight.ExtraBold,
                                color = TucumanGreen,
                                fontSize = 15.sp
                            )
                            IconButton(
                                onClick = {
                                    if (member != null && activity != null) {
                                        val receiptText = viewModel.generateReceiptText(member, activity, payment)
                                        shareTextIntent(context, receiptText, "Compartir Recibo ${payment.receiptNumber}")
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = "Compartir Comprobante",
                                    tint = TucumanSky,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
