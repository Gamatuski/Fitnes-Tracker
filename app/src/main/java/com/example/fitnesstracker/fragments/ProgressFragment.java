package com.example.fitnesstracker.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.fitnesstracker.R;
import com.example.fitnesstracker.activities.AddTrainingActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ProgressFragment extends Fragment {

    private TextView titleTextView;
    private FloatingActionButton addTrainingButton; // Объявляем FloatingActionButton

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress, container, false);

        // Инициализация элементов

        addTrainingButton = view.findViewById(R.id.addTrainingButton); // Инициализация кнопки

        // ... (Ваш код для стилизации titleTextView, если есть) ...

        // Обработчик нажатия на кнопку "+"
        addTrainingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddTrainingActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }
}