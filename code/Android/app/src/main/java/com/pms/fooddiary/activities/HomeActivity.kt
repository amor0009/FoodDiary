package com.pms.fooddiary.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pms.fooddiary.API.APIClient
import com.pms.fooddiary.API.UserAPIService
import com.pms.fooddiary.API.UserWeightAPIService
import com.pms.fooddiary.R
import com.pms.fooddiary.activities.WeightActivity
import com.pms.fooddiary.models.UserResponse
import com.pms.fooddiary.models.UserUpdate
import com.pms.fooddiary.models.UserWeightRead
import com.pms.fooddiary.models.UserWeightUpdate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private lateinit var firstName: EditText
    private lateinit var lastName: EditText
    private lateinit var age: EditText
    private lateinit var height: EditText
    private lateinit var weight: EditText
    private lateinit var genderGroup: RadioGroup
    private lateinit var dietGroup: RadioGroup
    private lateinit var profileImage: ImageView
    private var profileImageUri: Uri? = null
    private lateinit var gestureDetector: GestureDetector


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        firstName = findViewById(R.id.first_name)
        lastName = findViewById(R.id.last_name)
        age = findViewById(R.id.age)
        height = findViewById(R.id.height)
        weight = findViewById(R.id.weight)
        genderGroup = findViewById(R.id.gender_group)
        dietGroup = findViewById(R.id.aim_of_diet)
        profileImage = findViewById(R.id.profile_image)

        val home_button: Button = findViewById(R.id.button_home)
        val weight_button: Button = findViewById(R.id.button_weight)
        val diary_button: Button = findViewById(R.id.button_diary)

        loadUserData()

        profileImage.setOnClickListener {
            selectImageFromGallery()
        }

        // Установка автообновления и отключение перехода при нажатии Enter
        setupAutoUpdateOnTextChange(firstName)
        setupAutoUpdateOnTextChange(lastName)
        setupAutoUpdateOnTextChange(age)
        setupAutoUpdateOnTextChange(height)
        setupAutoUpdateOnTextChange(weight)

        setupAutoUpdate()

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
                    if (e1.x > e2.x) {
                        // Свайп вправо
                        startActivity(Intent(this@HomeActivity, WeightActivity::class.java))
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

    private fun setupAutoUpdateOnTextChange(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateUserData()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Обрабатываем нажатие Enter для предотвращения переноса строки
        editText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {

                // Скрываем клавиатуру и убираем фокус
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(editText.windowToken, 0)
                editText.clearFocus()
                true
            } else {
                false
            }
        }
    }

    private fun loadUserData() {
        val retrofit = APIClient.createRetrofit(this@HomeActivity)
        val userService = retrofit.create(UserAPIService::class.java)
        userService.getCurrentUser().enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!.user
                    firstName.setText(user.firstname ?: "")
                    lastName.setText(user.lastname ?: "")
                    age.setText(user.age?.toString() ?: "")
                    height.setText(user.height?.toString() ?: "")
                    weight.setText(user.weight?.toString() ?: "")

                    genderGroup.check(
                        if (user.gender == "Мужской") R.id.gender_male else R.id.gender_female
                    )
                    dietGroup.check(
                        when (user.aim) {
                            "Похудение" -> R.id.diet
                            "Набор массы" -> R.id.gain_weight
                            else -> R.id.maintaining_weight
                        }
                    )

                    if (user.profileImagePath != null) {
                        profileImageUri = Uri.parse(user.profileImagePath)
                        profileImage.setImageURI(profileImageUri)
                    } else {
                        profileImage.setImageResource(R.drawable.dark)
                    }
                    updateCurrentWeight(user.weight!!)
                } else {
                    Toast.makeText(this@HomeActivity, "Failed to load user data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                profileImageUri = uri
                profileImage.setImageURI(profileImageUri)
                updateUserData()
            }
        }
    }

    private fun setupAutoUpdate() {
        genderGroup.setOnCheckedChangeListener { _, _ -> updateUserData() }
        dietGroup.setOnCheckedChangeListener { _, _ -> updateUserData() }
    }

    private fun updateCurrentWeight(user_weight: Float) {
        val userWeight = UserWeightUpdate(
            weight = user_weight
        )
        val retrofit = APIClient.createRetrofit(this@HomeActivity)
        val userWeightAPIService = retrofit.create(UserWeightAPIService::class.java)
        userWeightAPIService.updateUserWeight(userWeight).enqueue(object : Callback<UserWeightRead> {
            override fun onResponse(call: Call<UserWeightRead>, response: Response<UserWeightRead>) {
                if (response.isSuccessful) {
                    // Обработка успешного обновления
                } else {
                    // Ошибка сервера
                    Toast.makeText(this@HomeActivity, "Failed to update weight", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserWeightRead>, t: Throwable) {
                // Обработка ошибки подключения
                Toast.makeText(this@HomeActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun updateUserData() {
        val user = UserUpdate(
            firstname = if (firstName.text.isNotBlank()) firstName.text.toString() else null,
            lastname = if (lastName.text.isNotBlank()) lastName.text.toString() else null,
            age = if (age.text.isNotBlank()) age.text.toString().toIntOrNull() else null,
            height = if (height.text.isNotBlank()) height.text.toString().toIntOrNull() else null,
            weight = if (weight.text.isNotBlank()) weight.text.toString().toFloatOrNull() else null,
            gender = if (genderGroup.checkedRadioButtonId == R.id.gender_male) "Мужской" else "Женский",
            aim = when (dietGroup.checkedRadioButtonId) {
                R.id.diet -> "Похудение"
                R.id.gain_weight -> "Набор массы"
                else -> "Поддержание массы"
            },
            profileImagePath = profileImageUri?.toString()
        )

        val retrofit = APIClient.createRetrofit(this@HomeActivity)
        val userService = retrofit.create(UserAPIService::class.java)
        userService.updateUser(user).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (!response.isSuccessful) {
                    Toast.makeText(this@HomeActivity, "Failed to update user data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Toast.makeText(this@HomeActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            true
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    companion object {
        private const val GALLERY_REQUEST_CODE = 1000
    }
}
