package ru.gamebreaker.tabladeanuncioskotlin.presentation.fragments

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.gamebreaker.tabladeanuncioskotlin.R
import ru.gamebreaker.tabladeanuncioskotlin.presentation.EditAdsAct
import ru.gamebreaker.tabladeanuncioskotlin.databinding.ListImageFragBinding
import ru.gamebreaker.tabladeanuncioskotlin.presentation.dialoghelper.ProgressDialog
import ru.gamebreaker.tabladeanuncioskotlin.utils.AdapterCallBack
import ru.gamebreaker.tabladeanuncioskotlin.utils.ImageManager
import ru.gamebreaker.tabladeanuncioskotlin.utils.ImagePicker
import ru.gamebreaker.tabladeanuncioskotlin.utils.ItemTouchMoveCallBack

class ImageListFragment : Fragment(), AdapterCallBack {

    val adapter = SelectImageRvAdapter(this)
    val dragCallback = ItemTouchMoveCallBack(adapter)
    val touchHelper = ItemTouchHelper(dragCallback)
    private var job: Job? = null
    private var addImageItem: MenuItem? = null
    lateinit var binding: ListImageFragBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ListImageFragBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onItemDelete() {
        addImageItem?.isVisible = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
        binding.apply {
            touchHelper.attachToRecyclerView(rcViewSelectImage)
            rcViewSelectImage.layoutManager = LinearLayoutManager(activity)
            rcViewSelectImage.adapter = adapter
        }

    }

    fun updateAdapterFromEdit(bitmapList: List<Bitmap>) {
        adapter.updateAdapter(bitmapList, true)
    }

    fun resizeSelectedImages(newList: ArrayList<Uri>, needClear: Boolean, activity: Activity) {
        job = CoroutineScope(Dispatchers.Main).launch {
            val dialog = ProgressDialog.createProgressDialog(activity)
            val bitmapList = ImageManager.imageResize(newList, activity)
            dialog.dismiss()
            adapter.updateAdapter(bitmapList, needClear)
            if (adapter.mainArray.size > 2) addImageItem?.isVisible = false
        }
    }

    private fun setUpToolbar() {

        binding.apply {
            tb.inflateMenu(R.menu.menu_choose_image)
            val deleteItem = tb.menu.findItem(R.id.id_delete_image)
            addImageItem = tb.menu.findItem(R.id.id_add_image)
            if (adapter.mainArray.size > 2) addImageItem?.isVisible = false

            deleteItem.setOnMenuItemClickListener {
                adapter.updateAdapter(ArrayList(), true)
                addImageItem?.isVisible = true
                true
            }

            addImageItem?.setOnMenuItemClickListener {
                val imageCount = ImagePicker.MAX_IMAGE_COUNT - adapter.mainArray.size
                ImagePicker.addImages(activity as EditAdsAct, imageCount)
                true
            }
        }
    }

    fun updateAdapter(newList: ArrayList<Uri>, activity: Activity) {
        resizeSelectedImages(newList, false, activity)
    }

    fun setSingleImage(uri: Uri, pos: Int) {
        val pBar = binding.rcViewSelectImage[pos].findViewById<ProgressBar>(R.id.pBar)
        job = CoroutineScope(Dispatchers.Main).launch {
            pBar.visibility = View.VISIBLE
            val bitmapList = ImageManager.imageResize(arrayListOf(uri), activity as Activity)
            pBar.visibility = View.GONE
            adapter.mainArray[pos] = bitmapList[0]
            adapter.notifyItemChanged(pos)
        }
    }
}