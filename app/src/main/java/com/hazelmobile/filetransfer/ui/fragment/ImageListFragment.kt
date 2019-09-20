package com.hazelmobile.filetransfer.ui.fragment


import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager

import com.hazelmobile.filetransfer.R
import com.hazelmobile.filetransfer.model.ImageHolder
import com.hazelmobile.filetransfer.ui.adapter.ImageListAdapter
import com.hazelmobile.filetransfer.util.TimeUtils
import com.hazelmobile.filetransfer.util.callback.TitleSupport
import kotlinx.android.synthetic.main.fragment_image_list.*


class ImageListFragment : Fragment(), TitleSupport {

    override fun getTitle(context: Context): CharSequence {
        return "Photos"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_image_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageList = ArrayList<ImageHolder>()

        val cursor =
            context?.contentResolver?.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
            )

        if (cursor != null) {
            if (cursor.moveToFirst()) {

                val idIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val titleIndex = cursor.getColumnIndex(MediaStore.Images.Media.TITLE)
                val displayIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                val albumIndex =
                    cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                val dateAddedIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)
                val sizeIndex = cursor.getColumnIndex(MediaStore.Images.Media.SIZE)
                val typeIndex = cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE)

                do {
                    val holder = ImageHolder(

                        TimeUtils.formatDateTime(
                            context!!,
                            cursor.getLong(dateAddedIndex) * 1000
                        ).toString(),
                        cursor.getString(albumIndex),
                        Uri.parse(
                            "${MediaStore.Images.Media.EXTERNAL_CONTENT_URI}/${cursor.getInt(
                                idIndex
                            )}"
                        )

                    )

                    imageList.add(holder)

                } while (cursor.moveToNext())
            }

            cursor.close()

            imageView.apply {
                imageView.layoutManager = GridLayoutManager(context, 3)
                val imageListAdapter = ImageListAdapter(imageList, context)
                imageView.adapter = imageListAdapter
            }

        }


    }
}
