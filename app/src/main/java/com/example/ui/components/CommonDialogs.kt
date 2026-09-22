package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActivityEntity
import com.example.data.model.ClubSettingsEntity
import com.example.data.model.MemberEntity
import com.example.data.model.MemberWithDebtAlert
import com.example.data.model.PaymentEntity
import com.example.ui.PeriodSelection
import com.example.ui.theme.TucumanGreen
import com.example.ui.theme.TucumanNavy
import com.example.ui.theme.TucumanOrange
import com.example.ui.theme.TucumanRed
import com.example.ui.theme.TucumanSky
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Función utilitaria para compartir texto mediante Intent (WhatsApp, Gmail, etc.)
 */
fun shareTextIntent(context: Context, text: String, title: String = "Compartir desde Club Tucumán BB") {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, title)
    context.startActivity(shareIntent)
}

fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "AR"))
    format.maximumFractionDigits = 0
    return format.format(amount)
}

/**
 * Diálogo para Agregar o Editar una Actividad con su precio independiente
 */
@Composable
fun AddEditActivityDialog(
    initialActivity: ActivityEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (ActivityEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialActivity?.name ?: "") }
    var description by remember { mutableStateOf(initialActivity?.description ?: "") }
    var monthlyFeeStr by remember { mutableStateOf(initialActivity?.monthlyFee?.toInt()?.toString() ?: "15000") }
    var category by remember { mutableStateOf(initialActivity?.category ?: "Básquet") }
    var schedule by remember { mutableStateOf(initialActivity?.schedule ?: "") }
    var selectedColor by remember { mutableStateOf(initialActivity?.colorHex ?: "#EA580C") }

    val categories = listOf("Básquet", "Gimnasio", "Salón", "General")
    val colorOptions = listOf("#EA580C", "#0284C7", "#8B5CF6", "#EC4899", "#16A34A", "#EAB308", "#64748B")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (initialActivity == null) "Nueva Actividad deportiva/social" else "Editar Actividad",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la Actividad *") },
                    placeholder = { Text("Ej: Básquetbol Juvenil, Yoga, Musculación") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("activity_name_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = monthlyFeeStr,
                    onValueChange = { monthlyFeeStr = it.filter { char -> char.isDigit() } },
                    label = { Text("Precio Cuota Mensual ($ ARS) *") },
                    placeholder = { Text("Ej: 18000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("activity_fee_input"),
                    singleLine = true
                )

                Text("Categoría:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 12.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = schedule,
                    onValueChange = { schedule = it },
                    label = { Text("Días y Horarios") },
                    placeholder = { Text("Ej: Lun, Mié y Vie 19:00 a 20:30 hs") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción / Ubicación") },
                    placeholder = { Text("Ej: Cancha central, Salón planta alta, Gimnasio") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                Text("Color identificador:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colorOptions.forEach { hex ->
                        val color = try {
                            Color(android.graphics.Color.parseColor(hex))
                        } catch (e: Exception) {
                            TucumanOrange
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (selectedColor == hex) 3.dp else 1.dp,
                                    color = if (selectedColor == hex) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedColor == hex) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Seleccionado",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val fee = monthlyFeeStr.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && fee > 0) {
                        onConfirm(
                            ActivityEntity(
                                id = initialActivity?.id ?: 0,
                                name = name.trim(),
                                description = description.trim(),
                                monthlyFee = fee,
                                category = category,
                                schedule = schedule.trim(),
                                colorHex = selectedColor,
                                isActive = true
                            )
                        )
                    }
                },
                modifier = Modifier.testTag("save_activity_button")
            ) {
                Text("Guardar Actividad")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Diálogo para Agregar o Editar un Socio y vincularlo a una o más actividades
 */
@Composable
fun AddEditMemberDialog(
    initialMember: MemberEntity? = null,
    initialSelectedActivityIds: List<Long> = emptyList(),
    availableActivities: List<ActivityEntity>,
    onDismiss: () -> Unit,
    onConfirm: (MemberEntity, List<Long>) -> Unit
) {
    var fullName by remember { mutableStateOf(initialMember?.fullName ?: "") }
    var dni by remember { mutableStateOf(initialMember?.dni ?: "") }
    var phone by remember { mutableStateOf(initialMember?.phone ?: "") }
    var email by remember { mutableStateOf(initialMember?.email ?: "") }
    var category by remember { mutableStateOf(initialMember?.category ?: "Socio Activo") }
    var notes by remember { mutableStateOf(initialMember?.notes ?: "") }

    val selectedActivityIds = remember {
        mutableStateListOf<Long>().apply {
            addAll(initialSelectedActivityIds)
        }
    }

    val categories = listOf("Socio Activo", "Infantil", "Juvenil", "Primera", "Socio Pleno")

    val totalMonthlyFee = availableActivities
        .filter { selectedActivityIds.contains(it.id) }
        .sumOf { it.monthlyFee }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (initialMember == null) "Nuevo Socio del Club" else "Editar Socio",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Nombre y Apellido *") },
                    placeholder = { Text("Ej: Lucas Martínez") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_name_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = dni,
                    onValueChange = { dni = it.filter { char -> char.isDigit() } },
                    label = { Text("DNI / Documento *") },
                    placeholder = { Text("Ej: 38456123") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_dni_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono / WhatsApp") },
                    placeholder = { Text("Ej: +54 381 511-2233") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo Electrónico (Gmail)") },
                    placeholder = { Text("Ej: socio@gmail.com") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Categoría:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Actividades a las que se inscribe:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Cada actividad tiene su cuota mensual independiente:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (availableActivities.isEmpty()) {
                    Text(
                        "No hay actividades creadas. Crea al menos una actividad primero.",
                        color = TucumanRed,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            availableActivities.forEach { activity ->
                                val isChecked = selectedActivityIds.contains(activity.id)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isChecked) {
                                                selectedActivityIds.remove(activity.id)
                                            } else {
                                                selectedActivityIds.add(activity.id)
                                            }
                                        }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            if (checked) selectedActivityIds.add(activity.id)
                                            else selectedActivityIds.remove(activity.id)
                                        }
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            activity.name,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            formatCurrency(activity.monthlyFee) + "/mes",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Total mensual proyectado
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Cuota total mensual:",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            formatCurrency(totalMonthlyFee),
                            fontWeight = FontWeight.Bold,
                            color = TucumanOrange,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas / Observaciones") },
                    placeholder = { Text("Ej: Apto médico al día, turno mañana") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.isNotBlank() && dni.isNotBlank()) {
                        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                        onConfirm(
                            MemberEntity(
                                id = initialMember?.id ?: 0,
                                fullName = fullName.trim(),
                                dni = dni.trim(),
                                phone = phone.trim(),
                                email = email.trim(),
                                category = category,
                                joinDate = initialMember?.joinDate ?: currentDate,
                                notes = notes.trim(),
                                isActive = true
                            ),
                            selectedActivityIds.toList()
                        )
                    }
                },
                modifier = Modifier.testTag("save_member_button")
            ) {
                Text("Guardar Socio")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Diálogo para Registrar el Pago de una Cuota
 */
@Composable
fun RegisterPaymentDialog(
    memberWithDebt: MemberWithDebtAlert,
    period: PeriodSelection,
    onDismiss: () -> Unit,
    onConfirm: (activityId: Long, amount: Double, method: String, notes: String) -> Unit
) {
    val pendingActivities = memberWithDebt.owedActivities
    var selectedActivityId by remember {
        mutableStateOf(pendingActivities.firstOrNull()?.id ?: 0L)
    }

    val selectedActivity = pendingActivities.find { it.id == selectedActivityId }
        ?: memberWithDebt.enrolledActivities.find { it.id == selectedActivityId }

    var amountStr by remember(selectedActivity) {
        mutableStateOf(selectedActivity?.monthlyFee?.toInt()?.toString() ?: "0")
    }

    var paymentMethod by remember { mutableStateOf("Efectivo") }
    var notes by remember { mutableStateOf("") }

    val methods = listOf("Efectivo", "Transferencia", "Mercado Pago", "Débito")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.MonetizationOn,
                    contentDescription = null,
                    tint = TucumanGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Registrar Pago de Cuota", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            memberWithDebt.member.fullName,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "DNI: ${memberWithDebt.member.dni} • Período: ${period.displayString}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Text("Seleccionar Actividad a Abonar:", style = MaterialTheme.typography.labelMedium)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val activitiesToShow = if (pendingActivities.isNotEmpty()) pendingActivities else memberWithDebt.enrolledActivities
                    activitiesToShow.forEach { act ->
                        val isSelected = selectedActivityId == act.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedActivityId = act.id
                                    amountStr = act.monthlyFee.toInt().toString()
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected)
                                    MaterialTheme.colorScheme.secondaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = if (isSelected)
                                borderStroke(TucumanOrange)
                            else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(act.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        "Cuota mensual asignada",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    formatCurrency(act.monthlyFee),
                                    fontWeight = FontWeight.Bold,
                                    color = TucumanOrange
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it.filter { c -> c.isDigit() } },
                    label = { Text("Monto a Cobrar ($ ARS)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_amount_input"),
                    singleLine = true
                )

                Text("Medio de Pago:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    methods.forEach { method ->
                        FilterChip(
                            selected = paymentMethod == method,
                            onClick = { paymentMethod = method },
                            label = { Text(method, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Nota / Referencia de comprobante") },
                    placeholder = { Text("Ej: Pago en caja de administración, transf. Galicia") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    if (selectedActivityId > 0 && amount > 0) {
                        onConfirm(selectedActivityId, amount, paymentMethod, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TucumanGreen),
                modifier = Modifier.testTag("confirm_payment_button")
            ) {
                Text("Confirmar y Cobrar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Diálogo con el Comprobante Oficial de Pago para visualizar y compartir vía WhatsApp/Email
 */
@Composable
fun ViewReceiptDialog(
    member: MemberEntity,
    activity: ActivityEntity,
    payment: PaymentEntity,
    settings: ClubSettingsEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "AR"))
    val formattedDate = dateFormat.format(Date(payment.paymentDate))
    val period = PeriodSelection(payment.periodMonth, payment.periodYear)

    val receiptText = remember {
        """
            🏀 *${settings.clubName}*
            📄 *COMPROBANTE OFICIAL DE PAGO*
            ------------------------------------
            🔖 *Nº Recibo:* ${payment.receiptNumber}
            👤 *Socio:* ${member.fullName}
            🪪 *DNI:* ${member.dni}
            📅 *Período:* ${period.displayString}
            🎯 *Actividad:* ${activity.name}
            💵 *Monto Abonado:* ${formatCurrency(payment.amountPaid)}
            💳 *Medio de Pago:* ${payment.paymentMethod}
            🕒 *Fecha:* $formattedDate
            ------------------------------------
            📌 *Sede:* ${settings.address}
            📧 *Contacto:* ${settings.officialGmail}
            ¡Muchas gracias por apoyar al básquet y al club!
        """.trimIndent()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Comprobante de Pago", fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                settings.clubName,
                                fontWeight = FontWeight.Bold,
                                color = TucumanNavy,
                                fontSize = 16.sp
                            )
                            Text(
                                payment.receiptNumber,
                                fontWeight = FontWeight.Bold,
                                color = TucumanOrange
                            )
                        }

                        Divider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Socio:", style = MaterialTheme.typography.bodySmall)
                            Text(member.fullName, fontWeight = FontWeight.SemiBold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("DNI:", style = MaterialTheme.typography.bodySmall)
                            Text(member.dni)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Período abonado:", style = MaterialTheme.typography.bodySmall)
                            Text(period.displayString, fontWeight = FontWeight.SemiBold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Actividad:", style = MaterialTheme.typography.bodySmall)
                            Text(activity.name, fontWeight = FontWeight.SemiBold, color = TucumanSky)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Medio de pago:", style = MaterialTheme.typography.bodySmall)
                            Text(payment.paymentMethod)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Fecha y Hora:", style = MaterialTheme.typography.bodySmall)
                            Text(formattedDate, fontSize = 12.sp)
                        }

                        Divider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "TOTAL ABONADO:",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                formatCurrency(payment.amountPaid),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = TucumanGreen
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        shareTextIntent(
                            context = context,
                            text = receiptText,
                            title = "Compartir Comprobante ${payment.receiptNumber}"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("share_receipt_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = TucumanSky)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Compartir por WhatsApp / Email")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Listo")
            }
        }
    )
}

/**
 * Diálogo para ver en detalle la alerta de deuda de un socio
 * y enviar aviso de cobro directo a su WhatsApp o registrar el pago
 */
@Composable
fun DebtDetailDialog(
    memberWithDebt: MemberWithDebtAlert,
    period: PeriodSelection,
    onDismiss: () -> Unit,
    onRegisterPayment: () -> Unit,
    onSendAlertMessage: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = TucumanRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Alerta de Deuda", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    memberWithDebt.member.fullName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "DNI: ${memberWithDebt.member.dni} • Período: ${period.displayString}",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Detalle de actividades inscriptas:",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelMedium
                )

                memberWithDebt.activityStatuses.forEach { status ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (status.isPaid)
                                TucumanGreen.copy(alpha = 0.1f)
                            else
                                TucumanRed.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(status.activity.name, fontWeight = FontWeight.Medium)
                                Text(
                                    if (status.isPaid) "✅ Pagado" else "⚠️ PENDIENTE DE PAGO",
                                    color = if (status.isPaid) TucumanGreen else TucumanRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                formatCurrency(status.activity.monthlyFee),
                                fontWeight = FontWeight.Bold,
                                color = if (status.isPaid) TucumanGreen else TucumanRed
                            )
                        }
                    }
                }

                Divider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("TOTAL ADEUDADO:", fontWeight = FontWeight.Bold)
                    Text(
                        formatCurrency(memberWithDebt.totalOwed),
                        fontWeight = FontWeight.ExtraBold,
                        color = TucumanRed,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = onSendAlertMessage,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Avisar deuda por WhatsApp")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onRegisterPayment()
                },
                colors = ButtonDefaults.buttonColors(containerColor = TucumanGreen)
            ) {
                Icon(Icons.Default.MonetizationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Cobrar Cuota")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

private fun borderStroke(color: Color) = androidx.compose.foundation.BorderStroke(2.dp, color)
