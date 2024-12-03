package com.pms.fooddiary.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pms.fooddiary.R
import com.pms.fooddiary.activities.ProductViewActivity
import com.pms.fooddiary.models.ProductRead

class OutputProductAdapter(
    private val products: List<ProductRead>,
    private val context: Context
) : RecyclerView.Adapter<OutputProductAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product_output, parent, false)
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
            productDetailsTextView.text = "Масса: ${product.weight} г, Калории: ${product.calories} ккал, " +
                    "Белки: ${product.proteins} г, Жиры: ${product.fats} г, Углеводы: ${product.carbohydrates} г"

            // Обработчик клика на продукт
            itemView.setOnClickListener {
                val intent = Intent(context, ProductViewActivity::class.java)
                intent.putExtra("product", product) // Передаем продукт
                context.startActivity(intent)
            }
        }
    }
}
