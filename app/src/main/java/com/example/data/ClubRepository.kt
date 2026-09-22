package com.example.data

import com.example.data.dao.ClubDao
import com.example.data.model.ActivityDebtStatus
import com.example.data.model.ActivityEntity
import com.example.data.model.ClubSettingsEntity
import com.example.data.model.MemberActivityCrossRef
import com.example.data.model.MemberEntity
import com.example.data.model.MemberWithDebtAlert
import com.example.data.model.PaymentEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ClubRepository(private val dao: ClubDao) {

    val allActivities: Flow<List<ActivityEntity>> = dao.getAllActivities()
    val activeActivities: Flow<List<ActivityEntity>> = dao.getActiveActivities()
    val allMembers: Flow<List<MemberEntity>> = dao.getAllMembers()
    val allEnrollments: Flow<List<MemberActivityCrossRef>> = dao.getAllEnrollments()
    val allPayments: Flow<List<PaymentEntity>> = dao.getAllPayments()
    val clubSettings: Flow<ClubSettingsEntity?> = dao.getClubSettings()

    fun getPaymentsForPeriod(month: Int, year: Int): Flow<List<PaymentEntity>> {
        return dao.getPaymentsForPeriod(month, year)
    }

    /**
     * Combina miembros, actividades inscriptas y pagos de un mes/año específico
     * para calcular con precisión en tiempo real quién debe qué actividad y el total adeudado.
     */
    fun getMembersWithDebtStatus(month: Int, year: Int): Flow<List<MemberWithDebtAlert>> {
        return combine(
            dao.getAllMembers(),
            dao.getAllActivities(),
            dao.getAllEnrollments(),
            dao.getPaymentsForPeriod(month, year)
        ) { members, activities, enrollments, payments ->
            val activitiesMap = activities.associateBy { it.id }
            val enrollmentsByMember = enrollments.groupBy { it.memberId }
            val paymentsByMemberAndActivity = payments.associateBy { "${it.memberId}_${it.activityId}" }

            members.map { member ->
                val memberEnrollments = enrollmentsByMember[member.id].orEmpty()
                val enrolledActivities = memberEnrollments.mapNotNull { activitiesMap[it.activityId] }

                val activityStatuses = enrolledActivities.map { activity ->
                    val paymentKey = "${member.id}_${activity.id}"
                    val payment = paymentsByMemberAndActivity[paymentKey]
                    ActivityDebtStatus(
                        activity = activity,
                        isPaid = payment != null,
                        paidPayment = payment
                    )
                }

                MemberWithDebtAlert(
                    member = member,
                    enrolledActivities = enrolledActivities,
                    activityStatuses = activityStatuses
                )
            }
        }
    }

    // Operaciones de Actividades
    suspend fun insertActivity(activity: ActivityEntity): Long = dao.insertActivity(activity)
    suspend fun updateActivity(activity: ActivityEntity) = dao.updateActivity(activity)
    suspend fun deleteActivity(activity: ActivityEntity) = dao.deleteActivity(activity)

    // Operaciones de Socios
    suspend fun insertMember(member: MemberEntity, selectedActivityIds: List<Long>): Long {
        val memberId = dao.insertMember(member)
        selectedActivityIds.forEach { activityId ->
            dao.insertEnrollment(MemberActivityCrossRef(memberId, activityId))
        }
        return memberId
    }

    suspend fun updateMember(member: MemberEntity, selectedActivityIds: List<Long>) {
        dao.updateMember(member)
        dao.deleteAllEnrollmentsForMember(member.id)
        selectedActivityIds.forEach { activityId ->
            dao.insertEnrollment(MemberActivityCrossRef(member.id, activityId))
        }
    }

    suspend fun deleteMember(member: MemberEntity) = dao.deleteMember(member)

    // Operaciones de Inscripción
    suspend fun enrollMember(memberId: Long, activityId: Long) {
        dao.insertEnrollment(MemberActivityCrossRef(memberId, activityId))
    }

    suspend fun unenrollMember(memberId: Long, activityId: Long) {
        dao.deleteEnrollment(memberId, activityId)
    }

    // Operaciones de Pagos
    suspend fun registerPayment(payment: PaymentEntity): Long = dao.insertPayment(payment)
    suspend fun deletePayment(payment: PaymentEntity) = dao.deletePayment(payment)

    // Configuración
    suspend fun saveClubSettings(settings: ClubSettingsEntity) = dao.saveClubSettings(settings)
}
