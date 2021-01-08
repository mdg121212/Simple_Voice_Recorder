package com.mattg.simplevoicerecorder.db



class RecordingRepository(private val dao: RecordingDao) {

    suspend fun getAllRecordings(): List<Recording> {
        return dao.getAll()
    }

    suspend fun deleteRecording(recording: Recording){
        dao.delete(recording)
    }

    suspend fun updateRecording(recording: Recording){
        dao.updateRecording(recording)
    }

    suspend fun addRecording(recording: Recording){
        dao.addRecording(recording)
    }

}