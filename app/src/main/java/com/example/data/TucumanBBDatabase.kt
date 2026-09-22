package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.ClubDao
import com.example.data.model.ActivityEntity
import com.example.data.model.ClubSettingsEntity
import com.example.data.model.MemberActivityCrossRef
import com.example.data.model.MemberEntity
import com.example.data.model.PaymentEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ActivityEntity::class,
        MemberEntity::class,
        MemberActivityCrossRef::class,
        PaymentEntity::class,
        ClubSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TucumanBBDatabase : RoomDatabase() {

    abstract fun clubDao(): ClubDao

    companion object {
        @Volatile
        private var INSTANCE: TucumanBBDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): TucumanBBDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TucumanBBDatabase::class.java,
                    "tucuman_bb_club.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.clubDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: ClubDao) {
            // Configuración inicial del club
            dao.saveClubSettings(
                ClubSettingsEntity(
                    id = 1,
                    clubName = "Club Tucumán BB",
                    officialGmail = "tucumanbbcuotas@gmail.com",
                    phone = "+54 381 423-1234",
                    address = "Suipacha 1160, San Miguel de Tucumán",
                    cbuAlias = "TUCUMAN.BB.OFICIAL",
                    receiptPrefix = "TBB"
                )
            )

            // Actividades iniciales del Club Tucumán BB
            val basquetId = dao.insertActivity(
                ActivityEntity(
                    name = "Básquetbol Formativo / Primera",
                    description = "Entrenamiento en cancha de parqué oficial y competencia asociativa",
                    monthlyFee = 18000.0,
                    category = "Básquet",
                    schedule = "Lun, Mié y Vie 19:30 a 21:30 hs",
                    colorHex = "#EA580C"
                )
            )

            val gimnasioId = dao.insertActivity(
                ActivityEntity(
                    name = "Gimnasio de Musculación & Cardio",
                    description = "Acceso libre a máquinas de fuerza, mancuernas y cintas",
                    monthlyFee = 15000.0,
                    category = "Gimnasio",
                    schedule = "Lun a Sáb 07:00 a 22:00 hs",
                    colorHex = "#0284C7"
                )
            )

            val yogaId = dao.insertActivity(
                ActivityEntity(
                    name = "Salón - Yoga & Flexibilidad",
                    description = "Clases guiadas en el salón multiuso del 1er piso",
                    monthlyFee = 12000.0,
                    category = "Salón",
                    schedule = "Mar y Jue 18:00 a 19:15 hs",
                    colorHex = "#8B5CF6"
                )
            )

            val aerobicId = dao.insertActivity(
                ActivityEntity(
                    name = "Salón - Aeróbic & Funcional",
                    description = "Entrenamiento aeróbico con música y coreografías",
                    monthlyFee = 12000.0,
                    category = "Salón",
                    schedule = "Lun, Mié y Vie 18:00 a 19:00 hs",
                    colorHex = "#EC4899"
                )
            )

            // Socios de demostración con diferentes actividades
            val socio1 = dao.insertMember(
                MemberEntity(
                    fullName = "Lucas Martínez",
                    dni = "38456123",
                    phone = "+54 381 511-2233",
                    email = "lucas.martinez@gmail.com",
                    category = "Primera División",
                    joinDate = "15/02/2024",
                    notes = "Capitán equipo de primera básquet"
                )
            )
            // Lucas hace Básquet + Gimnasio
            dao.insertEnrollment(MemberActivityCrossRef(socio1, basquetId))
            dao.insertEnrollment(MemberActivityCrossRef(socio1, gimnasioId))

            val socio2 = dao.insertMember(
                MemberEntity(
                    fullName = "Mariana Gómez",
                    dni = "35987456",
                    phone = "+54 381 644-8899",
                    email = "mariana.gomez@gmail.com",
                    category = "Socio Activo",
                    joinDate = "10/05/2024",
                    notes = "Viene turnos tarde"
                )
            )
            // Mariana hace Gimnasio + Yoga
            dao.insertEnrollment(MemberActivityCrossRef(socio2, gimnasioId))
            dao.insertEnrollment(MemberActivityCrossRef(socio2, yogaId))

            val socio3 = dao.insertMember(
                MemberEntity(
                    fullName = "Facundo Albarracín",
                    dni = "42111333",
                    phone = "+54 381 722-3344",
                    email = "facu.alba@gmail.com",
                    category = "U19 Básquet",
                    joinDate = "01/03/2025",
                    notes = "Juvenil"
                )
            )
            // Facundo hace Básquet
            dao.insertEnrollment(MemberActivityCrossRef(socio3, basquetId))

            val socio4 = dao.insertMember(
                MemberEntity(
                    fullName = "Carolina Herrera",
                    dni = "33445566",
                    phone = "+54 381 488-9900",
                    email = "caro.herrera@gmail.com",
                    category = "Socio Activo",
                    joinDate = "20/01/2025",
                    notes = "Clases aeróbic"
                )
            )
            dao.insertEnrollment(MemberActivityCrossRef(socio4, aerobicId))

            // Registramos un pago de prueba para el mes actual (Septiembre 2026)
            // Lucas pagó Básquetbol pero debe Gimnasio -> generará una alerta de deuda específica
            dao.insertPayment(
                PaymentEntity(
                    memberId = socio1,
                    activityId = basquetId,
                    periodMonth = 9,
                    periodYear = 2026,
                    amountPaid = 18000.0,
                    paymentDate = System.currentTimeMillis() - 86400000L * 2,
                    paymentMethod = "Transferencia",
                    receiptNumber = "TBB-2026-0001",
                    notes = "Transferencia a alias TUCUMAN.BB.OFICIAL"
                )
            )

            // Mariana pagó Yoga y Gimnasio -> está 100% al día
            dao.insertPayment(
                PaymentEntity(
                    memberId = socio2,
                    activityId = gimnasioId,
                    periodMonth = 9,
                    periodYear = 2026,
                    amountPaid = 15000.0,
                    paymentDate = System.currentTimeMillis() - 86400000L * 4,
                    paymentMethod = "Efectivo",
                    receiptNumber = "TBB-2026-0002",
                    notes = "Abonó en recepción del club"
                )
            )
            dao.insertPayment(
                PaymentEntity(
                    memberId = socio2,
                    activityId = yogaId,
                    periodMonth = 9,
                    periodYear = 2026,
                    amountPaid = 12000.0,
                    paymentDate = System.currentTimeMillis() - 86400000L * 4,
                    paymentMethod = "Efectivo",
                    receiptNumber = "TBB-2026-0003",
                    notes = "Abonó en recepción del club"
                )
            )
        }
    }
}
