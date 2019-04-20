package com.example.jeffrey.postcardsfromparis.view

import com.example.jeffrey.postcardsfromparis.R
import com.example.jeffrey.postcardsfromparis.model.Postcard
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder

class PostcardItem(private val postcard: Postcard): Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.partial_postcard
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        // TODO: implement stub
    }
}