package com.safehill.kclient.api.dtos

import kotlinx.serialization.Serializable

@Serializable
data class SHUserInputDTO(
    val identifier: String,
    val publicKey: String,
    val publicSignature: String,
    val name: String
) {}