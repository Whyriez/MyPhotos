package com.example.myphotos

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import com.example.myphotos.Server.Client
import com.example.myphotos.Server.helper.Constant
import com.example.myphotos.Server.helper.PreferenceHelper
import com.example.myphotos.databinding.ActivityMainBinding
import com.example.myphotos.databinding.ActivityUploadBinding
import org.json.JSONObject
import java.io.*
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.Executors

class UploadActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUploadBinding
    private lateinit var sharedPref: PreferenceHelper

    var b : Bundle? = null

    var uri: Uri? = null
    var file: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = PreferenceHelper(this)

        val username  = sharedPref.getString(Constant.USERNAME)
        binding.nameUsers.text = "Hello, $username"
        b = intent.extras
        initButton()
    }

    private fun initButton() {
        binding.btnBackHome.setOnClickListener {
            onBackPressed()
        }
        binding.btnUpload.setOnClickListener {
            startActivityForResult(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI), 100)
        }
        binding.btnSubmitPhoto.setOnClickListener {
            uploadImage()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 100 && resultCode == RESULT_OK ){
            uri = data?.data
            var path = ""
            val cursor = contentResolver.query(uri!!, null,null,null,null)
            if(cursor!!.moveToFirst()){
                val column = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                path = cursor.getString(column)
            }
            binding.btnUpload.setImageURI(uri)
            file = File(cacheDir, path)
        }
    }

    fun uploadImage() {
        if (b != null) {
            val idAlbum = b!!.getString("id").toString()

            val url = URL(Client.baseUrl + "/api/photos")
            val boundary = UUID.randomUUID().toString()
            val handler = Handler(Looper.getMainLooper())
            Executors.newSingleThreadExecutor().execute(object: Runnable{
                override fun run() {
                    var result = ""
                    val client = url.openConnection() as HttpURLConnection
                    client.requestMethod = "POST"
                    client.addRequestProperty("Authorization", sharedPref.getString(Constant.TOKEN))
                    client.addRequestProperty("Accept", "application/json")
                    client.addRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                    client.doInput = true
                    client.doOutput = true

                    val writer = BufferedWriter(OutputStreamWriter(client.outputStream))




                    if(idAlbum != ""){
                        writer.write("\r\n--$boundary\r\n")
                        writer.write("Content-Disposition: form-data; name=\"album_id\"")
                        writer.write("\r\n\r\n")
                        writer.write(idAlbum)
                    }
                    writer.write("\r\n--$boundary\r\n")
                    writer.write("Content-Disposition: form-data; name=\"files\";")
                    writer.write("filename=\"" + file?.name + "\"")
                    writer.write("\r\n\r\n")
                    writer.flush()

                    val descriptor = contentResolver.openFileDescriptor(uri!!, "r", null)?: return
                    val input = FileInputStream(descriptor.fileDescriptor)
                    var byte: Int
                    val buffer = ByteArray(1024)
                    while (input.read(buffer).also { byte = it } != -1){
                        client.outputStream.write(buffer, 0, byte)
                    }
                    client.outputStream.flush()

                    writer.write("\r\n--$boundary--\r\n")
                    writer.flush()

                    client.outputStream.close()
                    writer.close()

                    try{
                        result = client.inputStream.bufferedReader().use{it.readText()}
                    }catch (e: Exception){
                        result = client.errorStream.bufferedReader().use{it.readText()}
                    }

                    handler.post(object: Runnable{
                        override fun run() {
                            Client.message(result,this@UploadActivity)
                        }
                    })
                }
            })
        }
    }


}