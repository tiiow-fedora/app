package com.example.smbimageviewer

import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation
import com.hierynomus.protocol.transport.TransportException
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.common.SMBRuntimeException
import com.hierynomus.smbj.connection.Connection
import com.hierynomus.smbj.session.Session
import com.hierynomus.smbj.share.DiskShare
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class SmbImageRepository {
    private val config = SmbConfig(
        host = "192.168.1.10",
        shareName = "photos",
        username = "smb_user",
        password = "smb_password",
        domain = "WORKGROUP",
        folderPath = "\u005cSamsung\u005cCamera"
    )

    suspend fun fetchImages(): Result<List<ByteArray>> = withContext(Dispatchers.IO) {
        runCatching {
            val client = SMBClient()
            client.use { smbClient ->
                smbClient.connect(config.host).use { connection ->
                    val session = authenticate(connection)
                    session.connectShare(config.shareName).use { share ->
                        if (share !is DiskShare) {
                            throw SMBRuntimeException("Share ${config.shareName} is not a disk share")
                        }
                        val images = mutableListOf<ByteArray>()
                        val directory = normalizePath(config.folderPath)
                        val listing = share.list(directory)
                        for (item in listing) {
                            if (item.isDirectory || !item.isImage()) {
                                continue
                            }
                            val bytes = readFile(share, directory, item)
                            if (bytes.isNotEmpty()) {
                                images.add(bytes)
                            }
                        }
                        images
                    }
                }
            }
        }
    }

    private fun authenticate(connection: Connection): Session {
        val authContext = AuthenticationContext(
            config.username,
            config.password.toCharArray(),
            config.domain
        )
        return connection.authenticate(authContext)
    }

    private fun readFile(share: DiskShare, directory: String, item: FileIdBothDirectoryInformation): ByteArray {
        val remotePath = "$directory/${item.fileName}"
        share.openFile(
            remotePath,
            setOf(AccessMask.GENERIC_READ),
            setOf(FileAttributes.FILE_ATTRIBUTE_NORMAL),
            setOf(),
            com.hierynomus.smbj.share.ShareAccess.ALL,
            com.hierynomus.smbj.share.SMB2CreateDisposition.FILE_OPEN,
            null
        ).use { file ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(8 * 1024)
            var offset = 0L
            while (true) {
                val read = file.read(buffer, offset, buffer.size)
                if (read <= 0) break
                output.write(buffer, 0, read)
                offset += read
            }
            return output.toByteArray()
        }
    }

    private fun normalizePath(path: String): String {
        return path.trimStart('\\').replace('\\', '/')
    }

    private fun FileIdBothDirectoryInformation.isImage(): Boolean {
        val name = fileName.lowercase()
        return name.endsWith(".jpg") ||
            name.endsWith(".jpeg") ||
            name.endsWith(".png") ||
            name.endsWith(".gif")
    }
}

private data class SmbConfig(
    val host: String,
    val shareName: String,
    val username: String,
    val password: String,
    val domain: String,
    val folderPath: String
)
