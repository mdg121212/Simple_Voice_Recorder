package com.mattg.simplevoicerecorder.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mattg.simplevoicerecorder.R
import com.mattg.simplevoicerecorder.databinding.RecordingListFragmentBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RecordingListFragment : Fragment() {


    private lateinit var viewModel: RecordingListViewModel
    private lateinit var recycler: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var mAdView: AdView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.recording_list_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = RecordingListFragmentBinding.bind(view)
        recycler = binding.rvRecordings
        fab = binding.fabAddRecording
        mAdView = binding.adView

        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        viewModel = ViewModelProvider(requireActivity()).get(RecordingListViewModel::class.java)
        val adapter = RecordingAdapter {
            val action =
                RecordingListFragmentDirections.actionRecordingListFragmentToSecondFragment(it)
            findNavController().navigate(action)
        }
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.recordings.collectLatest {
                adapter.submitData(it)
            }
        }

        recycler.setAdapter(adapter)
        fab.setOnClickListener {
            findNavController().navigate(R.id.action_recordingListFragment_to_RecordFragment)
        }
    }


}