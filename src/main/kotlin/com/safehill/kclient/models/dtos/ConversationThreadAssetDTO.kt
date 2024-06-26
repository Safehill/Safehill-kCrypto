package com.safehill.kclient.models.dtos

import com.safehill.kclient.models.serde.InstantSerializer
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class ConversationThreadAssetDTO(
    val globalIdentifier: String,
    val addedByUserIdentifier: String,
    @Serializable(with = InstantSerializer::class) val addedAt: Instant,
    val groupId: String
)