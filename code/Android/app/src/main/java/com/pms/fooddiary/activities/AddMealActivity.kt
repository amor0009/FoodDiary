package com.pms.fooddiary.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pms.fooddiary.API.APIClient
import com.pms.fooddiary.API.MealAPIService
import com.pms.fooddiary.API.ProductAPIService
import com.pms.fooddiary.R
import com.pms.fooddiary.adapters.AddedProductsAdapter
import com.pms.fooddiary.adapters.SearchProductsAdapter
import com.pms.fooddiary.models.MealCreate
import com.pms.fooddiary.models.MealProductsCreate
import com.pms.fooddiary.models.MealRead
import com.pms.fooddiary.models.ProductRead
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.round

class AddMealActivity : AppCompatActivity() {

    private lateinit var mealNameInput: EditText
    private lateinit var mealSummaryTextView: TextView
    private lateinit var addMealButton: Button
    private lateinit var addedProductsRecyclerView: RecyclerView
    private lateinit var searchProductRecyclerView: RecyclerView
    private lateinit var addedProductsAdapter: AddedProductsAdapter
    private lateinit var searchProductsAdapter: SearchProductsAdapter
    private val addedProducts = mutableListOf<ProductRead>()
    private val searchResults = mutableListOf<ProductRead>()
    private lateinit var backButton: Button


    private val mealAPIService by lazy {
        APIClient.createRetrofit(this).create(MealAPIService::class.java)
    }

    private val productAPIService by lazy {
        APIClient.createRetrofit(this).create(ProductAPIService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_meal)

        mealNameInput = findViewById(R.id.meal_name_input)
        mealSummaryTextView = findViewById(R.id.meal_summary_text_view)
        addMealButton = findViewById(R.id.add_meal_button)
        addedProductsRecyclerView = findViewById(R.id.products_list_view)
        searchProductRecyclerView = findViewById(R.id.search_product_recycler_view)
        backButton = findViewById(R.id.back_button)

        addedProductsAdapter = AddedProductsAdapter(
            addedProducts,
            { product -> removeProductFromMeal(product) },
            { product, newWeight -> updateProductWeight(product, newWeight) },
            { product -> navigateToProductView(product) } // Передача обработчика клика
        )

        searchProductsAdapter = SearchProductsAdapter(searchResults) { product ->
            addProductToMeal(product)
        }

        addedProductsRecyclerView.layoutManager = LinearLayoutManager(this)
        addedProductsRecyclerView.adapter = addedProductsAdapter

        searchProductRecyclerView.layoutManager = LinearLayoutManager(this)
        searchProductRecyclerView.adapter = searchProductsAdapter

        addMealButton.setOnClickListener {
            createMeal()
            startActivity(Intent(this, DiaryActivity::class.java))
        }

        setupProductSearch()

        backButton.setOnClickListener {
            finish() // Завершение текущей активности и возврат к предыдущей
        }
    }

    private fun navigateToProductView(product: ProductRead) {
        val intent = Intent(this, ProductViewActivity::class.java)
        intent.putExtra("product", product) // Передаем объект продукта
        startActivity(intent)
    }

