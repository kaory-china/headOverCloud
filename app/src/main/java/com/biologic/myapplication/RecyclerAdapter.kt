package com.biologic.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.biologic.myapplication.domain.PulpContent

class RecyclerAdapter(private val contentList: ArrayList<PulpContent>) :
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    private var clickListener: ClickListener<PulpContent>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.card_layout, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerAdapter.ViewHolder, position: Int) {
        val content = contentList[position]
        holder.fileName.text = contentList[position].relative_path.toString()

        holder.itemView.setOnClickListener {
        clickListener!!.onClickListener(content)
        }
    }

    override fun getItemCount(): Int {
        return contentList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileName: TextView
        val button: Button

        init {
            fileName = itemView.findViewById(R.id.file_name)
            button = itemView.findViewById(R.id.delete_button)
        }
    }

    fun setOnClickListener(clickListener: ClickListener<PulpContent>) {
        this.clickListener = clickListener
    }
}

interface ClickListener<T> {
    fun onClickListener(data: T)
}