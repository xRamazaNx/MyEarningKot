package ru.developer.press.myearningkot.dialogs

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.Dialog
import android.content.ClipData
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import kotlinx.android.synthetic.main.edit_cell_image.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.dip
import org.jetbrains.anko.support.v4.toast
import ru.developer.press.myearningkot.R
import ru.developer.press.myearningkot.adapters.AdapterViewPagerFromImageCell
import ru.developer.press.myearningkot.helpers.filesFolder
import ru.developer.press.myearningkot.helpers.setAlertButtonColors
import ru.developer.press.myearningkot.model.Column
import ru.developer.press.myearningkot.model.ImageTypeValue
import java.io.File


class DialogEditImageCell(
    private val column: Column,
    value: String,
    private val changed: (sourceValue: String) -> Unit
) : DialogFragment() {
    private val imageFolder = "${filesFolder}images/".also { File(it).mkdirs() }
    private val imageValue: ImageTypeValue = Gson().fromJson(value, ImageTypeValue::class.java)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return getImageDialog()

    }

    private fun getImageDialog(): AlertDialog {
        val alertDialog = getAlertDialog().apply {
            activity?.apply {
                val imageViewer = getImageViewer()
                setView(imageViewer)
            }
        }.create()
        alertDialog.setAlertButtonColors(R.color.colorAccent, R.color.colorAccent)
        return alertDialog
    }

    private fun initImageViewer(imageViewer: View) {
        val changeImage = imageValue.changeImage
        val viewPager = imageViewer.imagesPager
        val tabs = imageViewer.imagesTabs

        val activity = requireActivity()
        val adapter = AdapterViewPagerFromImageCell(
            fragmentManager = activity.supportFragmentManager,
            lifecycle = lifecycle,
            imageUriList = imageValue.imagePathList
        )
        viewPager.adapter = adapter
        val dpsToPixels = activity.dip(48)
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            val image = ImageView(activity).apply {
                layoutParams = TableLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                ).apply {
                    weight = 0f
                }
            }
            tab.customView = image
            Glide
                .with(this)
                .load(imageValue.imagePathList[position])
                .error(R.drawable.ic_image_error)
                .fitCenter()
                .into(image)
            tab.view.layoutParams =
                LinearLayout.LayoutParams(
                    dpsToPixels,
                    dpsToPixels
                ).apply {
                    weight = 0f
                }
        }.attach()
        viewPager.post {
            if (imageValue.imagePathList.isNotEmpty()
                && changeImage > -1
                && changeImage < imageValue.imagePathList.size
            ) {
                tabs.getTabAt(changeImage)?.select()
            }
            // оно должно быть тут чтоб выбиралась позиция после того как мы перешли на ту что была выбрана
            tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(tab: TabLayout.Tab?) {

                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }

                override fun onTabSelected(tab: TabLayout.Tab?) {
                    imageValue.changeImage = tab?.position ?: -1
                }
            })
        }
        imageViewer.imageDeleteButton.setOnClickListener {
            if (imageValue.imagePathList.isEmpty())
                return@setOnClickListener
            val selectedTabPosition = tabs.selectedTabPosition
            imageValue.removePath(selectedTabPosition)
            initImageViewer(imageViewer)
        }
    }

    private fun FragmentActivity.getImageViewer(): View? {
        val imageViewer = layoutInflater.inflate(R.layout.edit_cell_image, null)
        imageViewer.title.text = column.name

        initImageViewer(imageViewer)

        imageViewer.imagePickButton.setOnClickListener {
            val intent = Intent()

            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            this.startActivityForResult(
                Intent.createChooser(intent, "Select Picture"),
                PICK_IMAGE_MULTIPLE
            )
        }
        return imageViewer
    }

    private fun getAlertDialog(): AlertDialog.Builder {
        return AlertDialog.Builder(context).apply {
            setPositiveButton(R.string.OK) { _: DialogInterface, _: Int ->
                changed(Gson().toJson(imageValue))
            }
            setNegativeButton(R.string.CANCEL) { _: DialogInterface, _: Int ->

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val imageUriList = mutableListOf<Uri>()
        try {
            if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == RESULT_OK && null != data
            ) {
                if (data.data != null) {
                    val mImageUri = data.data
                    mImageUri?.let { imageUriList.add(it) }
                } else {
                    if (data.clipData != null) {
                        val mClipData: ClipData = data.clipData!!
                        for (i in 0 until mClipData.itemCount) {
                            val item: ClipData.Item = mClipData.getItemAt(i)
                            val uri: Uri = item.uri
                            imageUriList.add(uri)


                        }
                    }
                }
            }
        } catch (e: Exception) {
            toast("Ошибка при добавлении фото.")
        }

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                imageUriList.forEach {
                    val openInputStream = requireContext().contentResolver.openInputStream(it)
                    val nameFile = it.path?.substringAfterLast('/')
                    val file =
                        File(imageFolder + nameFile)

                    if (!file.exists())
                        openInputStream?.use { input ->
                            file.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                    imageValue.imagePathList.add(0, file.path)
                }
            }
            dialog?.let {
                imageValue.changeImage = 0
                val layout = it.findViewById<LinearLayout>(R.id.imageViewerContainer)
                initImageViewer(layout)
            }
        }

//        1 не копировать файл если такой есть +++
//        2 после удаления или перемещения выбрать ближайщую картинку с права, если с право нет то последнюю
//        3 добавить кнопки перемещения
    }

    private fun File.copyTo(file: File) {
        inputStream().use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        this.dialog?.apply {
            context.let {
                window?.setBackgroundDrawable(
                    ColorDrawable(
                        ContextCompat.getColor(
                            it,
                            R.color.colorDialogBackground
                        )
                    )
                )
            }
        }
    }
}