package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ActivityEntity
import com.example.data.model.MemberEntity
import com.example.data.model.PaymentEntity
import com.example.ui.ClubViewModel
import com.example.ui.components.formatCurrency
import com.example.ui.components.shareTextIntent
import com.example.ui.theme.TucumanGreen
import com.example.ui.theme.TucumanOrange
import com.example.ui.theme.TucumanRed
import com.example.ui.theme.TucumanSky
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PaymentsHistoryScreen(
    viewModel: ClubViewModel,
    onViewReceipt: (MemberEntity, ActivityEntity, PaymentEntity) -> Unit
) {
    val context = LocalContext.current
    val allPayments by viewModel.allPayments.collectAsStateWithLifecycle()
    val rawMembers by viewModel.rawMembersWithDebt.collectAsStateWithLifecycle()
    val activities by viewModel.activities.collectAsStateWithLifecycle()

    var paymentSearch by remember { mutableStateOf("") }
    var paymentToDelete by remember { mutableStateOf<PaymentEntity?>(null) }

    val membersMap = remember(rawMembers) {
        rawMembers.associate { it.member.id to it.member }
    }
    val activitiesMap = remember(activities) {
        activities.associateBy { it.id }
    }

    val filteredPayments = remember(allPayments, paymentSearch, membersMap) {
        allPayments.filter { payment ->
            if (paymentSearch.isBlank()) return@filter true
            val member = membersMap[payment.memberId]
            val memberName = member?.fullName.orEmpty()
            val receipt = payment.receiptNumber
            val method = payment.paymentMethod
            memberName.contains(paymentSearch, ignoreCase = true) ||
                    receipt.contains(paymentSearch, ignoreCase = true) ||
                    method.contains(paymentSearch, ignoreCase = true)
        }
    }

    val totalCollected = remember(filteredPayments) {
        filteredPayments.sumOf { it.amountPaid }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("payments_history_screen")
    ) {
        // Resumen total y buscador
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Historial de Recibos y Cuotas",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "${filteredPayments.size} pagos registrados",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "Total Filtrado",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            formatCurrency(totalCollected),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = TucumanGreen
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = paymentSearch,
            onValueChange = { paymentSearch = it },
            placeholder = { Text("Buscar por socio, Nº recibo o medio de pago...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (filteredPayments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No se encontraron pagos registrados",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredPayments) { payment ->
                    val member = membersMap[payment.memberId]
                    val activity = activitiesMap[payment.activityId]
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "AR"))
                    val formattedDate = dateFormat.format(Date(payment.paymentDate))

                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (member != null && activity != null) {
                                    onViewReceipt(member, activity, payment)
                                }
                            },
                        shape = RoundedCornerShape(10.dp),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = TucumanOrange.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                payment.receiptNumber,
                                                fontWeight = FontWeight.Bold,
                                                color = TucumanOrange,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "Mes ${payment.periodMonth}/${payment.periodYear}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        member?.fullName ?: "Socio #${payment.memberId}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        "Actividad: ${activity?.name ?: "Actividad"}",
                                        fontSize = 12.sp,
                                        color = TucumanSky,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        formatCurrency(payment.amountPaid),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = TucumanGreen
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            payment.paymentMethod,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    formattedDate,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row {
                                    IconButton(
                                        onClick = {
                                            if (member != null && activity != null) {
                                                val receiptText = viewModel.generateReceiptText(member, activity, payment)
                                                shareTextIntent(
                                                    context,
                                                    receiptText,
                                                    "Comprobante ${payment.receiptNumber}"
                                                )
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

                                    IconButton(
                                        onClick = { paymentToDelete = payment },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Anular Pago",
                                            tint = TucumanRed,
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
    }

    // Diálogo para anular pago
    paymentToDelete?.let { payment ->
        AlertDialog(
            onDismissRequest = { paymentToDelete = null },
            title = { Text("Anular Comprobante de Pago") },
            text = {
                Text("¿Estás seguro de anular el comprobante ${payment.receiptNumber} por ${formatCurrency(payment.amountPaid)}? El socio volverá a figurar con deuda pendiente.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePayment(payment)
                        paymentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TucumanRed)
                ) {
                    Text("Anular Pago")
                }
            },
            dismissButton = {
                TextButton(onClick = { paymentToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
