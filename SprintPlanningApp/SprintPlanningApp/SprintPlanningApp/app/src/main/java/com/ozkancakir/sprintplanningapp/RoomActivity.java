package com.ozkancakir.sprintplanningapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.GridView;
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
import java.util.List;
import java.util.Map;

public class RoomActivity extends AppCompatActivity {

    private static final String TAG = "RoomActivity";

    private TextView roomCodeTextView;
    private TextView timerTextView;
    private GridView votingGridView;
    private GridLayout participantsGridLayout;
    private Button startTimerButton;
    private Button finishRoundButton;

    private Map<String, String> userVotes = new HashMap<>();
    private CountDownTimer countDownTimer;
    private String userName;
    private String roomName;
    private DatabaseReference databaseReference;
    private long timeLeftInMillis = 30000;

    private ArrayList<String> stories;
    private int currentStoryIndex = 0;

    private String[] fibonacciNumbers = {"0", "1", "2", "3", "5", "8", "13", "21", "34", "55", "89", "144", "?", "∞", "☕"};
    private int totalParticipants = 9;

    private boolean resultsShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        initViews();
        getDataFromIntent();
        setUpDatabaseReference();
        loadCurrentStory();

        votingGridView.setAdapter(new CustomGridAdapter(this, fibonacciNumbers));

        startTimerButton.setOnClickListener(v -> startTimer());
        finishRoundButton.setOnClickListener(v -> finishRound());

        votingGridView.setOnItemClickListener((parent, view, position, id) -> registerVote(fibonacciNumbers[position]));

