package ru.gamebreaker.tabladeanuncioskotlin.utils

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.gamebreaker.tabladeanuncioskotlin.presentation.adapters.ImageAdapter
import ru.gamebreaker.tabladeanuncioskotlin.domain.model.Ad

object ImageManager {

    private const val MAX_IMAGE_SIZE = 1000
    private const val WIDTH = 0
    private const val HEIGHT = 1

    fun getImageSize(uri: Uri, act: Activity): List<Int> {
        val inStream = act.contentResolver.openInputStream(uri)
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(inStream, null, options)
        return listOf(options.outWidth, options.outHeight)
    }

    fun chooseScaleType(im: ImageView, bitmap: Bitmap) {
        if (bitmap.width > bitmap.height) {
            im.scaleType = ImageView.ScaleType.CENTER_CROP
        } else {
            im.scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
    }

    suspend fun imageResize(uris: List<Uri>, act: Activity): List<Bitmap> =
        withContext(Dispatchers.IO) {
            val tempList = ArrayList<List<Int>>()
            val bitmapList = ArrayList<Bitmap>()
            for (n in uris.indices) {
                val size = getImageSize(uris[n], act)
                Log.d("MyLog", "Width : ${size[WIDTH]} Height : ${size[HEIGHT]}")
                val imageRatio = size[WIDTH].toFloat() / size[HEIGHT].toFloat()
                if (imageRatio > 1) {
                    if (size[WIDTH] > MAX_IMAGE_SIZE) {
                        tempList.add(listOf(MAX_IMAGE_SIZE, (MAX_IMAGE_SIZE / imageRatio).toInt()))
                    } else {
                        tempList.add(listOf(size[WIDTH], size[HEIGHT]))
                    }
                } else {
                    if (size[HEIGHT] > MAX_IMAGE_SIZE) {
                        tempList.add(listOf((MAX_IMAGE_SIZE * imageRatio).toInt(), MAX_IMAGE_SIZE))
                    } else {
                        tempList.add(listOf(size[WIDTH], size[HEIGHT]))
                    }
                }
                Log.d("MyLog", "Width : ${tempList[n][WIDTH]} Height : ${tempList[n][HEIGHT]}")
            }
            for (i in uris.indices) {
                kotlin.runCatching {
                    bitmapList.add(
                        Picasso.get().load(uris[i])
                            .get()
                    )
                }
            }
            return@withContext bitmapList
        }

    private suspend fun getBitmapFromUris(uris: List<String?>): List<Bitmap> =
        withContext(Dispatchers.IO) {
            val bitmapList = ArrayList<Bitmap>()
            for (i in uris.indices) {
                kotlin.runCatching {
                    bitmapList.add(Picasso.get().load(uris[i]).get())
                }
            }
            return@withContext bitmapList
        }

    fun fillImageArray(ad: Ad, adapter: ImageAdapter) {
        val listUris = listOf(ad.mainImage, ad.secondImage, ad.thirdImage)
        CoroutineScope(Dispatchers.Main).launch {
            val bitMapList = getBitmapFromUris(listUris)
            adapter.update(bitMapList as ArrayList<Bitmap>)
        }
    }
}