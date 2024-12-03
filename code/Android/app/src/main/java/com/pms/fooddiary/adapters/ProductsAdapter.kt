package com.pms.fooddiary.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pms.fooddiary.R
import com.pms.fooddiary.models.ProductRead

class ProductsAdapter(
    private val products: MutableList<ProductRead>,
    private val onProductClick: (ProductRead) -> Unit // Функция для добавления при клике на продукт
) : RecyclerView.Adapter<ProductsAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product_added, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product)
    }

    override fun getItemCount(): Int = products.size

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productNameTextView: TextView = itemView.findViewById(R.id.product_name_text_view)
        private val productDetailsTextView: TextView = itemView.findViewById(R.id.product_details_text_view)

        fun bind(product: ProductRead) {
            productNameTextView.text = product.name
            productDetailsTextView.text = "Масса: ${product.weight} г, Калории: ${product.calories} ккал, Белки: ${product.proteins} г, Жиры: ${product.fats} г, Углеводы: ${product.carbohydrates} г"

            itemView.setOnClickListener {
                onProductClick(product) // Добавляем продукт в приём пищи при клике
            }
        }
    }
}
