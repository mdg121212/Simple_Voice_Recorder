package com.mattg.simplevoicerecorder.ui

import android.app.Application
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.lifecycle.*
import com.mattg.simplevoicerecorder.R
import com.mattg.simplevoicerecorder.db.Recording
import com.mattg.simplevoicerecorder.db.RecordingDatabase
import com.mattg.simplevoicerecorder.db.RecordingRepository
import kotlinx.coroutines.launch
import java.io.File


class PlayBackViewModel(private val app: Application) : AndroidViewModel(app) {

    var recording: Recording? = null
    var mediaPlayer: MediaPlayer? = null
    private val dao = RecordingDatabase.getInstance(app).recordingDao()
    private val recordingRepo = RecordingRepository(dao)
    private val context = app.applicationContext
    private val _mediaDurationSecs = MutableLiveData<Int?>()
    val mediaDurationSecs: LiveData<Int?> = _mediaDurationSecs

    private val _mediaDurationReadable = MutableLiveData<String?>()
    val mediaDurationReadable: LiveData<String?> = _mediaDurationReadable

    private val _currentPositionReadable = MutableLiveData<String?>()
    val currentPositionReadable: LiveData<String?> = _currentPositionReadable

    private val _currentPositionInSeconds = MutableLiveData<Int?>()
    val currentPositionInSeconds: LiveData<Int?> = _currentPositionInSeconds

    private val _seekBarDuration = MutableLiveData<Int?>()
    val seekBarDuration: LiveData<Int?> = _seekBarDuration

    private val _mediaSessionIdData = MutableLiveData<MediaPlayer?>()
    val mediaSessionIdData: LiveData<MediaPlayer?> = _mediaSessionIdData

    private val _playButtonDrawable = MutableLiveData<Int>()
    val playButtonDrawable: LiveData<Int> = _playButtonDrawable

    private val _playerState = MutableLiveData<PlayerState>()
    val playerState: LiveData<PlayerState> = _playerState

    private val statusInterval = 1000L
    private val handler = Handler(Looper.getMainLooper())
    private var statusChecker: Runnable? = null

    private var storageDir = Utils().getExternalStorageDirectory(getApplication())
    private var fileName = ""
    private var extension = ""
    private var folder = ""


    private val playerStateObserver = Observer<PlayerState> {
        when (it) {
            PlayerState.PAUSED -> {
                _playButtonDrawable.postValue(R.drawable.playblue)

                mediaPlayer?.pause()
            }
            PlayerState.RESUMED -> {
                _playButtonDrawable.postValue(R.drawable.pauseblue)

                mediaPlayer?.start()
            }

            PlayerState.PLAYING -> {

                _playButtonDrawable.postValue(R.drawable.pauseblue)
            }
            PlayerState.STOPPED -> {
                _playButtonDrawable.postValue(R.drawable.playblue)

                mediaPlayer?.reset()
            }
        }
    }

    init {
        _currentPositionReadable.postValue("00:00")
        _playerState.observeForever(playerStateObserver)
        _playerState.postValue(PlayerState.STOPPED)
    }

    fun setMpStatusWatcher() {
        Log.i("TESTING", "STATUSWATCHER FIRED")
        statusChecker = object : Runnable {
            override fun run() {
                try {
                    updateStatus()
                } finally {
                    handler.postDelayed(this, statusInterval)
                }
            }
        }
        statusChecker?.run()

    }


    private fun updateStatus() {
        mediaPlayer?.let {
            val postionValue = it.currentPosition / 1000
            val durationValue = it.duration / 1000
            _mediaSessionIdData.postValue(it)

            if (it.isPlaying) {
                _playerState.value = PlayerState.PLAYING
            }
            _currentPositionInSeconds.postValue(it.currentPosition)
            _currentPositionReadable.postValue(postionValue.toTimeReadable())
            _mediaDurationSecs.postValue(it.duration)
            _mediaDurationReadable.postValue(durationValue.toTimeReadable())


        }
    }


    fun playRecording(): MediaPlayer? {
        _playerState.value = PlayerState.PLAYING
        folder = recording?.unixTimeMillis.toString()
        fileName = recording?.name.toString()
        extension = recording?.extension.toString()
        val extStorage = Utils().getExternalStorageDirectory(context)
        val myUri = Uri.fromFile(
            Utils().getRecordingPath(
                extStorage,
                folder,
                fileName,
                extension
            )
        )
        Log.i("TESTING", "PLAY recording uri is $myUri")
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
            setDataSource(context, myUri)
            prepareAsync()
            setOnPreparedListener {
                start()
                _mediaSessionIdData.value = (it)
            }
            setOnCompletionListener {
                _playerState.postValue(PlayerState.PAUSED)
            }
        }
        return mediaPlayer
    }

    fun pausePlayer() {
        mediaPlayer?.pause()
        _playButtonDrawable.postValue(R.drawable.playblue)
        _playerState.postValue(PlayerState.PAUSED)

    }

    fun resumePlayer() {
        mediaPlayer?.start()
        _playButtonDrawable.postValue(R.drawable.pauseblue)
        _playerState.postValue(PlayerState.PLAYING)
    }

    fun close() {
        mediaPlayer?.stop()
    }

    override fun onCleared() {
        Log.i("TESTING", "PLAYBACK VIEWMODEL ONCLEARED CALLED")
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacks(statusChecker!!)

    }

    private fun Int.toTimeReadable(): String {
        var returnString: String
        if (this <= 60) {
            returnString = "00:" + String.format("%02d", this)
        } else {
            val minutes = this / 60
            val seconds = this % 60
            returnString = if (seconds < 10) {
                "$minutes:0$seconds"
            } else "$minutes:$seconds"
        }
        return returnString
    }

    fun getMediaFile(): File {
        Log.i(
            "TRYING TO FIX FILE",
            "vars in order, storage dir = $storageDir / folder = ${recording?.unixTimeMillis.toString()}/ filename = ${recording?.name} / ${recording?.extension}"
        )
        return Utils().getRecordingPath(
            storageDir,
            recording?.unixTimeMillis.toString(),
            recording?.name.toString(),
            recording?.extension!!.trim()
        )
    }

    fun changeStoredFileName(name: String): Boolean {
        val didSave: Boolean
        storageDir = Utils().getExternalStorageDirectory(getApplication())
        fileName = recording?.name.toString()
        extension = recording?.extension.toString()
        folder = recording?.unixTimeMillis.toString()
        val from = Utils().getRecordingPath(storageDir, folder, fileName, extension)
        fileName = name
        val to = Utils().getRecordingPath(
            storageDir,
            folder,
            name,
            extension
        ) // watch out for trailing slash in storageDir

        didSave = Utils().changeFileNameOnDisk(from, to)
        recording?.let { Utils().changeRecordingName(it, name, dao) }
        //   didSave = from.renameTo(to)

        return didSave
    }

    fun deleteRecording() {
        viewModelScope.launch {
            recording?.let { recordingRepo.deleteRecording(it) }
        }
        val file = getMediaFile()
        if (file.exists()) {
            file.delete()
        }
    }

}

