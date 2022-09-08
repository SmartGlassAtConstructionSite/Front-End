package com.example.safetymanagement2022.ui.list.buildingcreate

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.safetymanagement2022.R
import com.example.safetymanagement2022.common.*
import com.example.safetymanagement2022.databinding.FragmentBuildingCreate2Binding
import com.example.safetymanagement2022.model.FloorPlanData
import com.example.safetymanagement2022.ui.base.BaseFragment
import com.example.safetymanagement2022.ui.common.EventObserver
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.schedulers.Schedulers
import java.io.File

// 다이얼로그에서 값 받아오기 위한 인터페이스
interface SelectImageInterface {
    fun onSelectedImage(countIndex: Int)
}

@AndroidEntryPoint
class BuildingCreate2Fragment : BaseFragment<FragmentBuildingCreate2Binding>(R.layout.fragment_building_create_2),
    SelectImageInterface {
    private val viewModel: BuildingCreateViewModel by viewModels()

    private var arrImage = ArrayList<FloorPlanData>()
    private var bitmap: Bitmap? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        val floorMax = requireArguments().getString(KEY_BUILDING_FLOOR_MAX)?.toIntOrNull() ?: 0
        val floorMin = requireArguments().getString(KEY_BUILDING_FLOOR_MIN)?.toIntOrNull() ?: 0

        setFloorList(floorMax, floorMin)
        viewModel.setListFloorPlan(arrImage)
        checkBtnEnable()

        viewModel.listFloorPlan.observe(viewLifecycleOwner) {
            binding.rvFloorPlan.adapter =
                BuildingCreateAdapter(viewModel, this@BuildingCreate2Fragment).apply {
                    submitList(arrImage)
                }
        }
        viewModel.openButton2Event.observe(viewLifecycleOwner, EventObserver {
            openBuildingCreateFinish()
        })

        viewModel.arrS3Url.observe(viewLifecycleOwner) {
            Log.d("mmm s3 frag", it.toString())
        }

        setBackBtnClickListener()
    }

    private fun setBackBtnClickListener() {
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun checkBtnEnable() {
        for (item in arrImage)
            if (item.imageUri == null) {
                binding.btnNext.isEnabled = false
                return
            }
        binding.btnNext.isEnabled = true
    }

    private fun openBuildingCreateFinish() {
        val multiUploadHashMap = mutableMapOf<String,File>()
        for(i in 0 until arrImage.size) {
            val path = getRealPathFromURI(arrImage[i].imageUri!!).toString()
            val file = File(path)
            multiUploadHashMap[file.name] = file
        }
        uploadImageToS3(multiUploadHashMap)
    }

    private fun uploadImageToS3(map: Map<String, File>) {
        val buildingId = requireArguments().getInt(KEY_BUILDING_ID)
        MultiUploaderS3Client(
            "detectus/building-image/${viewModel.getUserId()}/$buildingId",
            requireContext(),
            viewModel
        ).uploadMultiple(map as MutableMap<String, File>)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe()
    }

    private fun setFloorList(floorMax: Int, floorMin: Int) {
        for (i in floorMax downTo 1) arrImage.add(FloorPlanData("지상 " + i + "층", null, null))
        for (i in 1..floorMin) arrImage.add(FloorPlanData("지하 " + i + "층", null,  null))
    }

    //  Adapter 에서 값 받고나서 할 작업들
    override fun onSelectedImage(countIndex: Int) {
        val intent = Intent("android.intent.action.GET_CONTENT")
        intent.type = "image/*"     // 모든 이미지
        startActivityForResult(intent, countIndex)
    }

//    @SuppressLint("CheckResult")
//    private fun uploadImageToS3(map: Map<String, File>) {
//        val buildingId: Int = requireArguments().getInt(KEY_BUILDING_ID)
//        MultiUploaderS3Client(
//            "detectus/building-image/${viewModel.getUserId()}/$buildingId",
//            requireContext()
//        ).uploadMultiple(map as MutableMap<String, File>)
//            .subscribeOn(Schedulers.io())
//            .observeOn(Schedulers.io())
//            .subscribe()
//    }

    private fun getRealPathFromURI(contentUri: Uri): String? {
        if (contentUri.path!!.startsWith("/storage"))
            return contentUri.path.toString()

        val id = DocumentsContract.getDocumentId(contentUri).split(":")[1]
        val columns = arrayOf<String>(MediaStore.Files.FileColumns.DATA)
        val selection = MediaStore.Files.FileColumns._ID + " = " + id
        val cursor = requireActivity().contentResolver.query(MediaStore.Files
            .getContentUri("external"), columns, selection, null, null)
        cursor.use {
            val columnIndex = it?.getColumnIndex(columns[0])
            if (it?.moveToFirst() == true)
                return it.getString(columnIndex!!)
        }
        return null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            val uri = data?.data    // 선택한 이미지의 주소
            if (uri != null) {
                arrImage[requestCode].imageUri = uri
                // '완료' 버튼 활성화 체크
                checkBtnEnable()
                // 이미지 색 변경
                (binding.rvFloorPlan.adapter as BuildingCreateAdapter)
                    .arrImgView[requestCode]
                    .setImageResource(R.drawable.ic_image_on)
            }
            else bitmap = null
        }
    }

}