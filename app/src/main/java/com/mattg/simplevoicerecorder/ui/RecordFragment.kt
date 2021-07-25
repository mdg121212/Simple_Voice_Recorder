package com.mattg.simplevoicerecorder.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.mattg.simplevoicerecorder.R
import com.mattg.simplevoicerecorder.databinding.FragmentRecordBinding
import com.mattg.simplevoicerecorder.ui.util.Event
import com.visualizer.amplitude.AudioRecordView


private const val REQUEST_RECORD_AUDIO_PERMISSION = 200


class RecordFragment : Fragment() {

    private lateinit var viewModel: RecordViewModel

    private var permissions: Array<String> =
        arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE)
    private var permissionToRecordAccepted: Boolean = false
    private var permissionToReadStorage: Boolean = false
    private lateinit var binding: FragmentRecordBinding
    private lateinit var record: ImageButton
    private lateinit var stop: ImageButton
    private lateinit var play: ImageButton
    private lateinit var save: ImageButton
    private lateinit var space: TextView
    private lateinit var etFileName: EditText
    private lateinit var viewRecordings: Button
    private lateinit var settings: ImageButton
    private lateinit var reload: ImageButton
    private lateinit var time: TextView
    private lateinit var warn: TextView
    private lateinit var visualizer: AudioRecordView
  //  private var mIsRecording = false
   // private var mIsPaused = false
 //   private var wasJustSaved = false
    private lateinit var animation: Animation
    private lateinit var sizeAnimation: Animation
    private lateinit var mAdView: AdView
    //private var wasPlayAnimation = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.setTheme(R.style.Theme_SimpleVoiceRecorder)
        super.onViewCreated(view, savedInstanceState)
        ActivityCompat.requestPermissions(
            requireActivity(),
            permissions,
            REQUEST_RECORD_AUDIO_PERMISSION
        )
        viewModel = ViewModelProvider(this).get(RecordViewModel::class.java)
        //bind views
        setupView(view)

        viewModel.setStorageDirectory(Utils().getExternalStorageDirectory(requireContext()))
        MobileAds.initialize(requireContext()) {
            val adRequest = AdRequest.Builder().build()
            mAdView.loadAd(adRequest)
        }
        observeViewModel()

//        stop.setOnClickListener {
//            viewModel.stopRecording()
//        }

//        viewRecordings.setOnClickListener {
//            viewModel.setEnded()
//            resetView()
//            findNavController().navigate(R.id.action_RecordFragment_to_recordingListFragment)
//        }

//        settings.setOnClickListener {
//            findNavController().navigate(R.id.action_RecordFragment_to_settingsFragment)
//        }

//        play.setOnClickListener {
//            val recording = viewModel.getRecording()
//            if (recording != null) {
//                val action =
//                    RecordFragmentDirections.actionRecordFragmentToSecondFragment(recording)
//                findNavController().navigate(action)
//                resetView()
//            } else Toast.makeText(
//                requireContext(),
//                "No recording to play, save first",
//                Toast.LENGTH_SHORT
//            )
//                .show()
//        }

//        reload.setOnClickListener {
//            resetView()
//        }

        ReviewPrompt(requireContext(), requireActivity()).promptForReview()



    }

    private fun setupView(view: View) {
        binding = FragmentRecordBinding.bind(view)
        record = binding.btnRecord
        viewRecordings = binding.btnViewRecordings
        settings = binding.btnSettings
        play = binding.btnRecordPlay
        //stop = binding.btnStopRecord
        etFileName = binding.etFileName
        save = binding.btnSaveRecording
        reload = binding.btnReload
        space = binding.tvAvailableSpace
        time = binding.tvRecordingTime
        mAdView = binding.adView
        warn = binding.tvWarning
        visualizer = binding.avVisual
        //initializing animations
        animation = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out_then_in)
        sizeAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.size_pulse)
        sizeAnimation.repeatMode = Animation.INFINITE
        animation.repeatMode = Animation.INFINITE


    }

//    private fun resetView() {
//        viewModel.resetView()
//        wasJustSaved = false
//        ViewAnimation().moveBackFromRight(play, 50f)
//    }

    @SuppressLint("SetTextI18n")
    private fun observeViewModel() {
        viewModel.clearRecordingOnCreate()
        viewModel.resetViewAction.observe(viewLifecycleOwner){
            it.getContentIfNotHandled()?.let {
                viewModel.resetView()
                ViewAnimation().moveBackFromRight(play, 50f)
            }
        }
        viewModel.navigationActionId.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { // Only proceed if the event has never been handled
                findNavController().navigate(it)
            }
        }
        viewModel.isRecordingForAnimation.observe(viewLifecycleOwner) {
            when (it) {
                true -> {
                    time.startAnimation(animation)
                    record.startAnimation(sizeAnimation)
                }
                false -> {
                    if (time.animation != null) {
                        time.clearAnimation()
                        record.clearAnimation()
                    }

                }
            }
        }
        viewModel.stopButtonVisible.observe(viewLifecycleOwner) {
            when (it) {
                true -> {
               //     warn.visibility = View.VISIBLE
               //     stop.visibility = View.VISIBLE
                    ViewAnimation().moveLeft(stop, 50f)

                }
                false -> {
              //      stop.visibility = View.GONE
              //      warn.visibility = View.GONE
                }
            }
        }
//        viewModel.storageSpaceValue.observe(viewLifecycleOwner) {
//            space.text = "$it Available Space"
//        }
//        viewModel.reloadButtonVisible.observe(viewLifecycleOwner) {
//            reload.visibility = if (it) View.VISIBLE else View.GONE
//        }
//        viewModel.isRecording.observe(viewLifecycleOwner) {
//            mIsRecording = it
//        }
        viewModel.recordButtonDrawable.observe(viewLifecycleOwner) {
            record.setImageResource(it)
        }
//        viewModel.isPaused.observe(viewLifecycleOwner) {
//            mIsPaused = it
//        }
        viewModel.playButtonVisible.observe(viewLifecycleOwner) {
            when (it) {
                true -> {
                //    play.visibility = View.VISIBLE
                //    if (!wasJustSaved) {
                    //    if (!wasPlayAnimation) {
                            ViewAnimation().moveRight(play, 50f)
                           // wasPlayAnimation = true
                     //   }
                  //  }
                }
             //   false -> play.visibility = View.GONE
            }
        }
        viewModel.amplitudeForVisual.observe(viewLifecycleOwner) {
            if (it != null) {
                visualizer.update(it)
            }
        }
//        viewModel.fileNameEditTextVisible.observe(viewLifecycleOwner) {
//            when (it) {
//                true -> {
//                    etFileName.visibility = View.VISIBLE
//                    etFileName.setText(viewModel.fileNameText.value.toString())
//                    save.visibility = View.VISIBLE
//                }
//                false -> {
//                    etFileName.visibility = View.GONE
//                    save.visibility = View.GONE
//                }
//            }
//        }
//        viewModel.recordingTimeReadable.observe(viewLifecycleOwner) {
//
//        }
//        viewModel.recordingStateText.observe(viewLifecycleOwner) {
//            time.text = it
//        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        permissionToReadStorage = if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            grantResults[1] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        if (!permissionToRecordAccepted || !permissionToReadStorage) informUserAboutPermissions()
    }

    private fun informUserAboutPermissions() {
        Toast.makeText(requireContext(), "You need to enable permissions for the app to function", Toast.LENGTH_SHORT).show()
    }


}

/**
 * Extension function to allow navigation based on id passed from LiveData Event
 */
private fun NavController.navigate(it: Event<Int>?) {
        this.navigate(it)
}
