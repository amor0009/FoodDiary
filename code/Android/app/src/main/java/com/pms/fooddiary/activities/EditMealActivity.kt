package com.pms.fooddiary.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import com.pms.fooddiary.models.MealProductsUpdate
import com.pms.fooddiary.models.MealRead
import com.pms.fooddiary.models.MealUpdate
import com.pms.fooddiary.models.ProductRead
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.round

class EditMealActivity : AppCompatActivity() {

    private lateinit var mealNameInput: EditText
    private lateinit var searchInput: EditText
    private lateinit var mealSummaryTextView: TextView
    private lateinit var saveMealButton: Button
    private lateinit var deleteMealButton: Button
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

    private var meal: MealRead? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_meal)

        // Инициализация UI-компонентов
        mealNameInput = findViewById(R.id.meal_name_input)
        mealSummaryTextView = findViewById(R.id.meal_summary_text_view)
        saveMealButton = findViewById(R.id.update_meal_button)
        deleteMealButton = findViewById(R.id.delete_meal_button)
        searchInput = findViewById(R.id.search_product_edit_text)
        addedProductsRecyclerView = findViewById(R.id.products_recycler_view)
        searchProductRecyclerView = findViewById(R.id.search_product_recycler_view)
        backButton = findViewById(R.id.back_button)

        // Получение объекта meal из Intent
        meal = intent?.getParcelableExtra<MealRead>("meal")
        if (meal == null) {
            Log.e("EditMealActivity", "Meal is null. Finishing activity.")
            finish() // Завершаем активность, если данных нет
            return
        }

        // Настройка адаптеров и RecyclerView
        setupRecyclerViews()

        // Установка обработчиков кнопок
        setupButtonListeners()

        // Настройка поиска продуктов
        setupProductSearch()

        // Загрузка данных о блюде
        loadMealData()

        backButton.setOnClickListener {
            finish() // Завершение текущей активности и возврат к предыдущей
        }
    }

    private fun setupRecyclerViews() {
        addedProductsAdapter = AddedProductsAdapter(addedProducts, { product ->
            removeProductFromMeal(product)
        }, { product, newWeight ->
            updateProductWeight(product, newWeight)
        }, { product ->
            navigateToProductView(product)  // Переход в ProductViewActivity
        })

        searchProductsAdapter = SearchProductsAdapter(searchResults) { product ->
            addProductToMeal(product)
        }

        addedProductsRecyclerView.layoutManager = LinearLayoutManager(this)
        addedProductsRecyclerView.adapter = addedProductsAdapter

        searchProductRecyclerView.layoutManager = LinearLayoutManager(this)
        searchProductRecyclerView.adapter = searchProductsAdapter
    }

    private fun setupButtonListeners() {
        saveMealButton.setOnClickListener {
            updateMeal()
            setResult(RESULT_OK) // Устанавливаем результат
            finish() // Возврат в DiaryActivity
        }

        deleteMealButton.setOnClickListener {
            deleteMeal()
            setResult(RESULT_OK) // Устанавливаем результат
            finish() // Возврат в DiaryActivity
        }
    }

    private fun loadMealData() {
        meal?.let {
            mealNameInput.setText(it.name)
            addedProducts.clear()
            addedProducts.addAll(it.products ?: emptyList())
            if (addedProducts.isNotEmpty()) {
                addedProductsRecyclerView.visibility = RecyclerView.VISIBLE
                searchProductRecyclerView.visibility = RecyclerView.GONE
            } else {
                addedProductsRecyclerView.visibility = RecyclerView.GONE
            }

            updateMealSummary()
            addedProductsAdapter.notifyDataSetChanged()
        }
    }



    private fun updateMeal() {
        val mealName = mealNameInput.text.toString()
        val mealUpdate = MealUpdate(
            name = mealName,
            weight = addedProducts.sumOf { it.weight },
            calories = addedProducts.sumOf { it.calories },
            proteins = addedProducts.sumOf { it.proteins },
            fats = addedProducts.sumOf { it.fats },
            carbohydrates = addedProducts.sumOf { it.carbohydrates },
            products = addedProducts.map { product ->
                MealProductsUpdate(
                    product_weight = product.weight,
                    product_id = product.id
                )
            }
        )

        mealAPIService.updateMeal(meal?.id ?: 0, mealUpdate).enqueue(object : Callback<MealRead> {
            override fun onResponse(call: Call<MealRead>, response: Response<MealRead>) {
                if (response.isSuccessful) {
                    Log.d("EditMealActivity", "Meal updated successfully")
                }
            }

            override fun onFailure(call: Call<MealRead>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    private fun deleteMeal() {
        meal?.let {
            mealAPIService.deleteMeal(it.id!!).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.d("EditMealActivity", "Meal deleted successfully")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    t.printStackTrace()
                }
            })
        }
    }

    private fun setupProductSearch() {
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
        addedProductsRecyclerView.visibility = RecyclerView.VISIBLE
        searchProductRecyclerView.visibility = RecyclerView.GONE
    }

    private fun searchingProducts(query: String) {
        searchProductRecyclerView.visibility = RecyclerView.VISIBLE
        addedProductsRecyclerView.visibility = RecyclerView.GONE

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
            searchInput.text.clear()
            showAddedProducts()
        }
    }

    private fun removeProductFromMeal(product: ProductRead) {
        val index = addedProducts.indexOfFirst { it.id == product.id }
        if (index != -1) {
            addedProducts.removeAt(index)
            addedProductsAdapter.notifyItemRemoved(index)
            updateMealSummary()
        }
    }

    private fun updateProductWeight(product: ProductRead, newWeight: Double) {
        val index = addedProducts.indexOfFirst { it.id == product.id }
        if (index != -1) {
            val roundedWeight = round(newWeight * 10) / 10
            val ratio = roundedWeight / (product.weight ?: 1.0)

            val updatedProduct = addedProducts[index].copy(
                weight = roundedWeight,
                calories = product.calories * ratio,
                proteins = product.proteins * ratio,
                fats = product.fats * ratio,
                carbohydrates = product.carbohydrates * ratio
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

        mealSummaryTextView.text = "Масса: ${"%.1f".format(totalWeight)} г, Калории: ${"%.1f".format(totalCalories)} ккал, " +
                "Белки: ${"%.1f".format(totalProteins)} г, Жиры: ${"%.1f".format(totalFats)} г, Углеводы: ${"%.1f".format(totalCarbs)} г"
    }

    private fun navigateToProductView(product: ProductRead) {
        val intent = Intent(this, ProductViewActivity::class.java)
        intent.putExtra("product", product)
        startActivity(intent)
    }

}
