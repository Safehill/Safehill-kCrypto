package com.safehill.kclient.network

import org.junit.jupiter.api.Test
import com.safehill.kclient.api.SafehillApi
import com.safehill.kclient.models.SHRemoteUser
import com.safehill.kclient.models.SHServerUser
import com.safehill.kclient.models.user.SHLocalUserInterface
import com.safehill.kclient.network.dtos.ConversationThreadOutputDTO
import com.safehill.kclient.network.dtos.RecipientEncryptionDetailsDTO
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`


class ServerProxyTest {
    private lateinit var mockUser: SHLocalUserInterface
    private lateinit var mockRemoteServer: SafehillApi
    private lateinit var mockLocalServer: LocalServerInterface
    private lateinit var serverProxy: ServerProxy

    @BeforeEach
    fun setUp() {
        mockUser = mock(SHLocalUserInterface::class.java)
        mockRemoteServer = mock(SafehillApi::class.java)
        mockLocalServer = mock(LocalServerInterface::class.java)
        serverProxy = ServerProxy(mockUser, mockLocalServer)
        serverProxy.remoteServer = mockRemoteServer
    }

    @Test
    fun `listThreads should return threads from remote server`() = runBlocking {
        // Arrange
        val expectedThreads = listOf(ConversationThreadOutputDTO(
            threadId = "threadId",
            name = null,
            membersPublicIdentifier = listOf(),
            lastUpdatedAt = null,
            encryptionDetails = RecipientEncryptionDetailsDTO(
                recipientUserIdentifier = "",
                ephemeralPublicKey = "",
                encryptedSecret = "",
                secretPublicSignature = "",
                senderPublicSignature = ""
            )
        ))
        `when`(mockRemoteServer.listThreads()).thenReturn(expectedThreads)

        // Act
        val result = serverProxy.listThreads()

        // Assert
        assert(expectedThreads == result)
        verify(mockRemoteServer).listThreads()
        verifyNoInteractions(mockLocalServer)
        verifyNoInteractions(mockUser)
    }

    @Test
    fun `listThreads should return threads from local server when remote server fails`() = runBlocking {
        // Arrange
        val expectedThreads = listOf(ConversationThreadOutputDTO(
            threadId = "threadId",
            name = null,
            membersPublicIdentifier = listOf(),
            lastUpdatedAt = null,
            encryptionDetails = RecipientEncryptionDetailsDTO(
                recipientUserIdentifier = "",
                ephemeralPublicKey = "",
                encryptedSecret = "",
                secretPublicSignature = "",
                senderPublicSignature = ""
            )
        ))
        `when`(mockRemoteServer.listThreads()).thenThrow(IllegalStateException("Remote server error"))
        `when`(mockLocalServer.listThreads()).thenReturn(expectedThreads)

        // Act
        val result = serverProxy.listThreads()

        // Assert
        assert(expectedThreads == result)
        verify(mockRemoteServer).listThreads()
        verify(mockLocalServer).listThreads()
        verifyNoInteractions(mockUser)
    }

    @Test
    fun `test getUsers() with empty userIdentifiersToFetch`() = runBlocking() {
        val result = serverProxy.getUsers(emptyList())

        assert(result == emptyList<SHServerUser>())
    }

    @Test
    fun `test getUsers() with successful remote call`() = runBlocking() {
        val userIdentifiers = listOf("id1", "id2")

        val expectedUsers = listOf(
            SHRemoteUser(identifier = "id1", name = "", publicKeyData = ByteArray(0), publicSignatureData = ByteArray(0)),
            SHRemoteUser(identifier = "id2", name = "", publicKeyData = ByteArray(0), publicSignatureData = ByteArray(0))
        )
        `when`(mockRemoteServer.getUsers(userIdentifiers)).thenReturn(expectedUsers)
        `when`(mockLocalServer.getUsers(userIdentifiers)).thenReturn(expectedUsers)

        val result = serverProxy.getUsers(userIdentifiers)

        verify(mockRemoteServer).getUsers(userIdentifiers)
        verify(mockLocalServer).createOrUpdateUser(identifier = "id1", name = "", publicKeyData = ByteArray(0), publicSignatureData = ByteArray(0))
        verify(mockLocalServer).createOrUpdateUser(identifier = "id2", name = "", publicKeyData = ByteArray(0), publicSignatureData = ByteArray(0))
        verify(mockLocalServer).getUsers(userIdentifiers)
        assert(expectedUsers == result)
    }

    @Test
    fun `test getUsers() with failed remote call and successful local call`() = runBlocking() {
        val userIdentifiers = listOf("id1", "id2")

        val expectedUsers = listOf(
            SHRemoteUser(identifier = "id1", name = "", publicKeyData = ByteArray(0), publicSignatureData = ByteArray(0)),
            SHRemoteUser(identifier = "id2", name = "", publicKeyData = ByteArray(0), publicSignatureData = ByteArray(0))
        )
        `when`(mockRemoteServer.getUsers(userIdentifiers)).thenThrow(IllegalStateException("Test exception"))
        `when`(mockLocalServer.getUsers(userIdentifiers)).thenReturn(expectedUsers)

        val result = serverProxy.getUsers(userIdentifiers)

        assert(expectedUsers == result)
        verify(mockRemoteServer).getUsers(userIdentifiers)
        verify(mockLocalServer, times(0)).createOrUpdateUser(identifier = "id1", name = "", publicKeyData = ByteArray(0), publicSignatureData = ByteArray(0))
        verify(mockLocalServer, times(0)).createOrUpdateUser(identifier = "id2", name = "", publicKeyData = ByteArray(0), publicSignatureData = ByteArray(0))
        verify(mockLocalServer).getUsers(userIdentifiers)
        assert(expectedUsers == result)
    }

    @Test
    fun `test getUsers() with failed remote call and unsuccessful local call`(): Unit = runBlocking {
        val userIdentifiers = listOf("id1", "id2")

        `when`(mockRemoteServer.getUsers(userIdentifiers)).thenThrow(IllegalStateException("Test exception"))
        `when`(mockLocalServer.getUsers(userIdentifiers)).thenReturn(emptyList())

        assertThrows(Exception::class.java) {
            runBlocking { serverProxy.getUsers(userIdentifiers) }
        }
    }
}
