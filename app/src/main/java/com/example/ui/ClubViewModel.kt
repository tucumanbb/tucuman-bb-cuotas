package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ClubRepository
import com.example.data.model.ActivityEntity
import com.example.data.model.ClubSettingsEntity
import com.example.data.model.MemberEntity
import com.example.data.model.MemberWithDebtAlert
import com.example.data.model.PaymentEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class DebtFilter {
    ALL,
    WITH_DEBT,
    UP_TO_DATE
}

data class PeriodSelection(
    val month: Int, // 1 to 12
    val year: Int
) {
    val monthName: String
        get() {
            val names = listOf(
                "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
            )
            return names.getOrElse(month - 1) { "Mes $month" }
        }

    val displayString: String
        get() = "$monthName $year"
}

class ClubViewModel(private val repository: ClubRepository) : ViewModel() {

    private val cal = Calendar.getInstance()
    private val initialMonth = cal.get(Calendar.MONTH) + 1
    private val initialYear = cal.get(Calendar.YEAR)

    private val _selectedPeriod = MutableStateFlow(PeriodSelection(initialMonth, initialYear))
    val selectedPeriod: StateFlow<PeriodSelection> = _selectedPeriod.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _debtFilter = MutableStateFlow(DebtFilter.ALL)
    val debtFilter: StateFlow<DebtFilter> = _debtFilter.asStateFlow()

    private val _selectedActivityFilter = MutableStateFlow<Long?>(null) // null = all activities
    val selectedActivityFilter: StateFlow<Long?> = _selectedActivityFilter.asStateFlow()

    // Actividades
    val activities: StateFlow<List<ActivityEntity>> = repository.allActivities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Configuración del club
    val clubSettings: StateFlow<ClubSettingsEntity> = repository.clubSettings
        .combine(MutableStateFlow(Unit)) { settings, _ ->
            settings ?: ClubSettingsEntity()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ClubSettingsEntity()
        )

