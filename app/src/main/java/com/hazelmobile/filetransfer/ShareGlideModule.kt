package com.hazelmobile.filetransfer

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.load.resource.drawable.DrawableResource
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.signature.ObjectKey
import com.bumptech.glide.util.Util
import java.io.IOException

@GlideModule
class ShareGlideModule: AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        super.registerComponents(context, glide, registry)

        registry.append(
            ApplicationInfo::class.java, ApplicationInfo::class.java,
            object : ModelLoaderFactory<ApplicationInfo, ApplicationInfo> {
                override fun build(
                    multiFactory: MultiModelLoaderFactory
                ): ModelLoader<ApplicationInfo, ApplicationInfo> {
                    return ApplicationIconModelLoader()
                }

                override fun teardown() {

                }
            }).append(
            ApplicationInfo::class.java,
            Drawable::class.java,
            ApplicationIconDecoder(context)
        )
    }

    private inner class ApplicationIconModelLoader : ModelLoader<ApplicationInfo, ApplicationInfo> {
        override fun buildLoadData(
            applicationInfo: ApplicationInfo,
            width: Int,
            height: Int,
            options: Options
        ): ModelLoader.LoadData<ApplicationInfo>? {
            return ModelLoader.LoadData(ObjectKey(applicationInfo), object :
                DataFetcher<ApplicationInfo> {
                override fun loadData(
                    priority: Priority,
                    callback: DataFetcher.DataCallback<in ApplicationInfo>
                ) {
                    callback.onDataReady(applicationInfo)
                }

                override fun cleanup() {

                }

                override fun cancel() {

                }

                override fun getDataClass(): Class<ApplicationInfo> {
                    return ApplicationInfo::class.java
                }

                override fun getDataSource(): DataSource {
                    return DataSource.LOCAL
                }
            })
        }

        override fun handles(applicationInfo: ApplicationInfo): Boolean {
            return true
        }
    }

    private inner class ApplicationIconDecoder(private val context: Context) :
        ResourceDecoder<ApplicationInfo, Drawable> {

        @Throws(IOException::class)
        override fun decode(
            source: ApplicationInfo,
            width: Int,
            height: Int,
            options: Options
        ): Resource<Drawable>? {
            val icon = source.loadIcon(context.packageManager)
            return object : DrawableResource<Drawable>(icon) {
                override fun getResourceClass(): Class<Drawable> {
                    return Drawable::class.java
                }

                override fun getSize(): Int {
                    return if (drawable is BitmapDrawable) Util.getBitmapByteSize(drawable.bitmap) else 1

                }

                override fun recycle() {}
            }
        }

        @Throws(IOException::class)
        override fun handles(source: ApplicationInfo, options: Options): Boolean {
            return true
        }
    }
}