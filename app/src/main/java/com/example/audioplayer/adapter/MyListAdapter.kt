package com.example.audioplayer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.appcompat.widget.AppCompatTextView
import com.example.audioplayer.R

class MyListAdapter(private val context: Context, private val strings:MutableList<String>) : BaseAdapter(){
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val viewHolder:ViewHolder
            val view:View
            if (convertView != null){
                viewHolder = convertView.tag as ViewHolder
                view = convertView
            }else{
                view = LayoutInflater.from(context).inflate(R.layout.item_group,parent,false)
                viewHolder = ViewHolder(view)
                view.tag = viewHolder
            }
            viewHolder.tvContent.text = getItem(position) as String
            return view
        }

        override fun getItem(position: Int): Any =
            strings[position]


        override fun getItemId(position: Int): Long = position.toLong()

        override fun getCount(): Int = strings.size

        class ViewHolder(itemView:View){
            val tvContent: AppCompatTextView = itemView.findViewById(R.id.tv_content)

        }
    }