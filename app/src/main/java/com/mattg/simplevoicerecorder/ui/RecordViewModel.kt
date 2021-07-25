package com.mattg.simplevoicerecorder.ui

import android.app.Application
import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.Bindable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.mattg.simplevoicerecorder.R
import com.mattg.simplevoicerecorder.db.Recording
import com.mattg.simplevoicerecorder.db.RecordingDatabase
import com.mattg.simplevoicerecorder.db.RecordingRepository
import com.mattg.simplevoicerecorder.ui.util.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.*


private const val LOG_TAG = "recording"

class RecordViewModel(application: Application) : AndroidViewModel(application) {

    private val dbScope = CoroutineScope(Dispatchers.IO)
    private var fileName: String = ""
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var timer: Timer? = null

    private val dao = RecordingDatabase.getInstance(application).recordingDao()
    private val recordingRepo = RecordingRepository(dao)

    private val _recordingStateText = MutableLiveData<String>()
    val recordingStateText: LiveData<String> = _recordingStateText

    private val _stopButtonVisible = MutableLiveData<Boolean>()
    val stopButtonVisible: LiveData<Boolean> = _stopButtonVisible
    fun isStopButtonVisible() : Int {
        stopButtonVisible.value?.apply {
            return if(this) View.VISIBLE else View.GONE
        }
        return View.GONE

    }

    private val _playButtonVisible = MutableLiveData<Boolean>()
    val playButtonVisible: LiveData<Boolean> = _playButtonVisible
    fun isPlayButtonVisible() : Int {
        playButtonVisible.value?.apply {
            return if(this) View.VISIBLE else View.GONE
        }
        return View.GONE

    }

    private val _reloadButtonVisible = MutableLiveData<Boolean>()
    val reloadButtonVisible: LiveData<Boolean> = _reloadButtonVisible
    fun isReloadButtonVisible() : Int {
        _reloadButtonVisible.value?.apply {
            return  if(this)  View.VISIBLE else View.GONE
        }
        return View.GONE
    }

    private val _recordButtonVisible = MutableLiveData<Boolean>()
    val recordButtonVisible: LiveData<Boolean> = _recordButtonVisible
    fun isRecordButtonVisible() : Int {
        _recordButtonVisible.value?.apply {
            return if(this) View.VISIBLE else View.GONE
        }
        return View.GONE

    }
    private val _recordButtonDrawable = MutableLiveData<Int>()
    val recordButtonDrawable: LiveData<Int> = _recordButtonDrawable
    fun getRecordButtonDrawable(): Int {
        return recordButtonDrawable.value!!
    }
    private val _isRecording = MutableLiveData<Boolean>()
    val isRecording: LiveData<Boolean> = _isRecording

    private val _fileNameEditTextVisible = MutableLiveData<Boolean>()
    val fileNameEditTextVisible: LiveData<Boolean> = _fileNameEditTextVisible
    fun isFileNameEditTextVisible() : Int {
        fileNameEditTextVisible.value?.apply {
            return if(this) View.VISIBLE else View.GONE
        }
        return View.GONE
    }
    private val _isPaused = MutableLiveData<Boolean>()
    val isPaused: LiveData<Boolean> = _isPaused

    private val _currentFileName = MutableLiveData<String>()
    val currentFileName: LiveData<String> = _currentFileName

    val _fileNameText = MutableLiveData<String>()
    val fileNameText: LiveData<String> = _fileNameText
    fun setFileNameText(fileName: String){
        _fileNameText.postValue(fileName)
    }
    private val _navigationActionId = MutableLiveData<Event<Int>>()
    val navigationActionId: LiveData<Event<Int>> = _navigationActionId
    fun setNavigationId(id: Int){
        _navigationActionId.postValue(Event(id))
    }
    private val _resetViewAction = MutableLiveData<Event<Boolean>>()
    public val resetViewAction: LiveData<Event<Boolean>> = _resetViewAction
    public fun setResetViewAction(shouldReset: Boolean){
        _resetViewAction.postValue(Event(shouldReset))
    }
    private val _recorderState = MutableLiveData<RecorderState>()
    val recorderState: LiveData<RecorderState> = _recorderState

    private val _storageSpaceValue = MutableLiveData<String>()
    val storageSpaceValue: LiveData<String> = _storageSpaceValue

    private val _recordingTimeInt = MutableLiveData<Int>()
    val recordingTimeInt: LiveData<Int> = _recordingTimeInt

    private val _recordingTimeReadable = MutableLiveData<String>()
    val recordingTimeReadable: LiveData<String> = _recordingTimeReadable

    private val _isRecodingForAnimation = MutableLiveData<Boolean>()
    val isRecordingForAnimation: LiveData<Boolean> = _isRecodingForAnimation

    private val _amplitudeForVisual = MutableLiveData<Int>()
    val amplitudeForVisual: LiveData<Int> = _amplitudeForVisual

