package com.ognjenlazic.lazicinox

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_model.view.*



class ModelAdapter (
    val models: List<Model>
) : RecyclerView.Adapter<ModelAdapter.ModelViewHolder>(){

    var selectedModel = MutableLiveData<Model>()
    private var selectefModelIndex = 0

    inner class ModelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_model, parent, false)
        return ModelViewHolder(view)
    }

    override fun getItemCount() = models.size

    override fun onBindViewHolder(holder: ModelViewHolder, position: Int) {

        if(selectefModelIndex == holder.layoutPosition)
        {
            holder.itemView.setBackgroundColor(Color.parseColor("#ffffff"))
            selectedModel.value = models[holder.layoutPosition]
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#1d58a7"))
        }



        holder.itemView.apply {
            ivThumbnail.setImageResource(models[position].imageResourceId)
            tvTitle.text = models[position].title

            setOnClickListener{
                selectModel(holder)
            }

        }
    }


    private fun  selectModel(holder : ModelViewHolder){
        if (selectefModelIndex != holder.layoutPosition){
            holder.itemView.setBackgroundColor(Color.parseColor("#ffffff"))
            notifyItemChanged(selectefModelIndex)
            selectefModelIndex = holder.layoutPosition
            selectedModel.value = models[holder.layoutPosition]
        }
    }
}