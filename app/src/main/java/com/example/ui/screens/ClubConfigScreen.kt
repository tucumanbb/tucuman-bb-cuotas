package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ClubSettingsEntity
import com.example.ui.ClubViewModel
import com.example.ui.components.formatCurrency
import com.example.ui.components.shareTextIntent
import com.example.ui.theme.TucumanGreen
import com.example.ui.theme.TucumanNavy
import com.example.ui.theme.TucumanOrange
import com.example.ui.theme.TucumanSky
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ClubConfigScreen(viewModel: ClubViewModel) {
    val context = LocalContext.current
    val settings by viewModel.clubSettings.collectAsStateWithLifecycle()
    val rawMembers by viewModel.rawMembersWithDebt.collectAsStateWithLifecycle()
    val activities by viewModel.activities.collectAsStateWithLifecycle()
    val allPayments by viewModel.allPayments.collectAsStateWithLifecycle()

    var clubName by remember(settings) { mutableStateOf(settings.clubName) }
    var officialGmail by remember(settings) { mutableStateOf(settings.officialGmail) }
    var phone by remember(settings) { mutableStateOf(settings.phone) }
    var address by remember(settings) { mutableStateOf(settings.address) }
    var cbuAlias by remember(settings) { mutableStateOf(settings.cbuAlias) }
    var savedSuccess by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("club_config_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tarjeta de Identidad y Configuración del Club
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(TucumanOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.SportsBasketball,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Datos Institucionales de Tucumán BB",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                "Estos datos se imprimen en los recibos y avisos de cuota",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedTextField(
                        value = clubName,
                        onValueChange = { clubName = it; savedSuccess = false },
                        label = { Text("Nombre del Club") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = officialGmail,
                        onValueChange = { officialGmail = it; savedSuccess = false },
                        label = { Text("Cuenta de Gmail Oficial del Club") },
                        placeholder = { Text("tucumanbb.cuotas@gmail.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("club_gmail_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = cbuAlias,
                        onValueChange = { cbuAlias = it; savedSuccess = false },
                        label = { Text("Alias CBU / CVU para Transferencias") },
                        placeholder = { Text("TUCUMAN.BB.OFICIAL") },
                        leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it; savedSuccess = false },
                        label = { Text("Teléfono / WhatsApp de Administración") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it; savedSuccess = false },
                        label = { Text("Dirección de la Sede") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            viewModel.updateClubSettings(
                                settings.copy(
                                    clubName = clubName.trim(),
                                    officialGmail = officialGmail.trim(),
                                    phone = phone.trim(),
                                    address = address.trim(),
                                    cbuAlias = cbuAlias.trim()
                                )
                            )
                            savedSuccess = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_club_settings_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = TucumanNavy)
                    ) {
                        Icon(
                            if (savedSuccess) Icons.Default.Check else Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (savedSuccess) "¡Datos Guardados con Éxito!" else "Guardar Cambios")
                    }
                }
            }
        }

        // Guía de Organización Profesional (Gmail, Git, VS Code, Supabase/Vercel)
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.HelpOutline,
                            contentDescription = null,
                            tint = TucumanSky,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Guía de Implementación Profesional",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    // Paso 1: Cuenta de Gmail
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "1. Cuenta de Gmail Exclusiva para el Club",
                                fontWeight = FontWeight.Bold,
                                color = TucumanNavy,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "• Crea una cuenta como 'tucumanbb.oficial@gmail.com' o 'tucumanbb.cuotas@gmail.com'.\n" +
                                        "• Activa la verificación en 2 pasos y guarda la clave de recuperación con la comisión directiva.\n" +
                                        "• Usa esta cuenta para centralizar: Google Drive (respaldos), recepción de comprobantes de transferencia y GitHub del club.",
                                style = MaterialTheme.typography.bodySmall,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    // Paso 2: VS Code y Git
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp), tint = TucumanOrange)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "2. Configuración en Visual Studio Code y Git",
                                    fontWeight = FontWeight.Bold,
                                    color = TucumanNavy,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Para separar tu correo personal del club en tu computadora:\n" +
                                        "En la terminal de VS Code dentro de la carpeta del proyecto ejecuta:",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = TucumanNavy,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "git config user.name \"Tucuman BB\"\ngit config user.email \"$officialGmail\"",
                                    color = Color(0xFF38BDF8),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Esto asegura que los commits se registren exclusivamente con la identidad del club y no con tu correo personal.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Paso 3: Supabase y Vercel vs Room Local
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "3. Recomendación de Arquitectura Gratuita",
                                fontWeight = FontWeight.Bold,
                                color = TucumanNavy,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "• Esta app móvil usa Room Database (SQLite local): es 100% gratuita, funciona sin internet en la secretaría del club y no requiere pagar servidores ni mantenimiento.\n" +
                                        "• Si más adelante quieres sincronización web en tiempo real: Supabase ofrece un plan gratuito (Free Tier) con PostgreSQL y Vercel permite publicar el portal web sin costo alguno.",
                                style = MaterialTheme.typography.bodySmall,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // Copia de Seguridad y Exportación a Gmail/Drive
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Backup,
                            contentDescription = null,
                            tint = TucumanGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Copia de Seguridad y Respaldo",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Text(
                        "Genera un informe completo en texto con todos los socios, actividades y pagos para enviar directamente por Gmail o guardar en Google Drive.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "AR"))
                            val dateStr = dateFormat.format(Date())

                            val backupContent = buildString {
                                appendLine("🏀 BACKUP Y REPORTE - ${settings.clubName}")
                                appendLine("Fecha: $dateStr")
                                appendLine("Email administrativo: ${settings.officialGmail}")
                                appendLine("==========================================")
                                appendLine()
                                appendLine("📋 ACTIVIDADES (${activities.size}):")
                                activities.forEach { act ->
                                    appendLine("  • ${act.name} | Cat: ${act.category} | Cuota: ${formatCurrency(act.monthlyFee)}")
                                }
                                appendLine()
                                appendLine("👥 SOCIOS Y ESTADO (${rawMembers.size}):")
                                rawMembers.forEach { m ->
                                    val status = if (m.hasDebt) "⚠️ DEBE: ${m.owedActivities.joinToString { it.name }} (${formatCurrency(m.totalOwed)})" else "✅ AL DÍA"
                                    appendLine("  • ${m.member.fullName} | DNI: ${m.member.dni} | ${m.member.phone} | $status")
                                }
                                appendLine()
                                appendLine("💰 TOTAL PAGOS REGISTRADOS: ${allPayments.size}")
                                val total = allPayments.sumOf { it.amountPaid }
                                appendLine("Monto Histórico Total: ${formatCurrency(total)}")
                            }

                            shareTextIntent(
                                context = context,
                                text = backupContent,
                                title = "Copia de Seguridad Club Tucumán BB"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_backup_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = TucumanGreen)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Exportar Respaldo (Gmail / Drive / WhatsApp)")
                    }
                }
            }
        }
    }
}
