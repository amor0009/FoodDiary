package com.pms.fooddiary.activities

import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.pms.fooddiary.API.APIClient
import com.pms.fooddiary.API.UserAPIService
import com.pms.fooddiary.API.UserWeightAPIService
import com.pms.fooddiary.R
import com.pms.fooddiary.models.UserResponse
import com.pms.fooddiary.models.UserWeightRead
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class WeightActivity : AppCompatActivity() {

    private lateinit var currentWeightTextView: TextView
    private lateinit var weightChangeTextView: TextView
    private lateinit var weightChart: LineChart
    private lateinit var userWeightAPIService: UserWeightAPIService
    private lateinit var userAPIService: UserAPIService
    private lateinit var gestureDetector: GestureDetector
    private var user_id: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weight)

        val home_button: Button = findViewById(R.id.button_home)
        val weight_button: Button = findViewById(R.id.button_weight)
        val diary_button: Button = findViewById(R.id.button_diary)

        // Инициализация UI элементов
        currentWeightTextView = findViewById(R.id.current_weight_text_view)
        weightChangeTextView = findViewById(R.id.weight_change_text_view)
        weightChart = findViewById(R.id.weight_chart)


        // Инициализация API сервиса
        userWeightAPIService = APIClient.createRetrofit(this).create(UserWeightAPIService::class.java)
        userAPIService = APIClient.createRetrofit(this).create(UserAPIService::class.java)
        userAPIService.getCurrentUser().enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    user_id = response.body()!!.user.id
                    loadCurrentWeight()
                    loadWeightHistory()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Toast.makeText(this@WeightActivity, "Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })

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
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 != null && e2 != null) {
                    if (e1.x < e2.x) {
                        // Свайп влево
                        startActivity(Intent(this@WeightActivity, HomeActivity::class.java))
                        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                    } else if (e1.x > e2.x) {
                        // Свайп вправо
                        startActivity(Intent(this@WeightActivity, DiaryActivity::class.java))

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

    private fun loadCurrentWeight() {
        val recordedAt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        userWeightAPIService.getUserWeight(recordedAt).enqueue(object : Callback<UserWeightRead> {
            override fun onResponse(call: Call<UserWeightRead>, response: Response<UserWeightRead>) {
                if (response.isSuccessful) {
                    response.body()?.let { weightData ->
                        currentWeightTextView.text = "Текущий вес: ${weightData.weight} кг"
                    }
                } else {
                    Toast.makeText(this@WeightActivity, "Ошибка при загрузке текущего веса", Toast.LENGTH_SHORT).show()
                }

            }

            override fun onFailure(call: Call<UserWeightRead>, t: Throwable) {
            }
        })
    }

    private fun loadWeightHistory() {
        userWeightAPIService.getUserWeightHistory().enqueue(object : Callback<List<UserWeightRead>> {
            override fun onResponse(call: Call<List<UserWeightRead>>, response: Response<List<UserWeightRead>>) {
                if (response.isSuccessful) {
                    response.body()?.let { weightHistory ->
                        // Подсчёт изменения веса за последние 30 дней
                        if (weightHistory.isNotEmpty()) {
                            val recentWeights = weightHistory.takeLast(30)
                            val startWeight = recentWeights.first().weight
                            val endWeight = recentWeights.last().weight
                            val weightChange = endWeight?.minus(startWeight!!)
                            weightChangeTextView.text = "Изменение веса: $weightChange кг"

                            // Построение графика
                            displayWeightChart(recentWeights)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<UserWeightRead>>, t: Throwable) {
                // Обработка ошибки
            }
        })
    }

    private fun displayWeightChart(weightHistory: List<UserWeightRead>) {
        val entries = weightHistory.mapIndexed { index, userWeight ->
            userWeight.weight?.let { Entry(index.toFloat(), it) }
        }

        val dataSet = LineDataSet(entries, "Вес за последние 30 дней").apply {
            color = resources.getColor(R.color.purple_500, theme)
            valueTextColor = resources.getColor(R.color.black, theme)
            lineWidth = 2f
            circleRadius = 4f
        }

        val lineData = LineData(dataSet)
        weightChart.data = lineData

        // Настройка графика
        weightChart.apply {
            description = Description().apply { text = "Динамика веса" }
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            invalidate() // Перерисовка графика
        }
    }
}
