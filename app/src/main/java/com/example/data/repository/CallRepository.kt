package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.CallDirection
import com.example.data.model.CallLogEntity
import com.example.data.model.CallType
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class CallRepository(private val database: AppDatabase) {
    private val callLogDao = database.callLogDao()

    val callLogsFlow: Flow<List<CallLogEntity>> = callLogDao.getCallLogsFlow()

    suspend fun recordCall(
        contactId: String,
        contactName: String,
        contactAvatar: String,
        callType: CallType,
        direction: CallDirection,
        durationSeconds: Int
    ) {
        val log = CallLogEntity(
            id = "call_${UUID.randomUUID()}",
            contactId = contactId,
            contactName = contactName,
            contactAvatar = contactAvatar,
            callType = callType.name,
            direction = direction.name,
            timestamp = System.currentTimeMillis(),
            durationSeconds = durationSeconds
        )
        callLogDao.insertCallLog(log)
    }

    suspend fun clearHistory() {
        callLogDao.clearAllCallLogs()
    }
}
