package com.pms.fooddiary.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pms.fooddiary.API.APIClient
import com.pms.fooddiary.API.AuthAPIService
import com.pms.fooddiary.R
import com.pms.fooddiary.models.AuthResponse
import com.pms.fooddiary.models.UserRegistration
import com.pms.fooddiary.security.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration)

        val userLogin: EditText = findViewById(R.id.user_login)
        val userEmail: EditText = findViewById(R.id.user_email)
        val userPassword: EditText = findViewById(R.id.user_password)
        val button: Button = findViewById(R.id.button_registration)
        val linkToAuth: TextView = findViewById(R.id.link_to_auth)

        linkToAuth.setOnClickListener {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }

        button.setOnClickListener {
            val login = userLogin.text.toString().trim()
            val email = userEmail.text.toString().trim()
            val password = userPassword.text.toString().trim()

            if (login.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Не все данные заполнены", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val user = UserRegistration(login, email, password)
            val retrofit = APIClient.createRetrofit(this@MainActivity)
            val authService = retrofit.create(AuthAPIService::class.java)
            authService.registerUser(user).enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    if (response.isSuccessful) {
                        val tokenManager = TokenManager(this@MainActivity)
                        tokenManager.saveToken(response.body()?.access_token ?: "")
                        Toast.makeText(this@MainActivity, "Регистрация успешна", Toast.LENGTH_LONG).show()
                        userLogin.text.clear()
                        userEmail.text.clear()
                        userPassword.text.clear()
                        startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                    } else {
                        Toast.makeText(this@MainActivity, "Ошибка регистрации", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_LONG).show() // Выводим сообщение об ошибке
                }
            })


            userLogin.text.clear()
            userEmail.text.clear()
            userPassword.text.clear()

        }
    }
}
