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
import com.pms.fooddiary.security.TokenManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.auth)

        val userLogin: EditText = findViewById(R.id.user_login_auth)
        val userPassword: EditText = findViewById(R.id.user_password_auth)
        val button: Button = findViewById(R.id.button_auth)
        val linkToReg: TextView = findViewById(R.id.link_to_reg)

        linkToReg.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        button.setOnClickListener {
            val email_login = userLogin.text.toString().trim()
            val password = userPassword.text.toString().trim()

            if (email_login.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Не все поля заполнены", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val retrofit = APIClient.createRetrofit(this@AuthActivity)
            val authService = retrofit.create(AuthAPIService::class.java)
            authService.loginUser(email_login, password).enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                    if (response.isSuccessful) {
                        val tokenManager = TokenManager(this@AuthActivity)
                        tokenManager.saveToken(response.body()?.access_token ?: "")
                        Toast.makeText(this@AuthActivity, "Авторизация успешна", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this@AuthActivity, HomeActivity::class.java))
                    } else {
                        Toast.makeText(this@AuthActivity, "Неверный логин или пароль", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    Toast.makeText(this@AuthActivity, "Ошибка сети: ${t.message}", Toast.LENGTH_LONG).show() // Выводим сообщение об ошибке
                }
            })
        }
    }
}