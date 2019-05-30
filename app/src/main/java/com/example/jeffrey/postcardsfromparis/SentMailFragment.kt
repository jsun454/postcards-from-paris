package com.example.jeffrey.postcardsfromparis

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.jeffrey.postcardsfromparis.model.Postcard
import com.example.jeffrey.postcardsfromparis.view.PostcardItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_sent_mail.*

class SentMailFragment : Fragment() {

    private val adapter = GroupAdapter<ViewHolder>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_sent_mail, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        fragment_sent_mail_rv_mail_list.adapter = adapter

        listenForPostcards()

        setClickListeners()
    }

    private fun listenForPostcards() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("postcards/$uid").orderByChild("time")
        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val postcard = p0.getValue(Postcard::class.java) ?: return
                val postcardItem = PostcardItem(postcard, true)
                adapter.add(0, postcardItem)
            }

            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {}
        })
    }

    private fun setClickListeners() {
        adapter.setOnItemClickListener { item, _ ->
            (item as PostcardItem).apply {
                showFront = !showFront
            }
            adapter.notifyItemChanged(item.getPosition(item))
        }
    }
}