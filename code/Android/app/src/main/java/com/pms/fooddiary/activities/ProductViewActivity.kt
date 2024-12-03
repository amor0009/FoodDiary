package com.pms.fooddiary.activities

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.pms.fooddiary.R
import com.pms.fooddiary.models.ProductRead

class ProductViewActivity : AppCompatActivity() {

    private lateinit var productImageView: ImageView
    private lateinit var productNameTextView: TextView
    private lateinit var productDetailsTextView: TextView
    private lateinit var productDescriptionTextView: TextView
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.item_product_view)

        // Инициализация View
        productImageView = findViewById(R.id.product_image)
        productNameTextView = findViewById(R.id.product_name)
        productDetailsTextView = findViewById(R.id.product_params)
        productDescriptionTextView = findViewById(R.id.product_description)
        backButton = findViewById(R.id.back_button)

        // Получаем переданный продукт
        val product = intent.getParcelableExtra<ProductRead>("product")

        if (product != null) {
            // Заполняем данные продукта
            productNameTextView.text = product.name
            productDetailsTextView.text = "Масса: ${product.weight} г, Калории: ${product.calories} ккал, " +
                    "Белки: ${product.proteins} г, Жиры: ${product.fats} г, Углеводы: ${product.carbohydrates} г"
            productDescriptionTextView.text = product.description

            // Загрузка изображения из ресурсов
            val imageResource = resources.getIdentifier(product.picture_path, "drawable", packageName)
            if (imageResource != 0) {
                productImageView.setImageResource(imageResource)
            }
        }

        // Обработчик кнопки "Назад"
        backButton.setOnClickListener {
            finish() // Завершение текущей активности и возврат к предыдущей
        }
    }
}
