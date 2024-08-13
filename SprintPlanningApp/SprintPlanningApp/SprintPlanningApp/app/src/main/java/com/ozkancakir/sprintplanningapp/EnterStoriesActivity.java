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

import java.util.ArrayList;

public class EnterStoriesActivity extends AppCompatActivity {

    private EditText storiesEditText;
    private Button saveAndAddNewButton;
    private Button saveAndCloseButton;

    private String userName;
    private String roomName;
    private DatabaseReference databaseReference;

    private ArrayList<String> storiesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_stories);

        storiesEditText = findViewById(R.id.storiesEditText);
        saveAndAddNewButton = findViewById(R.id.saveAndAddNewButton);
        saveAndCloseButton = findViewById(R.id.saveAndCloseButton);

        Intent intent = getIntent();
        userName = intent.getStringExtra("username");
        roomName = intent.getStringExtra("roomName");

        databaseReference = FirebaseDatabase.getInstance().getReference();

        saveAndAddNewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String storyText = storiesEditText.getText().toString().trim();
                if (!storyText.isEmpty()) {
                    storiesList.add(storyText);
                    storiesEditText.setText("");
                    Toast.makeText(EnterStoriesActivity.this, "Story added", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(EnterStoriesActivity.this, "Please enter a story", Toast.LENGTH_SHORT).show();
                }
            }
        });

        saveAndCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String storyText = storiesEditText.getText().toString().trim();
                if (!storyText.isEmpty()) {
                    storiesList.add(storyText);
                }
                if (!storiesList.isEmpty()) {
                    databaseReference.child("rooms").child(roomName).child("stories").setValue(storiesList);
                    databaseReference.child("rooms").child(roomName).child("currentStoryIndex").setValue(0);
                    Intent roomIntent = new Intent(EnterStoriesActivity.this, RoomActivity.class);
                    roomIntent.putExtra("username", userName);
                    roomIntent.putExtra("roomName", roomName);
                    roomIntent.putStringArrayListExtra("stories", storiesList);
                    startActivity(roomIntent);
                    finish();
                } else {
                    Toast.makeText(EnterStoriesActivity.this, "Please add at least one story", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
