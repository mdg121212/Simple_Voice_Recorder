package com.mattg.simplevoicerecorder.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mattg.simplevoicerecorder.R
import com.mattg.simplevoicerecorder.db.Recording

class RecordingAdapter(private val clickListener: (Recording) -> Unit) :
    PagingDataAdapter<Recording, RecordingAdapter.RecordingViewHolder>(
        DIFF_CALLBACK
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordingViewHolder {
        return RecordingViewHolder(parent)
    }

    override fun onBindViewHolder(holder: RecordingViewHolder, position: Int) {
        val recording: Recording? = getItem(position)
        recording?.let {
            holder.bindTo(it)
            holder.itemView.setOnClickListener { clickListener(recording) }
        }

    }

    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<Recording>() {
            // Concert details may have changed if reloaded from the database,
            // but ID is fixed.
            override fun areItemsTheSame(
                oldRecording: Recording,
                newRecording: Recording
            ) = oldRecording.id == newRecording.id

            override fun areContentsTheSame(
                oldRecording: Recording,
                newRecording: Recording
            ) = oldRecording == newRecording
        }
    }

    class RecordingViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.recycler_item, parent, false)
    ) {
        private val name = itemView.findViewById<TextView>(R.id.tv_recording_name)
        private val date = itemView.findViewById<TextView>(R.id.tv_dateRecorded)
        private val length = itemView.findViewById<TextView>(R.id.tv_recordingLength)
        var recording: Recording? = null

        fun bindTo(rec: Recording?) {
            this.recording = rec
            name.text = rec?.name
            date.text = rec?.unixTimeMillis?.let { Utils().convertUnixTimeMillisForDisplay(it) }
            length.text = rec?.readable_length.toString()
        }
    }


}
