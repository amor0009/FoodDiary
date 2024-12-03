package com.pms.fooddiary.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pms.fooddiary.R
import com.pms.fooddiary.models.MealRead

class MealsAdapter(
    private val meals: MutableList<MealRead>,
    private val onMealClick: (MealRead) -> Unit
) : RecyclerView.Adapter<MealsAdapter.MealViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_meal, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = meals[position]
        holder.bind(meal)
    }

    override fun getItemCount(): Int = meals.size

    fun updateMeals(newMeals: List<MealRead>) {
        meals.clear()
        meals.addAll(newMeals)
        notifyDataSetChanged()
    }

    inner class MealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mealNameTextView: TextView = itemView.findViewById(R.id.meal_name_text_view)
        private val mealDetailsTextView: TextView = itemView.findViewById(R.id.meal_details_text_view)
        private val productsRecyclerView: RecyclerView = itemView.findViewById(R.id.products_recycler_view)

        fun bind(meal: MealRead) {
            mealNameTextView.text = meal.name

            // Расчет параметров на основе продуктов
            val totalWeight = meal.products?.sumOf { it.weight ?: 0.0 } ?: 0.0
            val totalCalories = meal.products?.sumOf { it.calories ?: 0.0 } ?: 0.0
            val totalProteins = meal.products?.sumOf { it.proteins ?: 0.0 } ?: 0.0
            val totalFats = meal.products?.sumOf { it.fats ?: 0.0 } ?: 0.0
            val totalCarbohydrates = meal.products?.sumOf { it.carbohydrates ?: 0.0 } ?: 0.0

            mealDetailsTextView.text = "Масса: ${totalWeight} г, Калории: ${totalCalories} ккал, Белки: ${totalProteins} г, Жиры: ${totalFats} г, Углеводы: ${totalCarbohydrates} г"

            // Настройка RecyclerView для продуктов
            productsRecyclerView.layoutManager = LinearLayoutManager(itemView.context)
            productsRecyclerView.adapter = OutputProductAdapter(meal.products ?: emptyList(), itemView.context)
            // Переход в ProductViewActivity при нажатии на продукт
            itemView.setOnClickListener {
                onMealClick(meal)
            }
        }
    }
}