    // Lista de socios con cálculo de alerta de deudas para el período seleccionado
    val rawMembersWithDebt: StateFlow<List<MemberWithDebtAlert>> = _selectedPeriod
        .flatMapLatest { period ->
            repository.getMembersWithDebtStatus(period.month, period.year)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Lista filtrada para la pantalla de socios y deudas
    val filteredMembers: StateFlow<List<MemberWithDebtAlert>> = combine(
        rawMembersWithDebt,
        _searchQuery,
        _debtFilter,
        _selectedActivityFilter
    ) { members, query, filter, activityId ->
        members.filter { memberWithDebt ->
            // Filtro por texto (Nombre o DNI)
            val matchesQuery = query.isBlank() ||
                    memberWithDebt.member.fullName.contains(query, ignoreCase = true) ||
                    memberWithDebt.member.dni.contains(query, ignoreCase = true)

            // Filtro por estado de deuda
            val matchesDebt = when (filter) {
                DebtFilter.ALL -> true
                DebtFilter.WITH_DEBT -> memberWithDebt.hasDebt
                DebtFilter.UP_TO_DATE -> !memberWithDebt.hasDebt && memberWithDebt.enrolledActivities.isNotEmpty()
            }

            // Filtro por actividad específica
            val matchesActivity = activityId == null ||
                    memberWithDebt.enrolledActivities.any { it.id == activityId }

            matchesQuery && matchesDebt && matchesActivity
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Historial de todos los pagos
    val allPayments: StateFlow<List<PaymentEntity>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Estadísticas del mes actual seleccionado
    val monthlyStats = combine(rawMembersWithDebt, activities) { members, actList ->
        val activeMembers = members.filter { it.member.isActive }
        val totalMembers = activeMembers.size
        val debtors = activeMembers.filter { it.hasDebt }
        val upToDate = activeMembers.filter { !it.hasDebt && it.enrolledActivities.isNotEmpty() }

        val totalCollected = activeMembers.flatMap { it.activityStatuses }
            .filter { it.isPaid }
            .sumOf { it.activity.monthlyFee }

        val totalPendingDebt = activeMembers.sumOf { it.totalOwed }

        val totalProjected = totalCollected + totalPendingDebt

        DashboardStats(
            totalMembers = totalMembers,
            debtorsCount = debtors.size,
            upToDateCount = upToDate.size,
            totalCollected = totalCollected,
            totalPendingDebt = totalPendingDebt,
            totalProjected = totalProjected
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    // Métodos para cambiar filtros
    fun setPeriod(month: Int, year: Int) {
        _selectedPeriod.value = PeriodSelection(month, year)
    }

    fun previousMonth() {
        val curr = _selectedPeriod.value
        if (curr.month == 1) {
            _selectedPeriod.value = PeriodSelection(12, curr.year - 1)
        } else {
            _selectedPeriod.value = PeriodSelection(curr.month - 1, curr.year)
        }
    }

    fun nextMonth() {
        val curr = _selectedPeriod.value
        if (curr.month == 12) {
            _selectedPeriod.value = PeriodSelection(1, curr.year + 1)
        } else {
            _selectedPeriod.value = PeriodSelection(curr.month + 1, curr.year)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setDebtFilter(filter: DebtFilter) {
        _debtFilter.value = filter
    }

    fun setActivityFilter(activityId: Long?) {
        _selectedActivityFilter.value = activityId
    }

    // Acciones de Actividades
    fun addActivity(activity: ActivityEntity) {
        viewModelScope.launch {
            repository.insertActivity(activity)
        }
    }

    fun updateActivity(activity: ActivityEntity) {
        viewModelScope.launch {
            repository.updateActivity(activity)
        }
    }

    fun deleteActivity(activity: ActivityEntity) {
        viewModelScope.launch {
            repository.deleteActivity(activity)
        }
    }

    // Acciones de Socios
    fun addMember(member: MemberEntity, activityIds: List<Long>) {
        viewModelScope.launch {
            repository.insertMember(member, activityIds)
        }
    }

    fun updateMember(member: MemberEntity, activityIds: List<Long>) {
        viewModelScope.launch {
            repository.updateMember(member, activityIds)
        }
    }

    fun deleteMember(member: MemberEntity) {
        viewModelScope.launch {
            repository.deleteMember(member)
        }
    }

    // Acciones de Pagos
    fun registerFeePayment(
        memberId: Long,
        activityId: Long,
        amount: Double,
        paymentMethod: String,
        notes: String
    ) {
        viewModelScope.launch {
            val period = _selectedPeriod.value
            val time = System.currentTimeMillis()
            val settings = clubSettings.value
            val receiptNum = "${settings.receiptPrefix}-${period.year}-${(time % 10000).toString().padStart(4, '0')}"

            val payment = PaymentEntity(
                memberId = memberId,
                activityId = activityId,
                periodMonth = period.month,
                periodYear = period.year,
                amountPaid = amount,
                paymentDate = time,
                paymentMethod = paymentMethod,
                receiptNumber = receiptNum,
                notes = notes
            )
            repository.registerPayment(payment)
        }
    }

    fun deletePayment(payment: PaymentEntity) {
        viewModelScope.launch {
            repository.deletePayment(payment)
        }
    }

    // Configuración del Club
    fun updateClubSettings(settings: ClubSettingsEntity) {
        viewModelScope.launch {
            repository.saveClubSettings(settings)
        }
    }

    // Generador de comprobante en texto para WhatsApp o Email
    fun generateReceiptText(
        member: MemberEntity,
        activity: ActivityEntity,
        payment: PaymentEntity
    ): String {
        val settings = clubSettings.value
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "AR"))
        val formattedDate = dateFormat.format(Date(payment.paymentDate))
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "AR"))
        val formattedAmount = currencyFormat.format(payment.amountPaid)
        val period = PeriodSelection(payment.periodMonth, payment.periodYear)

        return """
            🏀 *${settings.clubName}*
            📄 *COMPROBANTE OFICIAL DE PAGO*
            ------------------------------------
            🔖 *Nº Recibo:* ${payment.receiptNumber}
            👤 *Socio:* ${member.fullName}
            🪪 *DNI:* ${member.dni}
            📅 *Período:* ${period.displayString}
            🎯 *Actividad:* ${activity.name}
            💵 *Monto Abonado:* $formattedAmount
            💳 *Medio de Pago:* ${payment.paymentMethod}
            🕒 *Fecha:* $formattedDate
            ------------------------------------
            📌 *Sede:* ${settings.address}
            📧 *Contacto:* ${settings.officialGmail}
            ¡Muchas gracias por su cuota y compromiso con el club!
        """.trimIndent()
    }

    // Generador de mensaje de Alerta de Deuda para WhatsApp o Email
    fun generateDebtAlertMessage(memberWithDebt: MemberWithDebtAlert): String {
        val settings = clubSettings.value
        val period = _selectedPeriod.value
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "AR"))
        val totalFormatted = currencyFormat.format(memberWithDebt.totalOwed)

        val activitiesList = memberWithDebt.owedActivities.joinToString("\n") { act ->
            "  • ${act.name}: ${currencyFormat.format(act.monthlyFee)}"
        }

        return """
            🏀 *${settings.clubName} - Administración de Cuotas*
            Hola ${memberWithDebt.member.fullName}, le recordamos el estado de su cuota social:

            📅 *Período:* ${period.displayString}
            ⚠️ *Actividades pendientes:*
            $activitiesList

            💰 *Total a regularizar:* $totalFormatted

            🏦 *Alias para transferencias:*
            ${settings.cbuAlias}

            📧 *Envío de comprobante a:*
            ${settings.officialGmail}

            ¡Gracias por formar parte de la familia de Tucumán BB!
        """.trimIndent()
    }
}

data class DashboardStats(
    val totalMembers: Int = 0,
    val debtorsCount: Int = 0,
    val upToDateCount: Int = 0,
    val totalCollected: Double = 0.0,
    val totalPendingDebt: Double = 0.0,
    val totalProjected: Double = 0.0
)

class ClubViewModelFactory(private val repository: ClubRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClubViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClubViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