    private fun setupProductSearch() {
        val searchInput = findViewById<EditText>(R.id.search_product_edit_text)

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                if (query.isBlank()) {
                    showAddedProducts()
                } else {
                    searchingProducts(query)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun showAddedProducts() {
        // Скрываем список поиска и показываем добавленные продукты
        addedProductsRecyclerView.visibility = View.VISIBLE
        searchProductRecyclerView.visibility = View.GONE
    }

    private fun searchingProducts(query: String) {
        // Показываем список результатов поиска
        addedProductsRecyclerView.visibility = View.GONE
        searchProductRecyclerView.visibility = View.VISIBLE

        productAPIService.searchProducts(query).enqueue(object : Callback<List<ProductRead>> {
            override fun onResponse(call: Call<List<ProductRead>>, response: Response<List<ProductRead>>) {
                if (response.isSuccessful) {
                    response.body()?.let { products ->
                        searchResults.clear()
                        searchResults.addAll(products)
                        searchProductsAdapter.notifyDataSetChanged()
                    }
                }
            }

            override fun onFailure(call: Call<List<ProductRead>>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    private fun addProductToMeal(product: ProductRead) {
        if (!addedProducts.contains(product)) {
            addedProducts.add(product)
            updateMealSummary()

            // Очистить текст в поле поиска и скрыть результаты поиска
            val searchInput = findViewById<EditText>(R.id.search_product_edit_text)
            searchInput.text.clear()
            showAddedProducts()
        }
    }

    private fun removeProductFromMeal(product: ProductRead) {
        val index = addedProducts.indexOfFirst { it.id == product.id }
        if (index != -1) {
            addedProducts.removeAt(index)
            addedProductsAdapter.notifyItemRemoved(index)  // Уведомляем адаптер о удалении
            updateMealSummary()  // Обновляем итоговую информацию
        }
    }

    private fun updateProductWeight(product: ProductRead, newWeight: Double) {
        val index = addedProducts.indexOfFirst { it.id == product.id }
        if (index != -1) {
            val originalProduct = addedProducts[index] // Исходный продукт

            // Округляем вес и пересчитываем параметры
            val roundedWeight = round(newWeight * 10) / 10
            val updatedProduct = originalProduct.copy(
                weight = roundedWeight,
                calories = round((originalProduct.calories * roundedWeight) / originalProduct.weight * 10) / 10,
                proteins = round((originalProduct.proteins * roundedWeight) / originalProduct.weight * 10) / 10,
                fats = round((originalProduct.fats * roundedWeight) / originalProduct.weight * 10) / 10,
                carbohydrates = round((originalProduct.carbohydrates * roundedWeight) / originalProduct.weight * 10) / 10
            )
            addedProducts[index] = updatedProduct
            addedProductsAdapter.notifyItemChanged(index)
            updateMealSummary()
        }
    }

    private fun updateMealSummary() {
        val totalWeight = addedProducts.sumOf { it.weight }
        val totalCalories = addedProducts.sumOf { it.calories }
        val totalProteins = addedProducts.sumOf { it.proteins }
        val totalFats = addedProducts.sumOf { it.fats }
        val totalCarbs = addedProducts.sumOf { it.carbohydrates }

        mealSummaryTextView.text = "Масса: $totalWeight г, Калории: $totalCalories ккал, " +
                "Белки: $totalProteins г, Жиры: $totalFats г, Углеводы: $totalCarbs г"
    }

    private fun createMeal() {
        val mealName = mealNameInput.text.toString()

        // Проверяем, что название не пустое, но список продуктов может быть пустым
        if (mealName.isBlank()) {
            return  // Название обязательно, возвращаемся, если оно пустое
        }

        // Создаем MealCreate объект, если продукты пустые, передаем пустой список
        val mealCreate = MealCreate(
            name = mealName,
            weight = addedProducts.sumOf { it.weight }, // Если продуктов нет, будет 0
            calories = addedProducts.sumOf { it.calories },
            proteins = addedProducts.sumOf { it.proteins },
            fats = addedProducts.sumOf { it.fats },
            carbohydrates = addedProducts.sumOf { it.carbohydrates },
            products = if (addedProducts.isEmpty()) emptyList() else addedProducts.map { product ->
                MealProductsCreate(
                    product_weight = product.weight,
                    product_id = product.id
                )
            }
        )

        // Отправляем запрос на создание блюда
        mealAPIService.addMeal(mealCreate).enqueue(object : Callback<MealRead> {
            override fun onResponse(call: Call<MealRead>, response: Response<MealRead>) {
                if (response.isSuccessful) {
                    // Обработка успешного добавления блюда
                }
            }

            override fun onFailure(call: Call<MealRead>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }
}
