package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Representa una actividad deportiva o social del Club Tucumán BB.
 * Básquetbol como principal, Gimnasio, Salón (Yoga, Aeróbic, etc.)
 * y cualquier actividad personalizada que se agregue.
 */
@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val monthlyFee: Double,
    val category: String = "Básquet", // Básquet, Gimnasio, Salón, General
    val schedule: String = "", // Ej: "Lunes, Miércoles y Viernes 19:00"
    val colorHex: String = "#EA580C",
    val isActive: Boolean = true
)

/**
 * Socio del club Tucumán BB.
 */
@Entity(
    tableName = "members",
    indices = [Index(value = ["dni"], unique = true)]
)
data class MemberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val dni: String,
    val phone: String = "",
    val email: String = "",
    val category: String = "Activo", // Infantil, Juvenil, Primera, Socio Pleno, Vitalicio
    val joinDate: String = "",
    val notes: String = "",
    val isActive: Boolean = true
)

/**
 * Relación muchos a muchos: Un socio puede estar inscripto en una o varias actividades
 * con cuotas independientes.
 */
@Entity(
    tableName = "member_activity_cross_ref",
    primaryKeys = ["memberId", "activityId"],
    foreignKeys = [
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ActivityEntity::class,
            parentColumns = ["id"],
            childColumns = ["activityId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["memberId"]),
        Index(value = ["activityId"])
    ]
)
data class MemberActivityCrossRef(
    val memberId: Long,
    val activityId: Long,
    val enrollmentDate: String = ""
)

/**
 * Registro individual del pago de una cuota por actividad y período.
 */
@Entity(
    tableName = "payments",
    indices = [
        Index(value = ["memberId", "activityId", "periodMonth", "periodYear"], unique = true),
        Index(value = ["paymentDate"])
    ]
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val memberId: Long,
    val activityId: Long,
    val periodMonth: Int, // 1 to 12
    val periodYear: Int,  // e.g. 2026
    val amountPaid: Double,
    val paymentDate: Long = System.currentTimeMillis(),
    val paymentMethod: String = "Efectivo", // Efectivo, Transferencia, Débito, Mercado Pago
    val receiptNumber: String = "",
    val notes: String = ""
)

/**
 * Configuración general del Club Tucumán BB (Cuenta Gmail, CBU, etc.)
 */
@Entity(tableName = "club_settings")
data class ClubSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val clubName: String = "Club Tucumán BB",
    val officialGmail: String = "tucumanbbcuotas@gmail.com",
    val phone: String = "+54 381 423-1234",
    val address: String = "Suipacha 1160, San Miguel de Tucumán",
    val cbuAlias: String = "TUCUMAN.BB.OFICIAL",
    val receiptPrefix: String = "TBB"
)

/**
 * Modelos de vista para la UI y cálculo de alertas de deudas
 */
data class ActivityDebtStatus(
    val activity: ActivityEntity,
    val isPaid: Boolean,
    val paidPayment: PaymentEntity? = null
)

data class MemberWithDebtAlert(
    val member: MemberEntity,
    val enrolledActivities: List<ActivityEntity>,
    val activityStatuses: List<ActivityDebtStatus>
) {
    val totalOwed: Double
        get() = activityStatuses.filter { !it.isPaid }.sumOf { it.activity.monthlyFee }

    val hasDebt: Boolean
        get() = activityStatuses.any { !it.isPaid }

    val owedActivities: List<ActivityEntity>
        get() = activityStatuses.filter { !it.isPaid }.map { it.activity }

    val paidActivities: List<ActivityEntity>
        get() = activityStatuses.filter { it.isPaid }.map { it.activity }
}
