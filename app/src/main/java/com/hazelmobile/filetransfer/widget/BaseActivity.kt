//package com.hazelmobile.filetransfer.widget
//
//import android.app.Activity
//import android.content.ComponentName
//import android.content.Context
//import android.content.Intent
//import android.content.ServiceConnection
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import android.graphics.Canvas
//import android.graphics.drawable.Drawable
//import android.os.Bundle
//import android.os.IBinder
//import android.widget.ImageView
//import androidx.appcompat.app.AppCompatActivity
//import com.bumptech.glide.request.Request
//import com.bumptech.glide.request.target.SizeReadyCallback
//import com.bumptech.glide.request.target.Target
//import com.bumptech.glide.request.transition.Transition
//import com.hazelmobile.filetransfer.GlideApp
//import com.hazelmobile.filetransfer.files.AppConfig
//import com.hazelmobile.filetransfer.pictures.AppUtils
//import com.hazelmobile.filetransfer.service.WorkerService
//import java.io.FileNotFoundException
//
//abstract class BaseActivity : AppCompatActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//    }
//
//    fun requestProfilePictureChange() {
//        startActivityForResult(
//            Intent(Intent.ACTION_PICK).setType("image/*"),
//            REQUEST_PICK_PROFILE_PHOTO
//        )
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == REQUEST_PICK_PROFILE_PHOTO)
//            if (resultCode == Activity.RESULT_OK && data != null) {
//                val chosenImageUri = data.data
//
//                if (chosenImageUri != null) {
//                    GlideApp.with(this)
//                        .load(chosenImageUri)
//                        .centerCrop()
//                        .override(200, 200)
//                        .into(object : Target<Drawable> {
//                            override fun onLoadStarted(placeholder: Drawable?) {
//
//                            }
//
//                            override fun onLoadFailed(errorDrawable: Drawable?) {
//
//                            }
//
//                            override fun onResourceReady(
//                                resource: Drawable,
//                                transition: Transition<in Drawable>?
//                            ) {
//                                try {
//                                    val bitmap = Bitmap.createBitmap(
//                                        AppConfig.PHOTO_SCALE_FACTOR,
//                                        AppConfig.PHOTO_SCALE_FACTOR,
//                                        Bitmap.Config.ARGB_8888
//                                    )
//                                    val canvas = Canvas(bitmap)
//                                    val outputStream =
//                                        openFileOutput("profilePicture", Context.MODE_PRIVATE)
//
//                                    resource.setBounds(0, 0, canvas.width, canvas.height)
//                                    resource.draw(canvas)
//                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
//
//                                    outputStream.close()
//
//                                    notifyUserProfileChanged()
//                                } catch (error: Exception) {
//                                    error.printStackTrace()
//                                }
//
//                            }
//
//                            override fun onLoadCleared(placeholder: Drawable?) {
//
//                            }
//
//                            override fun getSize(cb: SizeReadyCallback) {
//
//                            }
//
//                            override fun removeCallback(cb: SizeReadyCallback) {
//
//                            }
//
//                            override fun getRequest(): Request? {
//                                return null
//                            }
//
//                            override fun setRequest(request: Request?) {
//
//                            }
//
//                            override fun onStart() {
//
//                            }
//
//                            override fun onStop() {
//
//                            }
//
//                            override fun onDestroy() {
//
//                            }
//                        })
//                }
//            }
//    }
//
//    fun attachRunningTask(task: WorkerService.RunningTask?) {
//        synchronized(mAttachedTasks) {
//            mAttachedTasks.add(task)
//        }
//    }
//
//    fun checkForTasks(): Boolean {
//        val serviceConnection = object : ServiceConnection {
//            override fun onServiceConnected(name: ComponentName, service: IBinder) {
//                val workerService = (service as WorkerService.LocalBinder).getService()
//
//                val task = workerService
//                    .findTaskByHash(WorkerService.intentHash(intent))
//
//                onPreviousRunningTask(task)
//
//                if (task != null)
//                    synchronized(mAttachedTasks) {
//                        attachRunningTask(task)
//                    }
//
//                unbindService(this)
//            }
//
//            override fun onServiceDisconnected(name: ComponentName) {
//
//            }
//        }
//
//        return bindService(
//            Intent(this@BaseActivity, WorkerService::class.java),
//            serviceConnection, Context.BIND_AUTO_CREATE
//        )
//    }
//
//    fun loadProfilePictureInto(deviceName: String, imageView: ImageView) {
//        try {
//            val inputStream = openFileInput("profilePicture")
//            val bitmap = BitmapFactory.decodeStream(inputStream)
//
//            GlideApp.with(this)
//                .load(bitmap)
//                .circleCrop()
//                .into(imageView)
//        } catch (e: FileNotFoundException) {
//            e.printStackTrace()
//            imageView.setImageDrawable(AppUtils.getDefaultIconBuilder(this).buildRound(deviceName))
//        }
//
//    }
//
//    fun notifyUserProfileChanged() {
//        if (!isFinishing)
//            runOnUiThread { onUserProfileUpdated() }
//    }
//
//    open fun onUserProfileUpdated() {
//
//    }
//
//    interface OnBackPressedListener {
//        fun onBackPressed(): Boolean
//    }
//
//    companion object {
//
//        @JvmStatic
//        val REQUEST_PICK_PROFILE_PHOTO = 1000
//    }
//
//}
