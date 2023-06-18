package com.example.a0509

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.a0509.databinding.NoticeListItemBinding

class NoticeAdapter(private val notices: MutableList<Notice>): RecyclerView.Adapter<NoticeAdapter.NoticeViewHolder>() {
	
	private lateinit var binding: NoticeListItemBinding
	
	interface OnItemClickListener {
		fun onItemClick(v: View, pos: Int)
	}
	
	private lateinit var itemClickListener: NoticeAdapter.OnItemClickListener
	fun setOnItemClickListener(listener : NoticeAdapter.OnItemClickListener) {
		itemClickListener = listener
	}
	
	inner class NoticeViewHolder(binding: NoticeListItemBinding) : RecyclerView.ViewHolder(binding.root) {
		init {
			binding.noticeViewGroup.setOnClickListener {
				val pos=adapterPosition
				if(pos!=RecyclerView.NO_POSITION) {
					itemClickListener.onItemClick(binding.noticeViewGroup,pos)
				}
			}
		}
		fun bind(item: Notice) {
			binding.noticeTitle.text=item.title
			binding.noticeDate.text=item.date
		}
	}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoticeAdapter.NoticeViewHolder {
		binding=NoticeListItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
		return NoticeViewHolder(binding)
	}
	
	override fun onBindViewHolder(holder: NoticeAdapter.NoticeViewHolder, position: Int) {
		val item = notices[position]
		holder.bind(item)
	}
	
	override fun getItemViewType(position: Int): Int {
		return position
	}
	
	override fun getItemCount(): Int {
		return notices.size
	}
}