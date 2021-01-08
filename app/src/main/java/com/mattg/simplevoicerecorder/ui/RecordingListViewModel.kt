package com.mattg.simplevoicerecorder.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.mattg.simplevoicerecorder.db.RecordingDatabase
import com.mattg.simplevoicerecorder.db.RecordingRepository

class RecordingListViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = RecordingDatabase.getInstance(application).recordingDao()
    private val recordingRepo = RecordingRepository(dao)

    val recordings = Pager(
        PagingConfig(
            pageSize = 50,
            enablePlaceholders = true,
            maxSize = 200
        )
    ) {
        dao.getAllPaged()
    }.flow


}