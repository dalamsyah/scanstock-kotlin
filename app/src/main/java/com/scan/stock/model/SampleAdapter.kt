package com.scan.stock.model

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.scan.stock.databinding.ItemRowBinding

class SampleAdapter  : RecyclerView.Adapter<SampleAdapter.ViewHolder>(){
    private lateinit var binding: ItemRowBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SampleAdapter.ViewHolder {
        binding= ItemRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder()
    }

    override fun onBindViewHolder(holder: SampleAdapter.ViewHolder, position: Int) {
        holder.setData(differ.currentList[position])
//        holder.setIsRecyclable(false)
    }

    override fun getItemCount()=differ.currentList.size

    inner class ViewHolder : RecyclerView.ViewHolder(binding.root){
        fun setData(item : ScanStock){
            binding.apply {
                txtSn.text = "SN: "+item.sn
                txtSn2.text = "SN2: "+item.sn2
                txtRack.text = "Rack: ${item.loc} ${item.zone} "

                txtScan.text = "Scan: "+item.scan
                txtUpload.text = "Upload: "+item.upload
                txtScanTime.text = "Scan Time: "+item.scan_datetime
            }
        }

    }

    private val differCallback = object : DiffUtil.ItemCallback<ScanStock>(){
        override fun areItemsTheSame(oldItem: ScanStock, newItem: ScanStock): Boolean {
            return  oldItem.id == newItem.id
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: ScanStock, newItem: ScanStock): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this,differCallback)

}