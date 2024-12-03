package com.pms.fooddiary.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pms.fooddiary.R
import com.pms.fooddiary.models.ProductRead

class SearchProductsAdapter(
    private val products: List<ProductRead>,
    private val onItemClick: (ProductRead) -> Unit
) : RecyclerView.Adapter<SearchProductsAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product_search, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product)
    }

    override fun getItemCount() = products.size

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productName = itemView.findViewById<TextView>(R.id.product_name_text_view)
        private val productDetails = itemView.findViewById<TextView>(R.id.product_details_text_view)

        fun bind(product: ProductRead) {
            productName.text = product.name
            productDetails.text = "Масса: ${product.weight} г, Калории: ${product.calories} ккал, Белки: ${product.proteins} г, Жиры: ${product.fats} г, Углеводы: ${product.carbohydrates} г"
            itemView.setOnClickListener { onItemClick(product) }
        }
    }
}
