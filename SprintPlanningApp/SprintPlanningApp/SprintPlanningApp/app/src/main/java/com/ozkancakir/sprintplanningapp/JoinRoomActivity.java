package com.ozkancakir.sprintplanningapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class JoinRoomActivity extends AppCompatActivity {

    private EditText yourNameEditText;
    private EditText roomNameEditText;
    private EditText passwordEditText;
    private Button joinRoomButton;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_room);

        yourNameEditText = findViewById(R.id.yourNameEditText);
        roomNameEditText = findViewById(R.id.roomNameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        joinRoomButton = findViewById(R.id.joinRoomButton);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        joinRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = yourNameEditText.getText().toString();
                String roomName = roomNameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (userName.isEmpty() || roomName.isEmpty() || password.isEmpty()) {
                    Toast.makeText(JoinRoomActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    joinRoom(userName, roomName, password);
                }
            }
        });
    }

    private void joinRoom(String userName, String roomName, String password) {
        databaseReference.child("rooms").child(roomName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Room room = dataSnapshot.getValue(Room.class);
                    if (room != null && room.getPassword().equals(password)) {
                        Toast.makeText(JoinRoomActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        databaseReference.child("rooms").child(roomName).child("stories").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot storiesSnapshot) {
                                if (storiesSnapshot.exists()) {
                                    ArrayList<String> stories = (ArrayList<String>) storiesSnapshot.getValue();
                                    Intent intent = new Intent(JoinRoomActivity.this, RoomActivity.class);
                                    intent.putExtra("username", userName);
                                    intent.putExtra("roomName", roomName);
                                    intent.putStringArrayListExtra("stories", stories);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(JoinRoomActivity.this, "No stories found in this room", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(JoinRoomActivity.this, "Database error", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(JoinRoomActivity.this, "Invalid room name or password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(JoinRoomActivity.this, "Room not available", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(JoinRoomActivity.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
