package com.example.myphotos.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.myphotos.FolderFragment
import com.example.myphotos.R
import com.example.myphotos.model.FolderModel

class FolderAdapter(val context: Context): RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {
    private val folder : MutableList<FolderModel> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        return FolderViewHolder(LayoutInflater.from(context).inflate(R.layout.item_folder, parent, false))
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        holder.bindmodel(folder[position])
    }
    override fun getItemCount(): Int {
        return folder.size
    }

    fun setFolder(data: List<FolderModel>){
        folder.clear()
        folder.addAll(data)
//        notifyDataSetChanged()
//    }
//
//    inner class FolderViewHolder(view: View): RecyclerView.ViewHolder(view){
//        val itemName: TextView = view.findViewById(R.id.nameFolder)
//        val viewFolder: LinearLayout = view.findViewById(R.id.viewFolder)
//        fun bindmodel(f: FolderModel){
//            itemName.text = f.name
//
//            viewFolder.setOnClickListener(object : View.OnClickListener{
//                override fun onClick(v: View) {
//                    val activty = v.context as AppCompatActivity

                    val myFragment = FolderFragment()
                    val fragmentManager = activty.supportFragmentManager
                    val fragmentTransaction = fragmentManager.beginTransaction()
                    val arg = Bundle()
                    arg.putString("id", f.id)
                    arg.putString("nameFolder", f.name)
                    myFragment.arguments = arg
                    fragmentTransaction.replace(R.id.container, myFragment)
                    fragmentTransaction.commit()
                }
            })
        }
    }


}