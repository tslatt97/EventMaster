package com.example.eventmaster

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.event_item.view.*

class EventsAdapter(var eventsListItem: List<EventItem>,  private val clickListener: (EventItem) -> Unit): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    //View holder
    class itemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(eventItem: EventItem, clickListener: (EventItem) -> Unit) {
            itemView.title_text.text = eventItem.title
            itemView.detail_text.text = eventItem.details
            itemView.distance_text.text = eventItem.address
            itemView.time.text = eventItem.time + " - " + eventItem.date


            itemView.setOnClickListener {
                clickListener(eventItem)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
       val view = LayoutInflater.from(parent.context).inflate(R.layout.event_item,parent,false)
        return itemViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as itemViewHolder).bind(eventsListItem[position],clickListener)
    }

    override fun getItemCount(): Int {
        return eventsListItem.size
    }
}