package com.hazelmobile.filetransfer.ui.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.genonbeta.android.framework.util.FileUtils
import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.model.VideoHolder
import com.hazelmobile.filetransfer.util.TimeUtils
import com.hazelmobile.filetransfer.util.callback.TitleSupport
import kotlinx.android.synthetic.main.video_list_fragment.view.*

class VideoListFragment : Fragment(), TitleSupport {

    override fun getTitle(context: Context): CharSequence {
        return "Videos"
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.video_list_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val videoList = ArrayList<VideoHolder>()

        val videoCursor =
            context?.contentResolver?.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
            )

        if (videoCursor != null) {
            if (videoCursor.moveToFirst()) {
                val idIndex = videoCursor.getColumnIndex(MediaStore.Video.Media._ID)
                val titleIndex = videoCursor.getColumnIndex(MediaStore.Video.Media.TITLE)
                val displayIndex = videoCursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
                val albumIndex = videoCursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                val lengthIndex = videoCursor.getColumnIndex(MediaStore.Video.Media.DURATION)
                val dateIndex = videoCursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED)
                val sizeIndex = videoCursor.getColumnIndex(MediaStore.Video.Media.SIZE)
                val typeIndex = videoCursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE)

                do {

                    val holder = VideoHolder(
                        videoCursor.getString(displayIndex),
                        FileUtils.sizeExpression(videoCursor.getLong(sizeIndex), false),
                        Uri.parse("${MediaStore.Video.Media.EXTERNAL_CONTENT_URI} / + ${videoCursor.getInt(idIndex)}"),
                        TimeUtils.getDuration(videoCursor.getLong(lengthIndex))
                    )

                    videoList.add(holder)

                } while (videoCursor.moveToNext())
            }

            videoCursor.close()
        }

        view.myVideosText.text = "Video(${videoList.size})"
    }

}
