package com.ozkancakir.sprintplanningapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateRoomActivity extends AppCompatActivity {

    private EditText yourNameEditText;
    private EditText roomNameEditText;
    private EditText passwordEditText;
    private Button createRoomButton;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);

        yourNameEditText = findViewById(R.id.yourNameEditText);
        roomNameEditText = findViewById(R.id.roomNameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        createRoomButton = findViewById(R.id.createRoomButton);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        createRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = yourNameEditText.getText().toString();
                String roomName = roomNameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (userName.isEmpty() || roomName.isEmpty() || password.isEmpty()) {
                    Toast.makeText(CreateRoomActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    createRoom(userName, roomName, password);
                }
            }
        });
    }

    private void createRoom(String userName, String roomName, String password) {
        Room room = new Room(roomName, password, userName);
        databaseReference.child("rooms").child(roomName).setValue(room).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(CreateRoomActivity.this, "Room created", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(CreateRoomActivity.this, EnterStoriesActivity.class);
                intent.putExtra("username", userName);
                intent.putExtra("roomName", roomName);
                startActivity(intent);
            } else {
                Toast.makeText(CreateRoomActivity.this, "Room creation failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
