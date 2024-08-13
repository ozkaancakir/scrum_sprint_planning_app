package com.ozkancakir.sprintplanningapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ResultsActivity extends AppCompatActivity {

    private TextView roomCodeTextView;
    private TextView resultTextView;
    private TextView votesTextView;
    private TextView allResultsTextView;
    private Button nextRoundButton;
    private Button endSessionButton;

    private String userName;
    private String roomName;
    private ArrayList<String> stories;
    private int currentStoryIndex;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        initViews();
        getDataFromIntent();
        setUpDatabaseReference();
        loadCurrentStory();

        if (savedInstanceState == null) {
            calculateAndDisplayResult();
            calculateAndDisplayVotes();
            displayAllResults();
        }

        if (isFinalRound()) {
            nextRoundButton.setVisibility(View.GONE);
        } else {
            nextRoundButton.setOnClickListener(v -> startNextRound());
        }

        endSessionButton.setOnClickListener(v -> endSession());
        listenForSessionEnd();
    }

    private void initViews() {
        roomCodeTextView = findViewById(R.id.roomCodeTextView);
        resultTextView = findViewById(R.id.resultTextView);
        votesTextView = findViewById(R.id.votesTextView);
        allResultsTextView = findViewById(R.id.allResultsTextView);
        nextRoundButton = findViewById(R.id.nextRoundButton);
        endSessionButton = findViewById(R.id.endSessionButton);
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        userName = intent.getStringExtra("username");
        roomName = intent.getStringExtra("roomName");
        stories = intent.getStringArrayListExtra("stories");
        currentStoryIndex = intent.getIntExtra("currentStoryIndex", 0);
    }

    private void setUpDatabaseReference() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    private void loadCurrentStory() {
        if (stories != null && currentStoryIndex < stories.size()) {
            String currentStory = stories.get(currentStoryIndex);
            roomCodeTextView.setText("Story: " + currentStory);
        }
    }

    private void calculateAndDisplayResult() {
        final String currentStoryName = stories.get(currentStoryIndex);
        final DatabaseReference resultRef = databaseReference.child("rooms").child(roomName).child("allResults");

        resultRef.orderByValue().equalTo(currentStoryName + ": " + resultTextView.getText().toString())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            return;
                        }

                        databaseReference.child("rooms").child(roomName).child("votes").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Map<String, Integer> voteCount = new HashMap<>();
                                for (DataSnapshot voteSnapshot : dataSnapshot.getChildren()) {
                                    String vote = voteSnapshot.getValue(String.class);
                                    if (vote != null && vote.matches("\\d+")) {
                                        int voteValue = Integer.parseInt(vote);
                                        voteCount.put(vote, voteCount.getOrDefault(vote, 0) + 1);
                                    }
                                }

                                String result = calculateResult(voteCount);
                                resultTextView.setText("Result: " + result);

                                resultRef.push().setValue(currentStoryName + ": " + result);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(ResultsActivity.this, "Error loading votes: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(ResultsActivity.this, "Error checking result: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void calculateAndDisplayVotes() {
        databaseReference.child("rooms").child(roomName).child("votes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Integer> voteCount = new HashMap<>();
                StringBuilder votesText = new StringBuilder("Votes:\n");

                for (DataSnapshot voteSnapshot : dataSnapshot.getChildren()) {
                    String vote = voteSnapshot.getValue(String.class);
                    if (vote != null) {
                        voteCount.put(vote, voteCount.getOrDefault(vote, 0) + 1);
                    }
                }

                for (Map.Entry<String, Integer> entry : voteCount.entrySet()) {
                    votesText.append(entry.getKey()).append(": ").append(entry.getValue()).append(" votes\n");
                }

                votesTextView.setText(votesText.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ResultsActivity.this, "Error loading votes: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayAllResults() {
        databaseReference.child("rooms").child(roomName).child("allResults").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashSet<String> uniqueResults = new HashSet<>();
                StringBuilder allResults = new StringBuilder();

                for (DataSnapshot resultSnapshot : dataSnapshot.getChildren()) {
                    String result = resultSnapshot.getValue(String.class);
                    if (result != null && uniqueResults.add(result)) {
                        allResults.append(result).append("\n");
                    }
                }
                allResultsTextView.setText(allResults.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ResultsActivity.this, "Error loading all results: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String calculateResult(Map<String, Integer> voteCount) {
        if (voteCount.isEmpty()) return "No votes";

        int highestVote = -1, secondHighestVote = -1;
        for (String vote : voteCount.keySet()) {
            int voteValue = Integer.parseInt(vote);
            if (voteValue > highestVote) {
                secondHighestVote = highestVote;
                highestVote = voteValue;
            } else if (voteValue > secondHighestVote) {
                secondHighestVote = voteValue;
            }
        }

        if (secondHighestVote == -1) {
            return String.valueOf(highestVote);
        }

        int highestVoteCount = voteCount.get(String.valueOf(highestVote));
        int secondHighestVoteCount = voteCount.get(String.valueOf(secondHighestVote));

        if (highestVoteCount == secondHighestVoteCount) {
            return String.valueOf(Math.max(highestVote, secondHighestVote));
        } else {
            return String.valueOf(highestVote);
        }
    }

    private boolean isFinalRound() {
        return currentStoryIndex >= stories.size() - 1;
    }

    private void startNextRound() {
        databaseReference.child("rooms").child(roomName).child("nextRoundStarted").setValue(true);
    }

    private void endSession() {
        databaseReference.child("rooms").child(roomName).child("sessionEnded").setValue(true);
    }

    private void listenForSessionEnd() {
        databaseReference.child("rooms").child(roomName).child("sessionEnded").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean sessionEnded = dataSnapshot.getValue(Boolean.class);
                if (sessionEnded != null && sessionEnded) {
                    navigateToMainActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ResultsActivity.this, "Error ending session: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToMainActivity() {
        databaseReference.child("rooms").child(roomName).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ResultsActivity.this, "Session ended successfully", Toast.LENGTH_SHORT).show();
                Intent mainIntent = new Intent(ResultsActivity.this, MainActivity.class);
                startActivity(mainIntent);
                finish();
            } else {
                Toast.makeText(ResultsActivity.this, "Failed to end session", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
