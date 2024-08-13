package com.ozkancakir.sprintplanningapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button createRoomButton;
    private Button joinRoomButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createRoomButton = findViewById(R.id.createRoomButton);
        joinRoomButton = findViewById(R.id.joinRoomButton);

        createRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreateRoomActivity.class);
                startActivity(intent);
            }
        });

        joinRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, JoinRoomActivity.class);
                startActivity(intent);
            }
        });
    }
}
