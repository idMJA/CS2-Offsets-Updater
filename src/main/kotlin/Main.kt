package my.mjba.CS2Offsets

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import javafx.application.Application
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import kotlin.system.exitProcess

object CS2OffsetsUpdater {
    private const val GITHUB_API_BASE = "https://api.github.com/repos/a2x/cs2-dumper/contents/output"
    private const val RAW_CONTENT_BASE = "https://raw.githubusercontent.com/a2x/cs2-dumper/main/output"
    private val client = OkHttpClient()
    private val mapper = jacksonObjectMapper()
    private val currentDir = File(".")

    fun getLocalFiles(): Set<String> {
        return currentDir.listFiles()
            ?.filter { it.isFile && (it.name.endsWith(".json") || it.name.endsWith(".cs") || 
                      it.name.endsWith(".hpp") || it.name.endsWith(".rs")) }
            ?.map { it.name }
            ?.toSet()
            ?: emptySet()
    }

    fun getRemoteContent(filename: String): String {
        val request = Request.Builder()
            .url("$RAW_CONTENT_BASE/$filename")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Failed to get remote content: ${response.code}")
            }
            return response.body?.string() ?: throw Exception("Empty response")
        }
    }

    fun getLocalExtensions(): Set<String> {
        return getLocalFiles()
            .map { it.substringAfterLast('.', "") }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    suspend fun checkForUpdates(): List<File> {
        val request = Request.Builder()
            .url(GITHUB_API_BASE)
            .build()

        val response = withContext(Dispatchers.IO) {
            client.newCall(request).execute()
        }

        if (!response.isSuccessful) {
            throw Exception("Failed to get repository contents: ${response.code}")
        }

        val content = response.body?.string() ?: throw Exception("Empty response")
        val files = mapper.readTree(content)
        getLocalExtensions()
        
        // Only get files that exist locally and need updates
        val localFiles = getLocalFiles()
        val candidates = files.asSequence()
            .map { it["name"].asText() }
            .filter { filename -> 
                // Only include files that exist locally
                localFiles.contains(filename)
            }
            .map { filename -> File(currentDir, filename) }
            .filter { file ->
                // Check if the file needs an update
                runBlocking { needsUpdate(file, file.name) }
            }
            .toList()
            
        return candidates
    }

    suspend fun updateFile(file: File) {
        // Only update if the file exists
        if (!file.exists()) {
            throw Exception("Cannot update non-existent file: ${file.name}")
        }
        
        val content = withContext(Dispatchers.IO) {
            getRemoteContent(file.name)
        }
        withContext(Dispatchers.IO) {
            file.writeText(content)
        }
    }

    private suspend fun needsUpdate(localFile: File, remoteFilename: String): Boolean {
        val localContent = withContext(Dispatchers.IO) {
            localFile.readText()
        }
        val remoteContent = withContext(Dispatchers.IO) {
            getRemoteContent(remoteFilename)
        }
        return localContent != remoteContent
    }
}

fun main() {
    try {
        Application.launch(CS2OffsetApp::class.java)
    } catch (e: Exception) {
        println("Error starting application: ${e.message}")
        e.printStackTrace()
        exitProcess(1)
    }
}