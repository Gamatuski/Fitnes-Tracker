package com.example.fitnesstracker.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.viewmodel.CreationExtras;

import com.example.fitnesstracker.R;
import com.example.fitnesstracker.api.FitnessApi;
import com.example.fitnesstracker.api.RetrofitClient;
import com.example.fitnesstracker.models.StepsResponse;
import com.example.fitnesstracker.utils.StyleTitleText;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StepsFragment extends Fragment implements SensorEventListener, OnChartValueSelectedListener {
    private TextView titleTextView;
    private ProgressBar progressCircle;
    private TextView stepsCountTextView, todayTextView;
    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private int totalSteps = 0; // Общее количество шагов (с сенсора)
    private int goalSteps = 5000; // Цель шагов
    private BarChart barChart;
    private List<Integer> stepsDataList = new ArrayList<>(); // Список для хранения данных о шагах из БД (для графика)

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_steps, container, false);

        // Инициализация элементов UI
        progressCircle = view.findViewById(R.id.progressCircle);
        stepsCountTextView = view.findViewById(R.id.stepsCountTextView);
        barChart = view.findViewById(R.id.barChart);
        titleTextView = view.findViewById(R.id.titleTextView);
        todayTextView = view.findViewById(R.id.todayTextView);

        // Стилизация заголовка
        StyleTitleText styleTitleText = new StyleTitleText();
        styleTitleText.styleTitleText(titleTextView);

        // Инициализация SensorManager для шагомера
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }

        // Настройка графика
        setupBarChart();

        // Загрузка данных о шагах из базы данных с сервера
        loadStepsDataFromDatabase();

        // Установка слушателя нажатий на график
        barChart.setOnChartValueSelectedListener(this);

        return view;
    }

    private void loadStepsDataFromDatabase() {
        // Получение userId из SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null);

        // Проверка наличия userId
        if (userId == null) {
            Log.e("StepsFragment", "userId не найден в SharedPreferences. Пользователь не залогинен?");
            showError("Ошибка: Не удалось получить ID пользователя. Пожалуйста, войдите в систему.");
            return; // Выход из метода, если userId отсутствует
        }

        // Выполнение запроса к API для получения данных о шагах
        FitnessApi api = RetrofitClient.getClient().create(FitnessApi.class);
        Call<StepsResponse> call = api.getSteps(userId);
        call.enqueue(new Callback<StepsResponse>() {
            @Override
            public void onResponse(Call<StepsResponse> call, Response<StepsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StepsResponse stepsResponse = response.body();
                    if (stepsResponse.isSuccess()) {
                        stepsDataList = stepsResponse.getSteps();
                        if (stepsDataList != null && stepsDataList.size() == 7) {
                            updateBarChart(stepsDataList); // Обновление графика данными с сервера
                        } else {
                            showError("Неверный формат данных о шагах с сервера.");
                        }
                    } else {
                        showError(stepsResponse.getMessage() != null ? stepsResponse.getMessage() : "Ошибка при получении данных о шагах.");
                    }
                } else {
                    String message = "Ошибка сервера при загрузке данных о шагах.";
                    if (response.errorBody() != null) {
                        try {
                            message += " " + response.errorBody().string();
                            Log.e("StepsFragment", "Error Body: " + response.errorBody().string());
                        } catch (Exception e) {
                            Log.e("StepsFragment", "Ошибка при чтении тела ошибки", e);
                        }
                    }
                    showError(message);
                }
            }

            @Override
            public void onFailure(Call<StepsResponse> call, Throwable t) {
                showError("Ошибка сети при загрузке данных о шагах: " + t.getMessage());
                Log.e("StepsFragment", "Network Error: " + t.getMessage());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Регистрация слушателя шагов при возобновлении фрагмента
        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Отмена регистрации слушателя шагов при приостановке фрагмента
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            // Получение текущего количества шагов с сенсора
            totalSteps = (int) event.values[0];
            updateUIForCurrentSteps(); // Обновление UI для текущих шагов (без графика)
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Метод не используется
    }

    // Обновление UI для отображения текущих шагов и прогресса (без графика)
    private void updateUIForCurrentSteps() {
        stepsCountTextView.setText(String.valueOf(totalSteps)); // Обновление текста текущих шагов
        int progress = (int) ((totalSteps * 100f) / goalSteps); // Вычисление прогресса в процентах
        progressCircle.setProgress(progress); // Обновление прогресс-бара
    }

    // Настройка столбчатой диаграммы (оси, легенда, описание)
    private void setupBarChart() {
        barChart.getDescription().setEnabled(false); // Отключение описания
        barChart.setDrawGridBackground(false); // Отключение фоновой сетки (если есть фон)

        // Настройка оси X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false); // Отключение вертикальных линий сетки оси X
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7); // 7 дней
        xAxis.setValueFormatter(new IndexAxisValueFormatter(getDaysOfWeek())); // Подписи дней недели

        // Настройка оси Y
        YAxis yAxis = barChart.getAxisLeft();
        yAxis.setAxisMinimum(0f); // Минимальное значение
        yAxis.setAxisMaximum(10000f); // Максимальное значение (можно изменить)
        yAxis.setGranularity(1000f); // Шаг оси Y
        yAxis.setDrawLabels(false); // Отключение подписей оси Y (справа)
        yAxis.setDrawGridLines(false); // **Добавлено: Отключение горизонтальных линий сетки оси Y**
        barChart.getAxisRight().setEnabled(false); // Отключение правой оси Y

        // Настройка легенды
        barChart.getLegend().setEnabled(false); // Отключение легенды
    }

    // Обновление данных столбчатой диаграммы
    private void updateBarChart(List<Integer> stepsData) {
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < stepsData.size(); i++) {
            entries.add(new BarEntry(i, stepsData.get(i)));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Шаги");
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.purpule));
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);
        barChart.setData(barData);
        barChart.invalidate();
    }

    // Получение массива подписей дней недели для оси X графика
    private String[] getDaysOfWeek() {
        return new String[]{"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"};
    }

    // Обработка выбора столбца на графике
    @Override
    public void onValueSelected(Entry e, Highlight h) {
        int index = (int) e.getX();
        if (stepsDataList != null && index < stepsDataList.size()) {
            int steps = stepsDataList.get(index);
            String[] daysOfWeek = getDaysOfWeek();
            todayTextView.setText(daysOfWeek[index]); // Обновление TextView с выбранным днем
            stepsCountTextView.setText(String.valueOf(steps)); // Обновление TextView с шагами за выбранный день
            int progress = (int) ((steps * 100f) / goalSteps);
            progressCircle.setProgress(progress); // Обновление прогресс бара для выбранного дня
        } else {
            Log.e("StepsFragment", "Индекс выбранного столбца выходит за пределы данных или данные не загружены.");
            showError("Ошибка при выборе данных на графике.");
        }
    }


    @Override
    public void onNothingSelected() {
        // Метод не используется
    }

    // Отображение Toast сообщения об ошибке
    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    @NonNull
    @Override
    public CreationExtras getDefaultViewModelCreationExtras() {
        return super.getDefaultViewModelCreationExtras();
    }
}