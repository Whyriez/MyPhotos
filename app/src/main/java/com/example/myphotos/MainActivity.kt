package com.example.myphotos

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.example.myphotos.Server.Client
import com.example.myphotos.Server.helper.Constant
import com.example.myphotos.Server.helper.PreferenceHelper
import com.example.myphotos.databinding.ActivityMainBinding
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPref: PreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = PreferenceHelper(this)

        initButton()
    }

    private fun initButton() {
        binding.btnGoRegister.setOnClickListener {
            val i = Intent(this, RegisterActivity::class.java)
            startActivity(i)
        }
        binding.btnLogin.setOnClickListener {
            login()
        }
    }


    private fun login(){
        val email = binding.txtEmail.text!!.trim().toString()
        val password = binding.txtPassword.text!!.trim().toString()
        val url = URL(Client.baseUrl + "/api/auth")
        val handler = Handler(Looper.getMainLooper())
        Executors.newSingleThreadExecutor().execute(object: Runnable{
            override fun run() {
                var result = ""
                val client = url.openConnection() as HttpURLConnection
                client.requestMethod = "POST"
                client.addRequestProperty("Accept", "application/json")
                client.addRequestProperty("Content-Type", "application/json")
                client.doOutput = true

                try{
                    val requestBody = JSONObject()
                    requestBody.put("email", email)
                    requestBody.put("password", password)

                    val outputStream = client.outputStream
                    outputStream.write(requestBody.toString().toByteArray(Charsets.UTF_8))
                    outputStream.close()

                    // Baca response dari server
                    if (client.responseCode == HttpURLConnection.HTTP_OK) {
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
                                val data = JSONObject(result)
                                val signature = data.getJSONObject("data").getString("signature")
                                val token = "Bearer $signature"
                                sharedPref.put(Constant.TOKEN, token)

                                if (sharedPref.getString(Constant.TOKEN) != ""){
                                    startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                                    finish()
                                }

                            }else{
                                Toast.makeText(this@MainActivity, "Email or Password is invalid", Toast.LENGTH_SHORT).show()
                            }

                        } catch (e: Exception){
                            Client.message("Email or Password is invalid", this@MainActivity)
                        }
                    }
                })
            }
        })
    }
}