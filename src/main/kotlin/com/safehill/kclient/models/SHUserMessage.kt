package com.safehill.kclient.models

interface SHUserMessage {
    val interactionId: String?
    val senderUserIdentifier: String?
    val inReplyToAssetGlobalIdentifier: String?
    val inReplyToInteractionId: String?
    val encryptedMessage: String // base64EncodedData with the cipher
    val createdAt: String? // ISO8601 formatted datetime
}
