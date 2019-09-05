package com.example.jeffrey.postcardsfromparis

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.SimpleItemAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.jeffrey.postcardsfromparis.model.Postcard
import com.example.jeffrey.postcardsfromparis.view.PostcardItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_received_mail.*
import kotlinx.android.synthetic.main.fragment_sent_mail.*

/**
 * This fragment displays postcards received by the user
 */
class ReceivedMailFragment : Fragment() {

    private val adapter = GroupAdapter<ViewHolder>()

    /**
     * Inflates the received postcards layout
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_received_mail, container, false)
    }

    /**
     * Loads postcards into view and sets click listeners
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        fragment_received_mail_rv_mail_list.adapter = adapter
        (fragment_received_mail_rv_mail_list.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        listenForPostcards()

        setClickListeners()
    }

    /**
     * Updates display when a new postcard is received
     */
    private fun listenForPostcards() {
        // Reads postcards from Firebase in the order they were created
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("postcards/$uid").orderByChild("time")
        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val postcard = p0.getValue(Postcard::class.java) ?: return

                // Ignore postcards that were sent by the user
                if(postcard.author.uid == uid) {
                    return
                }

                // Add the postcard to the top of the screen
                val postcardItem = PostcardItem(postcard, true)
                adapter.add(0, postcardItem)

                fragment_sent_mail_rv_mail_list?.smoothScrollToPosition(0)
                fragment_received_mail_txt_placeholder?.visibility = View.GONE
            }

            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {}
        })

        // This section runs after the existing postcards have been obtained from Firebase
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                // Display filler text if there are no postcards to display
                if(adapter.itemCount == 0) {
                    fragment_received_mail_txt_placeholder.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(p0: DatabaseError) {}
        })
    }

    /**
     * Flips postcards when the user taps them
     */
    private fun setClickListeners() {
        adapter.setOnItemClickListener { item, _ ->
            (item as PostcardItem).apply {
                showFront = !showFront
            }
            adapter.notifyItemChanged(item.getPosition(item))
        }
    }
}
