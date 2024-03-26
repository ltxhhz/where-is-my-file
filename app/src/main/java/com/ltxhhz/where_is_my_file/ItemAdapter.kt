package com.ltxhhz.where_is_my_file

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class ItemAdapter(
    private val ctx: Context,
    private val itemList: List<ReceiveFile>,
    private val itemOnClick: (item: ReceiveFile) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val pathTextView: TextView = itemView.findViewById(R.id.pathTextView)
        private val uriTextView: TextView = itemView.findViewById(R.id.uriTextView)

        fun bind(item: ReceiveFile) {
            titleTextView.text = if (item.isDir) "文件夹" else item.filename
            pathTextView.text = "${if (item.possible) "可能的" else ""}路径: ${item.path}"
            uriTextView.text = "Uri: ${item.uri}"

            itemView.setOnClickListener {
                itemOnClick(item)
            }
        }
    }

}
