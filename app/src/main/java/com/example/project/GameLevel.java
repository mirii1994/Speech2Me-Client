package com.example.project;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.media.MediaRecorder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public abstract class GameLevel extends AppCompatActivity {
    public QuestionsData questions = new QuestionsData();
    protected Question mQuestion;
    protected int sizeOfLevel = 6;
    protected int questionNumber = 0;
    protected int REQUEST_ANSWER = 200;
    protected int mLevel;
    protected final int REQUEST_PREMISSION_CODE = 1000;
    protected int[] answeredQuestions;
    protected String mId;
    protected String mPathSave = "";
    protected boolean mIsRecording = false;
    protected boolean nextQuestion = false;
    protected Question currQuestion;
    protected Button answer;
    protected Button homePage;
    protected Button goToNextQuestion;
    protected ImageView imageTryAgain;
    protected ImageView imageGoodJob;
    protected TextView textClue;
    protected TextView textTryAgain;
    protected TextView textGoodJob;
    protected TextView textPressToContinue;
    protected MediaRecorder mMediaRecorder;



    protected void moveToHomePage(String iCurrUserId)
    {
        Intent intent = new Intent(this, HomePage.class);
        intent.putExtra("id", iCurrUserId);
        intent.putExtra("newScore", mQuestion.GetmScore());
        startActivity(intent);
    }

    protected void setBirdAnswerVisibility(ImageView iImage, TextView iText, ImageView iImagePlay){
        iImage.setVisibility(View.VISIBLE);
        iText.setVisibility(View.VISIBLE);
        textPressToContinue.setVisibility(View.VISIBLE);
        answer.setVisibility(View.INVISIBLE);
        textClue.setVisibility(View.INVISIBLE);
        iImagePlay.setVisibility(View.INVISIBLE);
        goToNextQuestion.setVisibility(View.INVISIBLE);
        nextQuestion = true;
    }

    protected void setNextLevelVisibility(ImageView iImage, TextView iText, ImageView iImagePlay) {
        if (nextQuestion) {
            iImage.setVisibility(View.INVISIBLE);
            iText.setVisibility(View.INVISIBLE);
            textPressToContinue.setVisibility(View.INVISIBLE);
            answer.setVisibility(View.VISIBLE);
            textClue.setVisibility(View.VISIBLE);
            iImagePlay.setVisibility(View.VISIBLE);
            goToNextQuestion.setVisibility(View.VISIBLE);
            nextQuestion = false;
        }
    }

    protected boolean checkPermissionFromDevice()
    {
        int write_external_storage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return write_external_storage_result == PackageManager.PERMISSION_GRANTED &&
                record_audio_result == PackageManager.PERMISSION_GRANTED;
    }

    protected void requestPermission()
    {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        }, REQUEST_PREMISSION_CODE);
    }

    protected void setupMediaRecorder() {
        mMediaRecorder = new MediaRecorder();
        //mMediaRecorder.setAudioSamplingRate(8000);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mMediaRecorder.setOutputFile(mPathSave);
    }

    protected void getNextQuestion(ImageView iImage){

        //continue to next question
        iImage.setVisibility(View.INVISIBLE);
        if (questionNumber < sizeOfLevel) {
            do {
                currQuestion = questions.getRandomQuestion(mLevel);
            } while (answeredQuestions[currQuestion.GetmId()] == 1);
            answeredQuestions[currQuestion.GetmId()] = 1;
            mQuestion = new AudioRecognitionQuestion(currQuestion);
            System.out.println("^^^^^^^^^^^^"+((AudioRecognitionQuestion) mQuestion).GetmImageClue());
            String imageCluePath = ((AudioRecognitionQuestion) mQuestion).GetmImageClue();
            Picasso.with(this).load(imageCluePath).into(iImage);
            answer.setText("ענה");
        }
        //finished level
        else {
            String text = String.format("כל הכבוד! סיימת את שלב %d!", mLevel);
            //SEND questionsStatistics TO SERVER AND DELETE IT FROM MEMORY
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(this, text, duration);
            toast.show();
            for (int i= 0; i< questions.getSizeOfLevel(mLevel);i++){
                answeredQuestions[i] = 0;
            }
            try
            {
                Thread.sleep(1000);
            }
            catch(InterruptedException ex)
            {
                Thread.currentThread().interrupt();
            }
            moveToHomePage(mId);
        }
    }
}