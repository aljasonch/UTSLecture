package com.example.utslecture.Setting

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.utslecture.R
import com.example.utslecture.data.RedeemItem
import com.example.utslecture.databinding.RedeemPointItemBinding

class RedeemAdapter(
    private val redeemList: MutableList<RedeemItem>,
    private var userPoints: Int,
    private val onRedeemClicked: (RedeemItem, String?) -> Unit
) : RecyclerView.Adapter<RedeemAdapter.RedeemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RedeemViewHolder {
        val binding = RedeemPointItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RedeemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RedeemViewHolder, position: Int) {
        val item = redeemList[position]
        val isRedeemable = userPoints >= item.points

        holder.bind(item,onRedeemClicked,isRedeemable )


    }

    override fun getItemCount(): Int = redeemList.size

    fun submitList(newList: List<RedeemItem>) {
        redeemList.clear()
        redeemList.addAll(newList)
        notifyDataSetChanged()
    }

    fun updateUserPoints(points: Int) {
        userPoints = points
        notifyDataSetChanged()
    }

    inner class RedeemViewHolder(private val binding: RedeemPointItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RedeemItem, onRedeemClicked: (RedeemItem, String?) -> Unit, isRedeemable: Boolean) {
            binding.tvRedeemPoints.text = "${item.points} Point"

            setButtonAppearance(item.redeemCode,isRedeemable)
            binding.btnRedeem.setOnClickListener {
                if (item.redeemCode != null){
                    onRedeemClicked(item,item.redeemCode)
                } else if(isRedeemable)
                    onRedeemClicked(item,null)


            }

            Glide.with(binding.ivRedeemImage.context)
                .load(item.imageUrl)
                .into(binding.ivRedeemImage)
        }


        fun setButtonAppearance(redeemCode: String?, isRedeemable: Boolean) {
            if (redeemCode != null){
                binding.btnRedeem.isEnabled = false
                binding.btnRedeem.text = "Redeemed"
                binding.btnRedeem.setBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.gray
                    )
                )
            }else  if (isRedeemable) {
                binding.btnRedeem.isEnabled = true
                binding.btnRedeem.text = "Redeem"
                binding.btnRedeem.setBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.primary_button
                    )
                )
            } else {
                binding.btnRedeem.isEnabled = false
                binding.btnRedeem.text = "Redeem"
                binding.btnRedeem.setBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.gray
                    )
                )
            }
        }
    }
}