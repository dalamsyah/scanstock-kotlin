package com.scan.stock.model

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.scan.stock.R
import com.scan.stock.databinding.ItemRowBinding

class SampleAdapter2 : ListAdapter<ScanStock, SampleAdapter2.WordViewHolder>(WordsComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        return WordViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtSn: TextView = itemView.findViewById(R.id.txtSn)
        private val txtSn2: TextView = itemView.findViewById(R.id.txtSn2)
        private val txtRack: TextView = itemView.findViewById(R.id.txtRack)
        private val txtScan: TextView = itemView.findViewById(R.id.txtScan)
        private val txtUpload: TextView = itemView.findViewById(R.id.txtUpload)
        private val txtScanTime: TextView = itemView.findViewById(R.id.txtScanTime)

        fun bind(model: ScanStock) {
            txtSn.text = "SN: "+ model.sn
            txtSn2.text = "SN2: "+ model.sn2
            txtRack.text = "Rack: ${model.zone}-${model.area}-${model.rack}-${model.bin}"
            txtScan.text = "Scan: "+ model.scan.toString()
            txtUpload.text = "Upload: "+ model.upload.toString()
            txtScanTime.text = "Scan time: "+ model.scan_datetime
        }

        companion object {
            fun create(parent: ViewGroup): WordViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_row, parent, false)
                return WordViewHolder(view)
            }
        }
    }

    class WordsComparator : DiffUtil.ItemCallback<ScanStock>() {
        override fun areItemsTheSame(oldItem: ScanStock, newItem: ScanStock): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: ScanStock, newItem: ScanStock): Boolean {
            return oldItem.id == newItem.id
        }
    }
}