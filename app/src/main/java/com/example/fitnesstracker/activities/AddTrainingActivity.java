package com.example.fitnesstracker.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnesstracker.R;

public class AddTrainingActivity extends AppCompatActivity {

    private TextView cancelTextView, saveTextView;
    private EditText actionEditText, distanceEditText, caloriesEditText, stepsEditText, durationEditText, startDateEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_training);

        // Инициализация элементов UI
        cancelTextView = findViewById(R.id.cancelTextView);
        saveTextView = findViewById(R.id.saveTextView);
        actionEditText = findViewById(R.id.actionEditText);
        distanceEditText = findViewById(R.id.distanceEditText);
        caloriesEditText = findViewById(R.id.caloriesEditText);
        stepsEditText = findViewById(R.id.stepsEditText);
        durationEditText = findViewById(R.id.durationEditText);
        startDateEditText = findViewById(R.id.startDateEditText);

        // Обработчик нажатия на "Отменить"
        cancelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Закрываем текущую активность
            }
        });

        // Обработчик нажатия на "Сохранить"
        saveTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTrainingData();
            }
        });
    }

    private void saveTrainingData() {
        // Получение данных из EditText полей
        String action = actionEditText.getText().toString();
        String distance = distanceEditText.getText().toString();
        String calories = caloriesEditText.getText().toString();
        String steps = stepsEditText.getText().toString();
        String duration = durationEditText.getText().toString();
        String startDate = startDateEditText.getText().toString();

        // TODO: Валидация введенных данных (например, проверка на пустоту обязательных полей, форматы чисел и даты)

        // TODO: Сохранение данных тренировки в базу данных (здесь пока просто вывод в Toast)

        String message = "Действие: " + action + "\n" +
                "Растояние: " + distance + "\n" +
                "Калории: " + calories + "\n" +
                "Шаги: " + steps + "\n" +
                "Длительность: " + duration + "\n" +
                "Дата начала: " + startDate;

        Toast.makeText(this, "Данные сохранены:\n" + message, Toast.LENGTH_LONG).show();

        finish(); // Закрываем активность после "сохранения" (в реальности - после успешного сохранения в БД)
    }
}