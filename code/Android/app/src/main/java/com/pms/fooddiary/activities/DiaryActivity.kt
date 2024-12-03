package com.pms.fooddiary.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.pms.fooddiary.API.APIClient
import com.pms.fooddiary.API.MealAPIService
import com.pms.fooddiary.R
import com.pms.fooddiary.adapters.AddedProductsAdapter
import com.pms.fooddiary.adapters.MealsAdapter
import com.pms.fooddiary.models.MealRead
import com.pms.fooddiary.utils.DateUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class DiaryActivity : AppCompatActivity() {

    private lateinit var mealsRecyclerView: RecyclerView
    private lateinit var mealsAdapter: MealsAdapter
    private lateinit var dateField: EditText
    private lateinit var addMealButton: Button
    private lateinit var gestureDetector: GestureDetector
    private lateinit var editMealLauncher: ActivityResultLauncher<Intent>



    private val mealAPIService by lazy {
        APIClient.createRetrofit(this).create(MealAPIService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary)

        val home_button: Button = findViewById(R.id.button_home)
        val weight_button: Button = findViewById(R.id.button_weight)
        val diary_button: Button = findViewById(R.id.button_diary)
        val calendar_button: ImageButton = findViewById(R.id.date_picker_button)

        mealsRecyclerView = findViewById(R.id.meals_recycler_view)
        dateField = findViewById(R.id.date_field)
        addMealButton = findViewById(R.id.button_add_meal)

        editMealLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                loadMealsWithProducts(DateUtils.formatDateForAPI(getCurrentDate()))
            }
        }

        mealsAdapter = MealsAdapter(mutableListOf()) { meal ->
            val intent = Intent(this, EditMealActivity::class.java)
            intent.putExtra("meal", meal)
            editMealLauncher.launch(intent)
        }

        mealsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DiaryActivity)
            adapter = mealsAdapter
        }

        addMealButton.setOnClickListener {
            val intent = Intent(this, AddMealActivity::class.java)
            startActivity(intent)
        }

        dateField.setText(getCurrentDate())
        calendar_button.setOnClickListener {
            showDatePickerDialog()
        }

        loadMealsWithProducts(DateUtils.formatDateForAPI(getCurrentDate()))

        home_button.setOnClickListener {
            intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
        weight_button.setOnClickListener {
            intent = Intent(this, WeightActivity::class.java)
            startActivity(intent)
        }
        diary_button.setOnClickListener {
            intent = Intent(this, DiaryActivity::class.java)
            startActivity(intent)
        }
        gestureDetector = GestureDetector(this@DiaryActivity, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 != null && e2 != null) {
                    if (e1.x < e2.x) {
                        // Свайп влево
                        startActivity(Intent(this@DiaryActivity, WeightActivity::class.java))
                        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)

                    }
                }
                return true
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun loadMealsWithProducts(target_date: String) {
        mealAPIService.getUserMealsWithProductsByDate(target_date).enqueue(object : Callback<List<MealRead>> {
            override fun onResponse(call: Call<List<MealRead>>, response: Response<List<MealRead>>) {
                if (response.isSuccessful) {
                    response.body()?.let { meals ->
                        var totalProteins = 0.0
                        var totalFats = 0.0
                        var totalCarbohydrates = 0.0
                        var totalCalories = 0.0

                        // Пройдитесь по каждому приему пищи и суммируйте макроэлементы
                        for (meal in meals) {
                            // Проверяем, что продукты в приёме пищи не null
                            meal.products?.forEach { product ->
                                totalProteins += product.proteins ?: 0.0
                                totalFats += product.fats ?: 0.0
                                totalCarbohydrates += product.carbohydrates ?: 0.0
                                totalCalories += product.calories ?: 0.0
                            }
                        }

                        findViewById<TextView>(R.id.all_proteins).text = "${totalProteins} г."
                        findViewById<TextView>(R.id.all_fats).text = "${totalFats} г."
                        findViewById<TextView>(R.id.all_carbohydrates).text = "${totalCarbohydrates} г."
                        findViewById<TextView>(R.id.all_calories).text = "${totalCalories} ккал."

                        // Обновляем адаптер с новыми данными
                        mealsAdapter.updateMeals(meals)
                    }
                } else {
                    Toast.makeText(this@DiaryActivity, "Ошибка при загрузке данных", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<MealRead>>, t: Throwable) {
                Toast.makeText(this@DiaryActivity, "Ошибка сети  ${t.message}", Toast.LENGTH_SHORT).show()
                t.printStackTrace()
            }
        })
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                // Форматируем дату в формате дд.ММ.гггг для отображения
                val selectedDate = String.format(
                    "%02d.%02d.%04d",
                    selectedDay, selectedMonth + 1, selectedYear
                )
                dateField.setText(selectedDate) // Устанавливаем выбранную дату в поле

                // Преобразуем дату для API (в формате yyyy-MM-dd)
                loadMealsWithProducts(DateUtils.formatDateForAPI(selectedDate))
            },
            year, month, day
        )

        datePickerDialog.show()
    }
}
