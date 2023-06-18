package com.example.myapplication

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.a0509.R
import com.example.a0509.Schedule
import com.example.a0509.ScheduleReadActivity
import com.example.a0509.databinding.ScheduleListItemBinding
import com.example.a0509.ui.home.HomeFragment
import java.util.Date

class ScheduleAdapter(private val schedules: List<Schedule>) : RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {
	
	private lateinit var binding: ScheduleListItemBinding
	
	interface OnItemClickListener{
		fun onItemClick(v: View, pos: Int)
	}
	
	private lateinit var itemClickListener: OnItemClickListener
	fun setOnItemClickListener(listener : OnItemClickListener) {
		itemClickListener = listener
	}
	
	inner class ScheduleViewHolder(binding: ScheduleListItemBinding) : RecyclerView.ViewHolder(binding.root) {
		init {
			binding.scheduleTitle.setOnClickListener {
				val pos=adapterPosition
				if(pos!=RecyclerView.NO_POSITION && itemClickListener!=null) {
					itemClickListener.onItemClick(binding.scheduleTitle,pos)
				}
			}
		}
		fun bind(item: Schedule) {
			binding.scheduleTitle.text=item.scheduleName
		}
	}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleAdapter.ScheduleViewHolder {
		binding=ScheduleListItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
		return ScheduleViewHolder(binding)
	}
	
	override fun onBindViewHolder(holder: ScheduleAdapter.ScheduleViewHolder, position: Int) {
		val item = schedules[position]
		holder.bind(item)
		
		holder.itemView.setOnClickListener {
			val intent=Intent(holder.itemView.context,ScheduleReadActivity::class.java)
			intent.putExtra("schedule_id",item.scheduleId)
			ContextCompat.startActivity(holder.itemView.context,intent,null)
		}
	}
	
	override fun getItemCount(): Int {
		return schedules.size
	}
}