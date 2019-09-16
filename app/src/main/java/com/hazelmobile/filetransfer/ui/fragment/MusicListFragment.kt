package com.hazelmobile.filetransfer.ui.fragment

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager

import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.model.SongHolder
import com.hazelmobile.filetransfer.ui.adapter.MusicListAdapter
import com.hazelmobile.filetransfer.ui.viewmodel.MusicListViewModel
import com.hazelmobile.filetransfer.utils.callback.TitleSupport
import kotlinx.android.synthetic.main.music_list_fragment.*

class MusicListFragment : Fragment(), TitleSupport {

    override fun getTitle(context: Context): CharSequence {
        return "Audio"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.music_list_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val audioList = ArrayList<SongHolder>()

        val songCursor = context?.contentResolver?.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            null,
            MediaStore.Audio.Media.IS_MUSIC + "=?",
            arrayOf(1.toString()), null
        )

        if (songCursor != null) {
            if (songCursor.moveToFirst()) {

                val songIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val nameIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                val dateIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)
                val sizeIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.SIZE)

                do {
                    val songSize = songCursor.getString(sizeIndex)
                    val songDate = songCursor.getString(dateIndex)
                    val songHolder = SongHolder(songCursor.getString(songIndex), songSize + songDate)
                    audioList.add(songHolder)
                } while (songCursor.moveToNext())
            }

            songCursor.close()
        }


        audioView.apply {
            layoutManager = LinearLayoutManager(context)
            val applicationListAdapter = MusicListAdapter(audioList, context)
            audioView.adapter = applicationListAdapter
        }

    }

}
