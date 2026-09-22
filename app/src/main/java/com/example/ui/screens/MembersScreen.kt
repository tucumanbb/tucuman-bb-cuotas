package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ActivityEntity
import com.example.data.model.MemberEntity
import com.example.data.model.MemberWithDebtAlert
import com.example.ui.ClubViewModel
import com.example.ui.DebtFilter
import com.example.ui.components.formatCurrency
import com.example.ui.components.shareTextIntent
import com.example.ui.theme.TucumanGreen
import com.example.ui.theme.TucumanNavy
import com.example.ui.theme.TucumanOrange
import com.example.ui.theme.TucumanRed
import com.example.ui.theme.TucumanSky

@Composable
fun MembersScreen(
    viewModel: ClubViewModel,
    onOpenAddMember: () -> Unit,
    onOpenEditMember: (MemberEntity, List<Long>) -> Unit,
    onOpenRegisterPayment: (MemberWithDebtAlert) -> Unit,
    onOpenDebtDetail: (MemberWithDebtAlert) -> Unit
) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val debtFilter by viewModel.debtFilter.collectAsStateWithLifecycle()
    val activityFilter by viewModel.selectedActivityFilter.collectAsStateWithLifecycle()
    val members by viewModel.filteredMembers.collectAsStateWithLifecycle()
    val activities by viewModel.activities.collectAsStateWithLifecycle()
    val period by viewModel.selectedPeriod.collectAsStateWithLifecycle()

    var memberToDelete by remember { mutableStateOf<MemberEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenAddMember,
                containerColor = TucumanOrange,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_member")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Nuevo Socio")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("members_screen")
        ) {
            // Buscador de socios por Nombre o DNI
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Buscar socio por Nombre o DNI...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_member_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Chips de filtro por Deuda (Todos, Con Deuda, Al Día)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = debtFilter == DebtFilter.ALL,
                    onClick = { viewModel.setDebtFilter(DebtFilter.ALL) },
                    label = { Text("Todos los socios") }
                )
                FilterChip(
                    selected = debtFilter == DebtFilter.WITH_DEBT,
                    onClick = { viewModel.setDebtFilter(DebtFilter.WITH_DEBT) },
                    label = { Text("⚠️ Con Deuda") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = TucumanRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
                FilterChip(
                    selected = debtFilter == DebtFilter.UP_TO_DATE,
                    onClick = { viewModel.setDebtFilter(DebtFilter.UP_TO_DATE) },
                    label = { Text("✅ Al Día") }
                )
            }

            // Chips de filtro por Actividad
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = activityFilter == null,
                    onClick = { viewModel.setActivityFilter(null) },
                    label = { Text("Todas las actividades", fontSize = 12.sp) }
                )
                activities.forEach { act ->
                    FilterChip(
                        selected = activityFilter == act.id,
                        onClick = {
                            viewModel.setActivityFilter(if (activityFilter == act.id) null else act.id)
                        },
                        label = { Text(act.name, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Lista de Socios
            if (members.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No se encontraron socios con este filtro",
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = onOpenAddMember) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Registrar Primer Socio")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(members) { memberWithDebt ->
                        MemberCard(
                            memberWithDebt = memberWithDebt,
                            onCobrar = { onOpenRegisterPayment(memberWithDebt) },
                            onVerDetalle = { onOpenDebtDetail(memberWithDebt) },
                            onEditar = {
                                onOpenEditMember(
                                    memberWithDebt.member,
                                    memberWithDebt.enrolledActivities.map { it.id }
                                )
                            },
                            onEliminar = { memberToDelete = memberWithDebt.member },
                            onEnviarAviso = {
                                val msg = viewModel.generateDebtAlertMessage(memberWithDebt)
                                shareTextIntent(
                                    context,
                                    msg,
                                    "Aviso de Cuota Tucumán BB para ${memberWithDebt.member.fullName}"
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    // Diálogo de confirmación para eliminar socio
    memberToDelete?.let { member ->
        AlertDialog(
            onDismissRequest = { memberToDelete = null },
            title = { Text("Eliminar Socio") },
            text = {
                Text("¿Estás seguro de que deseas eliminar a ${member.fullName}? Se eliminarán también sus registros de cuotas.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMember(member)
                        memberToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TucumanRed)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { memberToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun MemberCard(
    memberWithDebt: MemberWithDebtAlert,
    onCobrar: () -> Unit,
    onVerDetalle: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    onEnviarAviso: () -> Unit
) {
    val member = memberWithDebt.member
    val hasDebt = memberWithDebt.hasDebt

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Fila superior: Nombre, DNI y Opciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        member.fullName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "DNI: ${member.dni}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(" • ", fontSize = 12.sp)
                        Text(
                            member.category,
                            fontSize = 12.sp,
                            color = TucumanSky,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (member.phone.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                member.phone,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row {
                    IconButton(onClick = onEditar, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onEliminar, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = TucumanRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Actividades inscriptas con precios individuales
            Text(
                "Actividades inscriptas:",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))

            if (memberWithDebt.enrolledActivities.isEmpty()) {
                Text(
                    "Sin actividades asignadas. Toca en editar para asignar.",
                    fontSize = 12.sp,
                    color = TucumanRed
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    memberWithDebt.activityStatuses.forEach { status ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (status.isPaid)
                                TucumanGreen.copy(alpha = 0.12f)
                            else
                                TucumanRed.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (status.isPaid) TucumanGreen.copy(alpha = 0.4f) else TucumanRed.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    status.activity.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    formatCurrency(status.activity.monthlyFee),
                                    fontSize = 11.sp,
                                    color = if (status.isPaid) TucumanGreen else TucumanRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider()
            Spacer(modifier = Modifier.height(10.dp))

            // ALERTA DE DEUDA O ESTADO AL DÍA
            if (hasDebt) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onVerDetalle() },
                    colors = CardDefaults.cardColors(containerColor = TucumanRed.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TucumanRed.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = TucumanRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    "DEBE: ${memberWithDebt.owedActivities.joinToString(", ") { it.name }}",
                                    fontWeight = FontWeight.Bold,
                                    color = TucumanRed,
                                    fontSize = 12.sp
                                )
                                Text(
                                    "Deuda total: ${formatCurrency(memberWithDebt.totalOwed)}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = onCobrar,
                            colors = ButtonDefaults.buttonColors(containerColor = TucumanGreen),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cobrar", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onEnviarAviso) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Avisar por WhatsApp", fontSize = 11.sp)
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(TucumanGreen.copy(alpha = 0.12f))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = TucumanGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Al día en todas sus actividades",
                            fontWeight = FontWeight.Bold,
                            color = TucumanGreen,
                            fontSize = 13.sp
                        )
                    }
                    Text(
                        "Cuota abonada",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
