package com.example.hyugakei311.tictactoe;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.Image;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//import android.util.Log;
import android.graphics.Color;
import android.graphics.Point;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TicTacToe tttGame;
    private Button [][] buttons;
    private TextView status;
    private ImageButton speak;
    private static final int PLAY_REQUEST = 1;
    private static final String DEFAULT_SENT = "Please retry";
    public static final float MIN_CONFIDENCE = 0.5f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        tttGame = new TicTacToe();
        buildGuiByCode();
        speak = findViewById(R.id.speak);

        //Test if device supports speech recognition
        PackageManager manager = getPackageManager();
        List<ResolveInfo> listOfMatches = manager.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH),0);
        if (listOfMatches.size() > 0)
            listen();
        else {//speech recognition not supported
            speak.setEnabled(false);
            Toast.makeText(this, "Sorry - Your device does not support speech recognition", Toast.LENGTH_LONG).show();
        }
    }

    public void buildGuiByCode() {
        //Get width of the screen
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int w = size.x / TicTacToe.SIDE;

        //Create the layout manager as a GridLayout
        GridLayout gridLayout = new GridLayout(this);
        gridLayout.setColumnCount(TicTacToe.SIDE);
        gridLayout.setRowCount(TicTacToe.SIDE + 1);

        //Create the buttons and add them to gridLayout
        buttons = new Button[TicTacToe.SIDE][TicTacToe.SIDE];
        ButtonHandler bh = new ButtonHandler();
        for (int row = 0; row < TicTacToe.SIDE; row++) {
            for (int col = 0; col < TicTacToe.SIDE; col++) {
                buttons[row][col] = new Button(this);
                buttons[row][col].setTextSize((int) (w * .2));
                String temp = (char)(row+65) + "" + (col + 1);
                buttons[row][col].setText(temp);
                buttons[row][col].setOnClickListener(bh);
                gridLayout.addView(buttons[row][col], w, w);
            }
        }

        // set up layout parameters of 4th row of gridLayout
        status = new Button(this);
        GridLayout.Spec rowSpec = GridLayout.spec(TicTacToe.SIDE, 1);
        GridLayout.Spec colSpec = GridLayout.spec(0, TicTacToe.SIDE);
        GridLayout.LayoutParams lpStatus = new GridLayout.LayoutParams(rowSpec,colSpec);
        status.setLayoutParams(lpStatus);

        // set up status' characteristics
        status.setWidth(TicTacToe.SIDE * w);
        status.setHeight(w);
        status.setGravity(Gravity.CENTER);
        status.setBackgroundColor(Color.GREEN);
        status.setTextSize((int) (w*.15));
        status.setText(tttGame.result());

        gridLayout.addView(status);

        // set up btnSpeak
//               speak =  findViewById(R.id.speak);
//        rowSpec = GridLayout.spec(TicTacToe.SIDE + 1,1);
//        colSpec = GridLayout.spec(1, TicTacToe.SIDE);
//        gridLayout.addView(speak,new GridLayout.LayoutParams(rowSpec,colSpec));

        //Set gridLayout as the View of this Activity
        setContentView(gridLayout);
    }

    public void startSpeaking(View v){
        listen();
    }

    private void listen(){
        //speak.setEnabled(false);
        Intent listenIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        listenIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Position");
        listenIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        listenIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);
        startActivityForResult(listenIntent, PLAY_REQUEST);
    }

    public String firstMatchWithMinConfidence(ArrayList<String> sentences, float[] confidLevels){
        if (sentences == null || confidLevels == null){
            return DEFAULT_SENT;
        }

        int numberOfSentences = sentences.size();
        ArrayList<String> entries = new ArrayList<>(Arrays.asList("A1", "A2", "A3", "B1", "B2", "B3", "C1", "C2", "C3"));
        for (int i = 0; i < numberOfSentences && i < confidLevels.length;i++){
            if (confidLevels[i] < MIN_CONFIDENCE)
                break;
            String sentence = sentences.get(0);

            if (entries.contains(sentence)) {
                int row = (int) sentence.toUpperCase().charAt(0) - 65;
                int col = sentence.toUpperCase().charAt(1) - 1;
                update(row,col);
            }
            return sentence.toUpperCase();

        }
//        if(entries.contains(sentences.get(0).toUpperCase())) {
//            update((int) sentences.get(0).toUpperCase().charAt(0) - 65, sentences.get(0).toUpperCase().charAt(1) - 1);
//        }
//            return sentences.get(0).toUpperCase();
        return DEFAULT_SENT;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLAY_REQUEST && resultCode == RESULT_OK){
            //retrieve a list of possible words
            ArrayList<String> returnedWords = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            //retrieve array of scores for returnedWords
            float[] scores = data.getFloatArrayExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES);

            //retrieve first good match
            status.setText(firstMatchWithMinConfidence(returnedWords, scores));

            //output to screen
//            TextView output = findViewById(R.id.result);
//            String trans = entries.getTranslation(firstMatch);
//            if (trans != null)
//                output.setText(firstMatch.substring(0, 1).toUpperCase() + firstMatch.substring(1) + "\n" + trans);
//            else{
//                output.setText(returnedWords.get(0) + "\n" + firstMatch);
//            }

        }
        //speak.setEnabled(true);
    }

    public void update(int row, int col) {
        int play = tttGame.play(row,col);
        if (play == 1)
            buttons[row][col].setText("X");
        else if (play == 2)
            buttons[row][col].setText("O");
        if (tttGame.isGameOver()) { //game over, disable buttons
            status.setBackgroundColor(Color.RED);
            enableButtons();
            status.setText(tttGame.result());
        }
    }

    public void enableButtons(){
        for (int row = 0; row < TicTacToe.SIDE; row++)
            for (int col = 0; col < TicTacToe.SIDE; col++)
                buttons[row][col].setEnabled(false);
    }

    private class ButtonHandler implements View.OnClickListener{
        public void onClick(View v){
            for (int row = 0; row < TicTacToe.SIDE; row++)
                for (int col = 0; col < TicTacToe.SIDE; col++)
                    if (v == buttons[row][col])
                        update(row,col);
        }
    }
}
