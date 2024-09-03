package com.example.myphotos

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myphotos.Server.Client
import com.example.myphotos.Server.helper.Constant
import com.example.myphotos.Server.helper.PreferenceHelper
import com.example.myphotos.adapter.FolderAdapter
import com.example.myphotos.adapter.PhotoAdapter
import com.example.myphotos.databinding.FragmentHomeBinding
import com.example.myphotos.model.FolderModel
import com.example.myphotos.model.ImageModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors


class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    private lateinit var folderAdapter: FolderAdapter
    private lateinit var listFoder : RecyclerView
    private val addFolder: MutableList<FolderModel> = ArrayList()

    private lateinit var imageAdapter: PhotoAdapter
    private lateinit var listImage : RecyclerView
    private val addImage: MutableList<ImageModel> = ArrayList()

    private lateinit var sharedPref: PreferenceHelper

    private lateinit var btnAdd: FloatingActionButton
    private lateinit var btnUploadPhoto: FloatingActionButton
    private lateinit var btnCreateAlbum: FloatingActionButton

    private var isExpanded = false


    private val fromBottomFabAnim: Animation by lazy {
        AnimationUtils.loadAnimation(requireContext(), R.anim.from_bottom)
    }
    private val toBottomFabAnim: Animation by lazy {
        AnimationUtils.loadAnimation(requireContext(), R.anim.to_bottom)
    }
    private val rotateClockWise: Animation by lazy {
        AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_clock_wise)
    }
    private val rotateClockAntiWise: Animation by lazy {
        AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_anti_clock_wise)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = FragmentHomeBinding.inflate(inflater, container, false)
        init(view)

        sharedPref = PreferenceHelper(requireContext())
        folderAdapter = FolderAdapter(requireContext())
        imageAdapter = PhotoAdapter(requireContext())
        setupRecycleFolder()
        setupRecyclePhotos()

        initButton()
        return view.root
    }

    private fun initButton() {
        btnAdd.setOnClickListener {
            if(isExpanded){
                displayFab()
            }else{
                hideFab()
            }
        }

        btnCreateAlbum.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            val dialogLayout = activity?.layoutInflater!!.inflate(R.layout.ly_edit_text, null)
            val editText = dialogLayout.findViewById<EditText>(R.id.txt_add_album)


            with(builder){
                setTitle("Create Album")
                setPositiveButton("Ok"){dialog, which->
                    var nameAlbum = editText.text.toString()
                    createAlbum(nameAlbum)
                }
                setNegativeButton("Cencel"){dialog, which->
                    dialog.dismiss()
                }
                setView(dialogLayout)
                show()
            }
        }
        btnUploadPhoto.setOnClickListener {
            val i = Intent(requireActivity(), UploadActivity::class.java)
            startActivity(i)
        }
    }

    private fun displayFab() {
        btnAdd.startAnimation(rotateClockAntiWise)
        btnUploadPhoto.startAnimation(toBottomFabAnim)
        btnCreateAlbum.startAnimation(toBottomFabAnim)
        isExpanded = !isExpanded
    }

    private fun hideFab() {
        btnAdd.startAnimation(rotateClockWise)
        btnUploadPhoto.startAnimation(fromBottomFabAnim)
        btnCreateAlbum.startAnimation(fromBottomFabAnim)
        isExpanded = !isExpanded
    }

    override fun onResume() {
        super.onResume()
        getMe()
    }

    private fun createAlbum(nameAlbum: String){
        val url = URL(Client.baseUrl + "/api/albums")
        val handler = Handler(Looper.getMainLooper())
        Executors.newSingleThreadExecutor().execute(object: Runnable{
            override fun run() {
                var result = ""
                val client = url.openConnection() as HttpURLConnection
                client.requestMethod = "POST"
                client.addRequestProperty("Authorization", sharedPref.getString(Constant.TOKEN))
                client.addRequestProperty("Accept", "text/plain")
                client.addRequestProperty("Content-Type", "application/json")
                client.doOutput = true

                try{
                    val requestBody = JSONObject()
                    requestBody.put("name", nameAlbum)

                    val outputStream = client.outputStream
                    outputStream.write(requestBody.toString().toByteArray(Charsets.UTF_8))
                    outputStream.close()

                    // Baca response dari server
                    if (client.responseCode == HttpURLConnection.HTTP_CREATED) {
                            result = client.inputStream.bufferedReader().use { it.readText() }
                    } else {
                            result = client.errorStream.bufferedReader().use { it.readText() }
                    }
                } catch (e: Exception){
                    e.printStackTrace()
                } finally {
                    client.disconnect()
                }

                handler.post(object: Runnable{
                    override fun run() {
                        try{
                            if(result != ""){
                                getMe()
                            }else{
                                Toast.makeText(requireContext(), "No Result", Toast.LENGTH_SHORT).show()
                            }

                        } catch (e: Exception){
                            Client.message("Error", requireActivity())
                        }
                    }
                })
            }
        })
    }

    private fun getMe() {
        val url = URL(Client.baseUrl + "/api/users/me")
        val handler = Handler(Looper.getMainLooper())
        Executors.newSingleThreadExecutor().execute(object: Runnable{
            override fun run() {
                var result = ""
                val client = url.openConnection() as HttpURLConnection
                client.requestMethod = "GET"
                client.addRequestProperty("Authorization", sharedPref.getString(Constant.TOKEN))
                client.addRequestProperty("Accept", "application/json")
                client.addRequestProperty("Content-Type", "application/json")

                try{
                    result = client.inputStream.bufferedReader().use { it.readText() }
                } catch (e: Exception){
                    result = client.errorStream.bufferedReader().use{it.readText()}
                }

                handler.post(object: Runnable {
                    override fun run() {
                        if (result != "") {
                            val jsonObject = JSONObject(result)
                            val data = jsonObject.getJSONObject("data")
                            val id = data.getString("id")
                            val username = data.getString("username")
                            getFolder(id)
                            getImage(id)
                            sharedPref.put(Constant.USERID, id)
                            sharedPref.put(Constant.USERNAME, username)
                        }
                    }
                })
            }
        })
    }

    private fun getFolder(id:String) {
        addFolder.clear()
        val url = URL(Client.baseUrl + "/api/users/$id/albums")
        val handler = Handler(Looper.getMainLooper())
        Executors.newSingleThreadExecutor().execute(object: Runnable{
            override fun run() {
                var result = ""
                val client = url.openConnection() as HttpURLConnection
                client.requestMethod = "GET"
                client.addRequestProperty("Authorization", sharedPref.getString(Constant.TOKEN))
                client.addRequestProperty("Accept", "application/json")
                client.addRequestProperty("Content-Type", "application/json")

                try{
                    result = client.inputStream.bufferedReader().use { it.readText() }
                } catch (e: Exception){
                    result = client.errorStream.bufferedReader().use{it.readText()}
                }

                handler.post(object: Runnable {
                    override fun run() {
                        if (result != "") {
                            val jsonObject = JSONObject(result)
                            val data = jsonObject.getJSONArray("data")
                            for (c in 0 until data.length()){
                                val table = data.getJSONObject(c)
                                addFolder.add(
                                    FolderModel(
                                    table.getString("id"),
                                    table.getString("user_id"),
                                    table.getString("name"),
                                )
                                )
                                folderAdapter.setFolder(addFolder)
                            }

                        }
                    }
                })
            }
        })
    }


    private fun getImage(id:String) {
        addImage.clear()
//        val id = sharedPref.getString(Constant.USERID)
        val url = URL(Client.baseUrl + "/api/users/$id/photos")
        val handler = Handler(Looper.getMainLooper())
        Executors.newSingleThreadExecutor().execute(object: Runnable{
            override fun run() {
                var result = ""
                val client = url.openConnection() as HttpURLConnection
                client.requestMethod = "GET"
                client.addRequestProperty("Authorization", sharedPref.getString(Constant.TOKEN))
                client.addRequestProperty("Accept", "application/json")
                client.addRequestProperty("Content-Type", "application/json")

                try{
                    result = client.inputStream.bufferedReader().use { it.readText() }
                } catch (e: Exception){
                    result = client.errorStream.bufferedReader().use{it.readText()}
                }

                handler.post(object: Runnable {
                    override fun run() {
                        if (result != "") {
                            val jsonObject = JSONObject(result)
                            val data = jsonObject.getJSONArray("data")
                            for (c in 0 until data.length()){
                                val image = data.getJSONObject(c)
                                val imageId = image.getString("id")
                                val albumId = image.getString("album_id")
                                val imageName = image.getString("name")
                                val imageUrl = image.getString("image")

                                val imageUrlApi = URL(Client.baseUrl + "/api/photos/$imageUrl/image")
                                val handler = Handler(Looper.getMainLooper())
                                Executors.newSingleThreadExecutor().execute(object: Runnable{
                                    override fun run() {
                                        var resultImage = ""
                                        val client = imageUrlApi.openConnection() as HttpURLConnection
                                        client.requestMethod = "GET"
                                        client.addRequestProperty("Accept", "application/json")
                                        client.addRequestProperty("Content-Type", "application/json")

                                        try{
                                            val inputStream: InputStream = client.inputStream

                                            // Mendapatkan bitmap dari input stream
                                            val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)

                                            // Mengonversi bitmap ke format base64
                                            val byteArrayOutputStream = ByteArrayOutputStream()
                                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                                            val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
                                            val base64Image: String = Base64.encodeToString(byteArray, Base64.DEFAULT)
                                            resultImage = base64Image
                                        } catch (e: Exception){
                                            resultImage = client.errorStream.bufferedReader().use{it.readText()}
                                        }

                                        handler.post(object: Runnable{
                                            override fun run() {
                                                if(resultImage != ""){
                                                    addImage.add(
                                                        ImageModel(imageId,
                                                        albumId,
                                                        imageName, resultImage)
                                                    )
                                                    imageAdapter.setPhoto(addImage)
                                                }
                                            }
                                        })
                                    }
                                })

                            }

                        }
                    }
                })
            }
        })
    }

    private fun init(view: FragmentHomeBinding) {
        listFoder = view.recycleFolder
        listImage = view.recyclePhoto
        btnAdd = view.btnAdd
        btnCreateAlbum = view.btnCreateAlbum
        btnUploadPhoto = view.btnUploadPhoto
    }

    private fun setupRecycleFolder() {
        listFoder.layoutManager = GridLayoutManager(activity, 2)
        listFoder.adapter = folderAdapter
        folderAdapter.notifyDataSetChanged()
    }

    private fun setupRecyclePhotos() {
        listImage.layoutManager = GridLayoutManager(activity, 2)

        listImage.adapter = imageAdapter
        imageAdapter.notifyDataSetChanged()
    }


}