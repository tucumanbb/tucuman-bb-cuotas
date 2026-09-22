package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ActivityEntity
import com.example.data.model.ClubSettingsEntity
import com.example.data.model.MemberActivityCrossRef
import com.example.data.model.MemberEntity
import com.example.data.model.PaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClubDao {

    // --- ACTIVIDADES ---
    @Query("SELECT * FROM activities ORDER BY name ASC")
    fun getAllActivities(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveActivities(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE id = :id LIMIT 1")
    suspend fun getActivityById(id: Long): ActivityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityEntity): Long

    @Update
    suspend fun updateActivity(activity: ActivityEntity)

    @Delete
    suspend fun deleteActivity(activity: ActivityEntity)

    // --- SOCIOS ---
    @Query("SELECT * FROM members ORDER BY fullName ASC")
    fun getAllMembers(): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members WHERE id = :id LIMIT 1")
    suspend fun getMemberById(id: Long): MemberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: MemberEntity): Long

    @Update
    suspend fun updateMember(member: MemberEntity)

    @Delete
    suspend fun deleteMember(member: MemberEntity)

    // --- INSCRIPCIONES A ACTIVIDADES ---
    @Query("SELECT * FROM member_activity_cross_ref")
    fun getAllEnrollments(): Flow<List<MemberActivityCrossRef>>

    @Query("""
        SELECT a.* FROM activities a 
        INNER JOIN member_activity_cross_ref x ON a.id = x.activityId 
        WHERE x.memberId = :memberId AND a.isActive = 1
    """)
    fun getActivitiesForMember(memberId: Long): Flow<List<ActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnrollment(crossRef: MemberActivityCrossRef)

    @Query("DELETE FROM member_activity_cross_ref WHERE memberId = :memberId")
    suspend fun deleteAllEnrollmentsForMember(memberId: Long)

    @Query("DELETE FROM member_activity_cross_ref WHERE memberId = :memberId AND activityId = :activityId")
    suspend fun deleteEnrollment(memberId: Long, activityId: Long)

    // --- PAGOS ---
    @Query("SELECT * FROM payments ORDER BY paymentDate DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE periodMonth = :month AND periodYear = :year ORDER BY paymentDate DESC")
    fun getPaymentsForPeriod(month: Int, year: Int): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE memberId = :memberId ORDER BY paymentDate DESC")
    fun getPaymentsForMember(memberId: Long): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Delete
    suspend fun deletePayment(payment: PaymentEntity)

    // --- CONFIGURACIÓN DEL CLUB ---
    @Query("SELECT * FROM club_settings WHERE id = 1 LIMIT 1")
    fun getClubSettings(): Flow<ClubSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveClubSettings(settings: ClubSettingsEntity)
}
