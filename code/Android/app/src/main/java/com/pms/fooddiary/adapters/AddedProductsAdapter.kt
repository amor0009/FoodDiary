package com.pms.fooddiary.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pms.fooddiary.R
import com.pms.fooddiary.models.ProductRead

class AddedProductsAdapter(
    private val products: MutableList<ProductRead>,
    private val onProductRemoved: (ProductRead) -> Unit,
    private val onWeightUpdated: (ProductRead, Double) -> Unit,
    private val onProductClicked: (ProductRead) -> Unit // Новый обработчик для клика
) : RecyclerView.Adapter<AddedProductsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product_added, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]

        // Устанавливаем имя и другие параметры
        holder.productNameTextView.text = product.name
        holder.productDetailsTextView.text = "Калории: ${product.calories} ккал, Белки: ${product.proteins} г, Жиры: ${product.fats} г, Углеводы: ${product.carbohydrates} г"

        // Убираем предыдущий TextWatcher перед установкой текста
        holder.productWeightInput.removeTextChangedListener(holder.textWatcher)

        // Устанавливаем текст в поле ввода массы
        val currentWeightText = holder.productWeightInput.text.toString()
        val productWeightText = product.weight.toString()
        if (currentWeightText != productWeightText) {
            holder.productWeightInput.setText(productWeightText)
            holder.productWeightInput.setSelection(productWeightText.length) // Устанавливаем курсор в конец
        }

        // Создаем новый TextWatcher
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Ничего не делаем при изменении текста в реальном времени
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Когда текст изменяется, ничего не делаем, пока не покинем поле или не нажмем Enter
            }
        }

        // Устанавливаем новый TextWatcher
        holder.productWeightInput.addTextChangedListener(textWatcher)
        holder.textWatcher = textWatcher

        // Обработчик кнопки удаления
        holder.deleteProductButton.setOnClickListener {
            onProductRemoved(product)
        }

        // Обработчик клика по продукту
        holder.itemView.setOnClickListener {
            onProductClicked(product)
        }
    }

    override fun getItemCount() = products.size

    // В ViewHolder добавим новые слушатели
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productNameTextView: TextView = itemView.findViewById(R.id.product_name_text_view)
        val productDetailsTextView: TextView = itemView.findViewById(R.id.product_details_text_view)
        val productWeightInput: EditText = itemView.findViewById(R.id.product_weight_input)
        val deleteProductButton: ImageButton = itemView.findViewById(R.id.delete_product_button)

        // Поле для хранения текущего TextWatcher
        var textWatcher: TextWatcher? = null

        init {
            // Обработчик выхода из поля
            productWeightInput.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    validateAndUpdateWeight()
                }
            }

            // Обработчик нажатия Enter
            productWeightInput.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                    validateAndUpdateWeight()
                    true
                } else {
                    false
                }
            }
        }

        private fun validateAndUpdateWeight() {
            val newWeightText = productWeightInput.text.toString()
            val newWeight = newWeightText.toDoubleOrNull()

            if (newWeight != null && newWeight != products[bindingAdapterPosition].weight) {
                onWeightUpdated(products[bindingAdapterPosition], newWeight)
            }
        }
    }
}
