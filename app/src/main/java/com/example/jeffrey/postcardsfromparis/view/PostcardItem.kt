package com.example.jeffrey.postcardsfromparis.view

import android.net.Uri
import com.example.jeffrey.postcardsfromparis.R
import com.example.jeffrey.postcardsfromparis.model.Postcard
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.loadImage
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.partial_postcard_back.view.*
import kotlinx.android.synthetic.main.partial_postcard_front.view.*

class PostcardItem(private val postcard: Postcard, var showFront: Boolean = true): Item<ViewHolder>() {

    override fun getLayout(): Int {
        return if(showFront) {
            R.layout.partial_postcard_front
        } else {
            R.layout.partial_postcard_back
        }
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.apply {
            if(showFront) {
                val postcardUri = Uri.parse(postcard.imgUrl)
                loadImage(postcardUri, partial_postcard_front_img_postcard_picture)
            } else {
                partial_postcard_back_txt_name.text = postcard.author.name
                partial_postcard_back_txt_location.text = postcard.location

                val profileUri = Uri.parse(postcard.author.imgUrl)
                loadImage(profileUri, partial_postcard_back_img_profile_picture)

                partial_postcard_back_txt_postcard_message.text = postcard.message
            }
        }
    }
}