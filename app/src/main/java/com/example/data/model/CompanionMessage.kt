package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "companion_messages")
data class CompanionMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val isUser: Boolean,
    val timestamp: String,
    val proposalTitle: String? = null,
    val proposalDetails: String? = null,
    val proposalApplied: Boolean = false,
    val planType: String? = null,    // "DAILY_PLAN", "READING_PLAN", "ROUTINE_PROPOSAL", "CLARIFICATION"
    val planPayload: String? = null, // JSON representation of the structured proposal
    val planStatus: String = "IDLE"  // "PENDING", "ACCEPTED", "EDITED", "CANCELLED", "IDLE"
)

