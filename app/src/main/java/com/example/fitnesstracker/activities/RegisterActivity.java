package com.example.fitnesstracker.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.fitnesstracker.R;
import com.example.fitnesstracker.api.FitnessApi;
import com.example.fitnesstracker.api.RetrofitClient;
import com.example.fitnesstracker.models.RegisterRequest;
import com.example.fitnesstracker.models.RegisterResponse;
import com.example.fitnesstracker.utils.StyleTitleText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText, heightEditText, weightEditText;
    private Button registerButton;
    private CardView registerCard;
    private TextView titleTextView, errorTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        heightEditText = findViewById(R.id.heightEditText);
        weightEditText = findViewById(R.id.weightEditText);
        registerButton = findViewById(R.id.registerButton);
        registerCard = findViewById(R.id.registerCard);
        titleTextView = findViewById(R.id.titleTextView);
        errorTextView = findViewById(R.id.errorTextView);

        // Стилизация заголовка
        StyleTitleText styleTitleText = new StyleTitleText();
        styleTitleText.styleTitleText(titleTextView);

        setupFocusListeners();

        registerButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            int height = Integer.parseInt(heightEditText.getText().toString());
            int weight = Integer.parseInt(weightEditText.getText().toString());

            if (validateInput(email, password, height, weight)) {
                registerUser(email, password, height, weight);
            }
        });
    }


    private void setupFocusListeners() {
        View.OnFocusChangeListener focusListener = (v, hasFocus) -> {
            if (hasFocus) {
                // Поднимаем карточку при фокусе
                registerCard.animate().translationY(-100).setDuration(200).start();
            } else {
                // Возвращаем карточку на место при потере фокуса
                registerCard.animate().translationY(0).setDuration(200).start();
            }
        };

        emailEditText.setOnFocusChangeListener(focusListener);
        passwordEditText.setOnFocusChangeListener(focusListener);
        heightEditText.setOnFocusChangeListener(focusListener);
        weightEditText.setOnFocusChangeListener(focusListener);
    }

    private void registerUser(String email, String password, int height, int weight) {
        RegisterRequest registerRequest = new RegisterRequest(email, password, height, weight);

        // Логирование тела запроса
        Log.d("RegisterActivity", "Отправка запроса: " + registerRequest.toString());

        FitnessApi api = RetrofitClient.getClient().create(FitnessApi.class);
        Call<RegisterResponse> call = api.register(registerRequest);
        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful()) {
                    RegisterResponse registerResponse = response.body();
                    if (registerResponse != null && registerResponse.isSuccess()) {
                        // Успешная регистрация
                        Toast.makeText(RegisterActivity.this, "Регистрация успешна", Toast.LENGTH_SHORT).show();
                        Log.d("RegisterActivity", "Response Success: " + new Gson().toJson(registerResponse)); // Log full success response
                        // Закрываем текущую активность и переходим к LoginActivity
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish(); // Закрываем RegisterActivity
                    } else {
                        // Ошибка регистрации
                        showError(registerResponse != null ? registerResponse.getMessage() : "Ошибка регистрации");
                        Log.d("RegisterActivity", "Response Code: " + response.code()); // Log response code
                    }
                } else {
                    // Ошибка сервера (например, 400 Bad Request)
                    try {
                        // Извлекаем тело ошибки
                        String errorBody = response.errorBody().string();
                        Log.e("RegisterActivity", "Тело ошибки: " + errorBody);

                        // Парсим JSON-ответ
                        JsonObject errorJson = JsonParser.parseString(errorBody).getAsJsonObject();
                        String errorMessage = errorJson.get("message").getAsString();

                        // Показываем сообщение об ошибке
                        showError(errorMessage);
                    } catch (IOException e) {
                        e.printStackTrace();
                        showError("Ошибка при обработке ответа сервера");
                    }
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                // Ошибка сети
                Log.e("RegisterActivity", "Ошибка сети: " + t.getMessage());
                showError("Ошибка сети");
            }
        });
    }

    private boolean validateInput(String email, String password, int height, int weight) {
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Введите корректный email");
            return false;
        }
        if (password.isEmpty() || password.length() < 6) {
            showError("Пароль должен содержать не менее 6 символов");
            return false;
        }
        if (height <= 0 || weight <= 0) {
            showError("Рост и вес должны быть положительными числами");
            return false;
        }
        return true;
    }

    private void showError(String errorMessage) {
        // Показываем текст ошибки
        errorTextView.setText(errorMessage);
        errorTextView.setVisibility(View.VISIBLE);

        // Красная обводка для карточки
        registerCard.setCardBackgroundColor(Color.parseColor("#FFF0F0")); // Светло-красный фон
    }

    private void resetErrorUI() {
        // Скрываем текст ошибки
        errorTextView.setVisibility(View.GONE);

        // Возвращаем стандартный фон для карточки
        registerCard.setCardBackgroundColor(Color.WHITE);
    }
}