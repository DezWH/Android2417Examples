package com.clara.aliens;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.LinkedList;


/**
 *
 * Created by clara on 9/28/16. Handles interaction with Firebase.
 *
 */

class Firebase {

	private static final String ALL_SCORES_KEY = "all_high_scores";
	private static final String TAG = "FIREBASE";
	private LinkedList<GameScore> highScores;
	private DatabaseReference dbReference;

	private LocalStorage localStorage;     //Local high mScore storage


	interface HighScoreUpdateListener {
		void highScoresUpdated(LinkedList<GameScore> scores);
	}


	Firebase(LocalStorage localStorage) {

		this.localStorage = localStorage;
		FirebaseDatabase database = FirebaseDatabase.getInstance();
		dbReference = database.getReference();

	}

	//FIXME - if user plays game for the first time online, and then connects to the internet,
	// their high mScore won't be written to Firebase until they beat that mScore.

	//If new high mScore for this user, replace old mScore with new mScore.


	String addHighScore(GameScore gameScore) {
		DatabaseReference newUserRef = dbReference.child(ALL_SCORES_KEY).push();
		newUserRef.setValue(gameScore);
		return newUserRef.getKey();
	}


	void saveHighScore(String userKey, GameScore gameScore) {
		dbReference.child(ALL_SCORES_KEY).child(userKey).setValue(gameScore);
	}


	void getSortedHighScores(final HighScoreUpdateListener highScoreListener) {

		Log.d(TAG, "Fetching scores from firebase");

		// Query - all scores, sort by mScore, which sorts lowest to highest.
		// Would like the high mScore table to display the 20 highest scores, not everything.
		// As the data is sorted, we need the last 20 results, so limit to the last 20. - for example, 999, 1000, 1010, ....
		// Sorting in reverse order is currently not supported. We'll have to sort in code.
		Query allHighScores = dbReference.child(ALL_SCORES_KEY).orderByChild("score").limitToLast(20);

		allHighScores.addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {

				Log.d(TAG, "onDataChange " + dataSnapshot);

				highScores = new LinkedList<>();

				for (DataSnapshot ds : dataSnapshot.getChildren()) {
					
					Log.d(TAG, "ds: " + ds);
					
					GameScore score = ds.getValue(GameScore.class);
					highScores.add(0, score);   // Add to list in reverse order to end up
					                            // with highest mScore first
				}

				//Notify listener that the scores are ready to display
				highScoreListener.highScoresUpdated(highScores);

			}

			@Override
			public void onCancelled(DatabaseError databaseError) {
				Log.e(TAG, "Get sorted high scores error " + databaseError);
			}
		});
	}


}
