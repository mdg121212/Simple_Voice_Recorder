package com.mattg.simplevoicerecorder.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.content.FileProvider.getUriForFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.chibde.visualizer.BarVisualizer
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.mattg.simplevoicerecorder.R
import com.mattg.simplevoicerecorder.databinding.FragmentPlaybackBinding


class PlayBackFragment : Fragment() {

    val args: PlayBackFragmentArgs by navArgs()
    private lateinit var viewModel: PlayBackViewModel
    private lateinit var play: ImageButton
    private lateinit var fwd: ImageButton
    private lateinit var rew: ImageButton
    private lateinit var recordingName: EditText
    private lateinit var secDisplay: TextView
    private lateinit var durationDisplay: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var save: ImageButton
    private lateinit var share: ImageButton
    private lateinit var delete: ImageButton
    private lateinit var sizeAnimation: Animation
    private lateinit var mAdView: AdView
    private lateinit var visualizer: BarVisualizer


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_playback, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(PlayBackViewModel::class.java)
        val recording = args.recording
        viewModel.recording = recording
        val binding = FragmentPlaybackBinding.bind(view)
        setUpView(view, binding)
        recordingName.setText(recording?.name)


        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        observeViewModel()

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    seekBar?.setProgress(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

                viewModel.mediaPlayer?.pause()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

                seekBar?.progress?.let { viewModel.mediaPlayer?.seekTo(it) }
                viewModel.mediaPlayer?.start()
            }
        })

        save.setOnClickListener {
            viewModel.changeStoredFileName(recordingName.text.toString())
            Toast.makeText(requireContext(), "File name changed", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_PlaybackFragment_self)
        }

        play.setOnClickListener {
            val state = viewModel.playerState.value
            when (state) {
                PlayerState.PLAYING -> {
                    viewModel.pausePlayer()
                }
                PlayerState.PAUSED -> {
                    viewModel.resumePlayer()
                }
                PlayerState.STOPPED -> {
                    viewModel.playRecording()
                }
                else -> Toast.makeText(
                    requireContext(),
                    "Error getting player state",
                    Toast.LENGTH_SHORT
                ).show()
            }


        }

        fwd.setOnClickListener {
            seekMediaPlayer(10000)
        }
        rew.setOnClickListener {
            seekMediaPlayer(-10000)
        }

        share.setOnClickListener {
            val FILE_PROVIDER = "com.mattg.simplevoicerecorder.fileprovider"
            val content = getUriForFile(
                requireActivity().applicationContext,
                FILE_PROVIDER,
                viewModel.getMediaFile()
            )
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, content)
                type = "*/*"

            }
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            sendIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            val chooser = Intent.createChooser(sendIntent, "Share File")
            startActivity(chooser)

        }

        delete.setOnClickListener {
            AlertDialog.Builder(requireContext()).setTitle("Delete Recording?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok) { dialog, which ->
                    viewModel.deleteRecording()
                    findNavController().navigate(R.id.action_PlaybackFragment_to_recordingListFragment)
                }
                .setNegativeButton(android.R.string.cancel) { dialog, which ->
                    dialog.dismiss()
                }
                .show()
        }

        durationDisplay.text = viewModel.mediaDurationReadable.value


    }

    private fun setUpView(view: View, binding: FragmentPlaybackBinding) {
        binding.viewModel = viewModel
        play = binding.btnPlaybackPlay
        fwd = binding.btnPlaybackForward
        rew = binding.btnPlaybackBack
        recordingName = binding.etFileName
        secDisplay = binding.tvCurrentTime
        durationDisplay = binding.tvDuration
        seekBar = binding.seekBarPlayback
        save = binding.btnSaveRecordingPlay
        share = binding.btnPlaybackShare
        delete = binding.btnPlayBackDelete
        mAdView = binding.adView
        visualizer = binding.avVisual
        sizeAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.size_pulse)
    }

    private fun seekMediaPlayer(offest: Int) {
        viewModel.mediaPlayer?.let {
            val pos = it.currentPosition
            val newPos = pos + offest
            it.seekTo(newPos)
        }
    }


    private fun observeViewModel() {
        val mediaP = viewModel.mediaPlayer
        viewModel.setMpStatusWatcher()

        viewModel.mediaSessionIdData.observe(viewLifecycleOwner) {
            if (it != null) {
                visualizer.setPlayer(it.audioSessionId)
                visualizer.setColor(R.color.playback_secondary)
            }
        }
        viewModel.currentPositionReadable.observe(viewLifecycleOwner) {
            secDisplay.text = it.toString()

        }
        viewModel.currentPositionInSeconds.observe(viewLifecycleOwner) {
            if (it != null) {
                seekBar.progress = it
                secDisplay.visibility = View.VISIBLE
            }
        }
        viewModel.playButtonDrawable.observe(viewLifecycleOwner) {
            play.setImageResource(it)
            when (it) {
                R.drawable.playblue -> {
                    if (play.animation != null) {
                        play.clearAnimation()
                    }
                }
                R.drawable.pauseblue -> {
                    play.startAnimation(sizeAnimation)

                }
            }
        }
        viewModel.mediaDurationSecs.observe(viewLifecycleOwner) {
            if (it != null) {
                seekBar.max = it
            }
        }
        viewModel.mediaDurationReadable.observe(viewLifecycleOwner) {
            durationDisplay.text = it.toString()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.close()
    }


}