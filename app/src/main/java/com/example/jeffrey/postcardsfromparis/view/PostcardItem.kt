package com.example.jeffrey.postcardsfromparis.view

import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.support.v4.content.ContextCompat
import com.example.jeffrey.postcardsfromparis.R
import com.example.jeffrey.postcardsfromparis.model.Postcard
import com.example.jeffrey.postcardsfromparis.util.SharedUtil.loadImage
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.partial_postcard_back.view.*
import kotlinx.android.synthetic.main.partial_postcard_front.view.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * This class provides the visual representation of a postcard
 *
 * @property postcard the postcard to display
 * @property showFront whether the front or back should be shown
 */
class PostcardItem(private val postcard: Postcard, var showFront: Boolean = true): Item<ViewHolder>() {

    /**
     * Returns either the front or back layout of the [postcard]
     *
     * @return the layout to display
     */
    override fun getLayout(): Int {
        return if(showFront) {
            R.layout.partial_postcard_front
        } else {
            R.layout.partial_postcard_back
        }
    }

    /**
     * Binds the attributes of the [postcard] to the display resource
     *
     * @param viewHolder a view holder
     * @param position unused parameter in this implementation
     */
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.apply {
            if(showFront) {
                // Bind postcard picture to front
                val postcardUri = Uri.parse(postcard.imgUrl)
                loadImage(postcardUri, partial_postcard_front_img_postcard_picture)
            } else {
                // Bind author and postcard information to back
                partial_postcard_back_txt_name.text = postcard.author.name
                partial_postcard_back_txt_location.text = postcard.location
                partial_postcard_back_txt_time.text = getTimeText(postcard.time)

                if(postcard.author.imgUrl.isNotEmpty()) {
                    val profileUri = Uri.parse(postcard.author.imgUrl)
                    loadImage(profileUri, partial_postcard_back_img_profile_picture)
                } else {
                    val color = ContextCompat.getColor(context, R.color.colorDefault)
                    val cd = ColorDrawable(color)
                    partial_postcard_back_img_profile_picture.setImageDrawable(cd)
                }

                partial_postcard_back_txt_postcard_message.text = postcard.message
            }
        }
    }

    /**
     * Converts Unix time into a formatted timestamp
     *
     * @param ut Unix time in milliseconds
     * @return the formatted timestamp as a string
     */
    private fun getTimeText(ut: Long): String {
        val now = System.currentTimeMillis()
        val elapsed = now - ut

        // Time units in milliseconds
        val minute = TimeUnit.MINUTES.toMillis(1)
        val hour = TimeUnit.HOURS.toMillis(1)
        val day = TimeUnit.DAYS.toMillis(1)
        val month = TimeUnit.DAYS.toMillis(30)
        val year = TimeUnit.DAYS.toMillis(365)

        // Return the amount of time that has elapsed between now and the time that was passed in
        return when(elapsed) {
            in 0 until minute -> "Just now"
            in minute until 2*minute -> "${elapsed / minute} minute ago"
            in 2*minute until hour -> "${elapsed / minute} minutes ago"
            in hour until 2*hour -> "${elapsed / hour} hour ago"
            in 2*hour until day -> "${elapsed / hour} hours ago"
            in day until 2*day -> "${elapsed / day} day ago"
            in 2*day until month -> "${elapsed / day} days ago"
            in month until 2*month -> "${elapsed / month} month ago"
            in 2*month until year -> "${elapsed / month} months ago"
            else -> {
                // If more than a year has elapsed, display the passed-in time as a standalone date
                val date = SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH)
                date.timeZone = TimeZone.getDefault()
                return date.format(Date(ut))
            }
        }
    }
}