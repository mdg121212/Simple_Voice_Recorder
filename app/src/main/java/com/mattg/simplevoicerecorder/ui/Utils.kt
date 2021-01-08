package com.mattg.simplevoicerecorder.ui

import android.content.Context
import androidx.core.content.ContextCompat
import com.mattg.simplevoicerecorder.db.Recording
import com.mattg.simplevoicerecorder.db.RecordingDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class Utils {

    fun getDateUnixMillis(): Long {
        return System.currentTimeMillis()
    }

    fun getDateTimeReadable(): String {
        val dateToFormat = Date(getDateUnixMillis())
        return SimpleDateFormat("yyyy-MM-dd HH-mm-ss", Locale.getDefault()).format(dateToFormat)
    }

    fun convertUnixTimeMillisForDisplay(input: Long): String {
        return try {
            val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            val netDate = Date(input)
            sdf.format(netDate)
        } catch (e: Exception) {
            e.toString()
        }
    }

    fun changeFileNameOnDisk(from: File, to: File): Boolean {
        return from.renameTo(to)
    }

    fun getExternalStorageDirectory(context: Context): String {
        val externalStorageVolumes: Array<out File> =
            ContextCompat.getExternalFilesDirs(context, null)
        val primaryExternalStorage = externalStorageVolumes[0]

        return ("${primaryExternalStorage.absolutePath}/")
    }

    fun getRecordingPath(
        storageDir: String,
        folder: String,
        fileName: String,
        fileExt: String
    ): File {
        return File(storageDir, "$folder/$fileName$fileExt")
    }

    fun getRecordingPathString(
        storageDir: String,
        folder: String,
        fileName: String,
        fileExt: String
    ): String {
        return File(storageDir, "$folder/$fileName$fileExt").absolutePath
    }

    fun changeRecordingName(rec: Recording, newName: String, dao: RecordingDao): Recording {
        rec.name = newName
        updateRecordingDb(rec, dao)
        return rec
    }

    private fun updateRecordingDb(rec: Recording?, dao: RecordingDao?) {
        rec?.let {
            CoroutineScope(Dispatchers.IO).launch {
                dao?.updateRecording(rec)
            }
        }

    }

    fun getAvailableStorage(context: Context): Long? {
        return context.externalCacheDir?.freeSpace
    }

}