    private val _toastMessageText = MutableLiveData<Event<String>>()
    val toastMessageText: LiveData<Event<String>> = _toastMessageText
    private fun setToast(message: String){
        _toastMessageText.postValue(Event(message))
    }


    private var storageDir: String = ""
    private var folder: String = ""
    private var recording: Recording? = null
    private val extension = ".mp4"

    fun getRecording(): Recording? {
        return recording
    }

    fun clearRecordingOnCreate() {
        recording = null
    }

    private val recorderStateObserver = Observer<RecorderState> {
        when (it) {
            RecorderState.JUSTSAVED -> {
                _isRecodingForAnimation.postValue(false)
                _recordingStateText.postValue("SAVED")
                _reloadButtonVisible.value = true
                _recordButtonDrawable.value = (R.drawable.recordblue)
                _isRecording.value = false
                dbScope.launch {
                    saveRecordToDb()
                }
            }
            RecorderState.NOTSTARTED -> {
                _isRecodingForAnimation.postValue(false)
                _recordingStateText.postValue("")
                _recordButtonDrawable.value = (R.drawable.recordblue)
                _reloadButtonVisible.value = false
                _isRecording.value = false
                _stopButtonVisible.value = false
                _playButtonVisible.value = false
                _fileNameEditTextVisible.value = false
            }
            RecorderState.STOPPED -> {
                _isRecodingForAnimation.postValue(false)
                _recordingStateText.postValue("STOPPED")
                _reloadButtonVisible.value = true
                _recordButtonDrawable.value = (R.drawable.recordblue)
                _isRecording.value = false
                _stopButtonVisible.value = false
                _playButtonVisible.value = true
                _fileNameEditTextVisible.value = true
                _currentFileName.value = fileName

            }
            RecorderState.PAUSED -> {
                _isRecodingForAnimation.postValue(false)
                _recordingStateText.postValue("PAUSED")
                _reloadButtonVisible.value = true
                _recordButtonDrawable.value = (R.drawable.recordblue)
                _isRecording.value = false
                _isPaused.value = true
                _stopButtonVisible.value = true
                _playButtonVisible.value = false

            }
            RecorderState.RECORDING -> {
                _isRecodingForAnimation.postValue(true)
                _recordingStateText.postValue("RECORDING")
                _recordButtonDrawable.value = (R.drawable.pausered)
                _reloadButtonVisible.value = false
                _isRecording.value = true
                _stopButtonVisible.value = true
                _playButtonVisible.value = false
            }
            RecorderState.ENDED -> {
                _recordingStateText.postValue("")
                _recordButtonDrawable.value = (R.drawable.recordblue)
                _isRecording.value = false
                _stopButtonVisible.value = false
                _playButtonVisible.value = true
            }
            else -> Log.e("Error", "Error getting recorder state")
        }

    }


    init {
        _recorderState.observeForever(recorderStateObserver)
        _recorderState.postValue(RecorderState.NOTSTARTED)
        _playButtonVisible.postValue(false)
        val converted = getSpace(application)
        _storageSpaceValue.postValue(converted)

    }

    private fun getSpace(context: Context): String? {
        val space = Utils().getAvailableStorage(context)
        return space?.let { convertBytes(it) }
    }

    private fun convertBytes(bytes: Long): String {
        val kbDivisor = 1024L
        val mbDivisor = kbDivisor * kbDivisor
        val gbDivisor = mbDivisor * kbDivisor
        return when {
            bytes <= mbDivisor -> {
                val kbDouble = bytes.toDouble() / kbDivisor
                String.format("%.2f", kbDouble) + " KB"
            }
            bytes <= gbDivisor -> {
                val mbDouble = bytes.toDouble() / mbDivisor
                String.format("%.2f", mbDouble) + " KB"
            }
            else -> {
                val gbDouble = bytes.toDouble() / gbDivisor
                String.format("%.2f", gbDouble) + "GB"
            }
        }

    }

    override fun onCleared() {
        super.onCleared()
        _recorderState.removeObserver(recorderStateObserver)
        player?.release()
    }

    fun setEnded() {
        _recorderState.postValue(RecorderState.ENDED)
    }

    fun resetView() {
        _recorderState.postValue(RecorderState.NOTSTARTED)
        getSpace(getApplication())
    }

    fun setViewSaved() {
        _recorderState.postValue(RecorderState.JUSTSAVED)
    }

    private fun ensureStorageDirExists() {
        File(storageDir, folder).mkdir()
    }

    private fun getAbsoluteFilePath(): String {
        return "$storageDir$folder/$fileName$extension"
    }


