package com.lagradost.quicknovel.ui.mainpage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lagradost.quicknovel.MainActivity
import com.lagradost.quicknovel.MainActivity.Companion.loadResult
import com.lagradost.quicknovel.SearchResponse
import com.lagradost.quicknovel.databinding.LoadingBottomBinding
import com.lagradost.quicknovel.databinding.SearchResultGridBinding
import com.lagradost.quicknovel.util.UIHelper.setImage
import com.lagradost.quicknovel.util.toPx
import com.lagradost.quicknovel.widget.AutofitRecyclerView
import kotlin.math.roundToInt
const val REGULAR_VIEW_TYPE = 0
const val FOOTER_VIEW_TYPE = 1
class MainAdapter2(private val resView: AutofitRecyclerView) : ListAdapter<SearchResponse, RecyclerView.ViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) { // Use 'when' to create the correct holder for any position
            REGULAR_VIEW_TYPE -> MainAdapter2Holder(SearchResultGridBinding.inflate(LayoutInflater.from(parent.context),parent,false))
            FOOTER_VIEW_TYPE -> LoadingHolder(LoadingBottomBinding.inflate(LayoutInflater.from(parent.context),parent,false))
            else -> {
                throw NotImplementedError()
            }
        }
    }

    /* private var loadingItems : Boolean = false

     fun setLoading(to : Boolean) {
         if (loadingItems == to) return
         if (to) {
             loadingItems = true
             notifyItemRemoved(currentList.size)
         } else {
             loadingItems = false
             notifyItemInserted(currentList.size)
         }
    }*/

    //override fun getItemCount() = currentList.size//if(loadingItems) 1 else 0 // We need the extra 1 for the footer to be counted

    override fun getItemViewType(position: Int) =
        if (position == currentList.size) FOOTER_VIEW_TYPE else REGULAR_VIEW_TYPE

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is MainAdapter2Holder -> {
                val currentItem = getItem(position)
                holder.bind(currentItem, resView)
            }
            is LoadingHolder -> {
                holder.bind()
            }
        }
    }

    class LoadingHolder(private val binding : LoadingBottomBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {

        }
    }

    class MainAdapter2Holder(private val binding : SearchResultGridBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(card : SearchResponse, resView: AutofitRecyclerView) {
            binding.apply {
                val compactView = false//resView.context?.getGridIsCompact() ?: return

                val coverHeight: Int =
                    if (compactView) 80.toPx else (resView.itemWidth / 0.68).roundToInt()

                imageView.apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        coverHeight
                    )
                    setImage(card.image)
                    setLayerType(View.LAYER_TYPE_SOFTWARE, null) // HALF IMAGE DISPLAYING FIX
                    setOnClickListener {
                        loadResult(card.url, card.apiName)
                    }
                    setOnLongClickListener {
                        MainActivity.loadPreviewPage(card)
                        return@setOnLongClickListener true
                    }
                }

                imageText.text = card.name
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SearchResponse>() {
        override fun areItemsTheSame(oldItem: SearchResponse, newItem: SearchResponse): Boolean = oldItem.name == newItem.name
        override fun areContentsTheSame(oldItem: SearchResponse, newItem: SearchResponse): Boolean = oldItem == newItem
    }
}