        listenForVotes();
        listenForRoundFinish();
        listenForTimerStart();
        listenForNextRound();
        listenForParticipants();
    }

    private void initViews() {
        roomCodeTextView = findViewById(R.id.roomCodeTextView);
        timerTextView = findViewById(R.id.timerTextView);
        votingGridView = findViewById(R.id.votingGridView);
        participantsGridLayout = findViewById(R.id.participantsGridLayout);
        startTimerButton = findViewById(R.id.startTimerButton);
        finishRoundButton = findViewById(R.id.finishRoundButton);
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

    private void startTimer() {
        databaseReference.child("rooms").child(roomName).child("timerStarted").setValue(true);
    }

    private void listenForTimerStart() {
        databaseReference.child("rooms").child(roomName).child("timerStarted").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean timerStarted = dataSnapshot.getValue(Boolean.class);
                if (timerStarted != null && timerStarted) {
                    runTimer();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(RoomActivity.this, "Error starting timer: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void runTimer() {
        timeLeftInMillis = 30000;
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            public void onTick(long millisUntilFinished) {
                timerTextView.setText(String.valueOf(millisUntilFinished / 1000));
            }

            public void onFinish() {
                if (!resultsShown) {
                    databaseReference.child("rooms").child(roomName).child("roundFinished").setValue(true);
                }
            }
        }.start();
        startTimerButton.setVisibility(View.GONE);
    }

    private void registerVote(String vote) {
        userVotes.put(userName, vote);
        databaseReference.child("rooms").child(roomName).child("votes").setValue(userVotes);

        if (userVotes.size() == totalParticipants) {
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            if (!resultsShown) {
                databaseReference.child("rooms").child(roomName).child("roundFinished").setValue(true);
            }
        }
    }

    private void finishRound() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (!resultsShown) {
            databaseReference.child("rooms").child(roomName).child("roundFinished").setValue(true);
        }
    }

    private void listenForVotes() {
        databaseReference.child("rooms").child(roomName).child("votes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userVotes.clear();
                for (DataSnapshot voteSnapshot : dataSnapshot.getChildren()) {
                    String username = voteSnapshot.getKey();
                    String vote = voteSnapshot.getValue(String.class);
                    userVotes.put(username, vote);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(RoomActivity.this, "Error retrieving votes: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void listenForRoundFinish() {
        databaseReference.child("rooms").child(roomName).child("roundFinished").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean roundFinished = dataSnapshot.getValue(Boolean.class);
                if (roundFinished != null && roundFinished) {
                    if (!resultsShown) {
                        resultsShown = true;
                        showResults();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(RoomActivity.this, "Error finishing round: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void listenForNextRound() {
        databaseReference.child("rooms").child(roomName).child("nextRoundStarted").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean nextRoundStarted = dataSnapshot.getValue(Boolean.class);
                if (nextRoundStarted != null && nextRoundStarted) {
                    startNextRound();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(RoomActivity.this, "Error starting next round: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void listenForParticipants() {
        databaseReference.child("rooms").child(roomName).child("participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                participantsGridLayout.removeAllViews();
                for (DataSnapshot participantSnapshot : dataSnapshot.getChildren()) {
                    String participant = participantSnapshot.getValue(String.class);
                    if (participant != null && participant.length() > 3) {
                        participant = participant.substring(0, 3);
                    }

                    TextView participantTextView = new TextView(RoomActivity.this);
                    participantTextView.setText(participant);
                    participantTextView.setPadding(8, 8, 8, 8);

                    participantsGridLayout.addView(participantTextView);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(RoomActivity.this, "Error retrieving participants: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        databaseReference.child("rooms").child(roomName).child("participants").child(userName).setValue(userName);
    }

    private void showResults() {
        String result = computeResult();
        databaseReference.child("rooms").child(roomName).child("timerStarted").setValue(false);
        databaseReference.child("rooms").child(roomName).child("roundFinished").setValue(false);

        Intent intent = new Intent(RoomActivity.this, ResultsActivity.class);
        intent.putStringArrayListExtra("votesList", (ArrayList<String>) new ArrayList<>(userVotes.values()));
        intent.putExtra("roomName", roomName);
        intent.putExtra("username", userName);
        intent.putExtra("result", result);
        intent.putExtra("currentStoryIndex", currentStoryIndex);
        intent.putStringArrayListExtra("stories", stories);
        intent.putExtra("isFinalRound", currentStoryIndex >= stories.size() - 1);
        startActivity(intent);
        finish();
    }

    private void startNextRound() {
        databaseReference.child("rooms").child(roomName).child("nextRoundStarted").setValue(false);
        Intent nextRoundIntent = new Intent(RoomActivity.this, RoomActivity.class);
        nextRoundIntent.putExtra("username", userName);
        nextRoundIntent.putExtra("roomName", roomName);
        nextRoundIntent.putStringArrayListExtra("stories", stories);
        nextRoundIntent.putExtra("currentStoryIndex", currentStoryIndex + 1);
        startActivity(nextRoundIntent);
        finish();
    }

    private String computeResult() {
        Map<String, Integer> voteCounts = new HashMap<>();
        boolean hasInfinity = false;
        boolean hasQuestionMark = false;
        boolean hasCoffeeBreak = false;

        for (String vote : userVotes.values()) {
            switch (vote) {
                case "∞":
                    hasInfinity = true;
                    break;
                case "?":
                    hasQuestionMark = true;
                    break;
                case "☕":
                    hasCoffeeBreak = true;
                    break;
                default:
                    voteCounts.put(vote, voteCounts.getOrDefault(vote, 0) + 1);
                    break;
            }
        }

        if (hasInfinity) {
            return "∞";
        }
        if (hasQuestionMark) {
            return "?";
        }
        if (hasCoffeeBreak) {
            return "☕";
        }

        String highestVote = "";
        int highestCount = 0;

        String secondHighestVote = "";
        int secondHighestCount = 0;

        for (Map.Entry<String, Integer> entry : voteCounts.entrySet()) {
            if (entry.getValue() > highestCount) {
                secondHighestVote = highestVote;
                secondHighestCount = highestCount;

                highestVote = entry.getKey();
                highestCount = entry.getValue();
            } else if (entry.getValue() > secondHighestCount) {
                secondHighestVote = entry.getKey();
                secondHighestCount = entry.getValue();
            }
        }

        if (highestCount > secondHighestCount) {
            return highestVote;
        } else {
            return Integer.parseInt(highestVote) > Integer.parseInt(secondHighestVote) ? highestVote : secondHighestVote;
        }
    }
}