    fun startRecording() {
        _recorderState.postValue(RecorderState.RECORDING)
        folder = Utils().getDateUnixMillis().toString()
        ensureStorageDirExists()
        setFileName(Utils().getDateTimeReadable())
        Log.e(LOG_TAG, "recording started")
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.DEFAULT)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(getAbsoluteFilePath())
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            try {
                prepare()
                start()
                startTimer()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed ${e.cause} \n${e.message}\n${e.localizedMessage}")
            }

        }
    }

    fun pauseRecording() {
        _recordButtonDrawable.value = (R.drawable.recordblue)
        _recorderState.postValue(RecorderState.PAUSED)
        _isRecodingForAnimation.postValue(false)
        recorder?.pause()
        stopTimer()
    }

    fun resumeRecording() {
        _recordButtonDrawable.value = (R.drawable.pause_red)
        _recorderState.postValue(RecorderState.RECORDING)
        _isRecodingForAnimation.postValue(true)
        _isPaused.value = false
        _isRecording.value = true
        recorder?.resume()
        startTimer()
    }

    private fun getMediaLengthInSeconds(): String {
        val uri: Uri =
            Uri.parse(Utils().getRecordingPathString(storageDir, folder, fileName, extension))
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(getApplication(), uri)
        val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val millSecond = durationStr!!.toInt()
        return (millSecond / 1000).toTimeReadable()
    }

    private fun startTimer() {
        Log.i("TESTING", "TIMER START FUNCTION CALLED")
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                Log.i("TESTING", "TIMER RUN OVERRIDE FUN CALLED")
                if (recorder != null) {
                    val currentMaxAmplitude = recorder?.maxAmplitude
                    _amplitudeForVisual.postValue(currentMaxAmplitude)
                    Log.i("TESTING", "TIMER maxamplitude = $currentMaxAmplitude")
                }

            }
        }, 0, 100)
    }

    private fun stopTimer() {
        timer?.cancel()
    }

    fun stopRecording() {
        stopTimer()
        _recorderState.postValue(RecorderState.STOPPED)
        _isRecodingForAnimation.postValue(false)

        recorder?.stop()
        recorder?.reset()
        recorder?.release()

        recorder = null

        Log.i(LOG_TAG, "recording ceased filename is $fileName")

        recorder = null
    }

    fun setStorageDirectory(storageDirectory: String) {
        storageDir = storageDirectory
    }


    private fun setFileName(name: String) {
        fileName = name
        _fileNameText.postValue(fileName)
    }

    fun changeStoredFileName(name: String): Boolean {
        val didSave : Boolean
        val from = Utils().getRecordingPath(storageDir, folder, fileName, extension)
        fileName = name
        val to = Utils().getRecordingPath(
            storageDir,
            folder,
            name,
            extension
        ) // watch out for trailing slash in storageDir
        dbScope.launch {
            updateRecording(fileName)
        }
        didSave = Utils().changeFileNameOnDisk(from, to)
        recording?.let { Utils().changeRecordingName(it, name, dao) }
        Log.i("TESTING", "SHOULD LOOK LIKE ${to.absolutePath}")
        return didSave
    }

    private suspend fun saveRecordToDb() {
        recording = Recording(fileName, folder.toLong(), null, getMediaLengthInSeconds(), extension)
        recording?.let {
            dbScope.launch {
                recordingRepo.addRecording(it)
            }
        }
    }

    private suspend fun updateRecording(newName: String) {
        recording?.name = newName
        recording?.let {
            viewModelScope.launch {
                recordingRepo.updateRecording(it)
            }
        }

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
    /* <---------------------------------- Binding Methdods ----------------------------------------->*/
    fun recordClick(){
        when (isRecording.value) {
            true -> {
               pauseRecording()
            }
            false -> {
                Log.i(
                    "TESTING",
                    "misrecording is false, and is paused is ${isPaused.value}"
                )
                if (isPaused.value == true) resumeRecording() else startRecording()
            }
        }
    }

   fun saveClick() {
        val newFileName = if (_fileNameText.value!!.isEmpty())  _fileNameText.value else ""
        val wasChanged = changeStoredFileName(newFileName!!)
        Log.i("SAVING", " was changed = $wasChanged")
        if (wasChanged) {
            setViewSaved()
            setToast("Recording Saved!")
        } else {
            setToast("Error saving recording to database...")
        }
    }

   fun viewRecordingsClick() {
           setEnded()
           resetView()
           //navigate(R.id.action_RecordFragment_to_recordingListFragment)
            setNavigationId(R.id.action_RecordFragment_to_recordingListFragment)
   }

    fun settingsClick() {
       // settings.setOnClickListener {
        setNavigationId(R.id.action_RecordFragment_to_settingsFragment)
    }

    fun playClick() {
            val recording = getRecording()
            if (recording != null) {
                setNavigationId(R.id.action_RecordFragment_to_SecondFragment)
                resetView()
            } else
        setToast("No recording to play, save first")
        }

    fun reloadClick() {
        resetView()
        setResetViewAction(true)
      }
    }
