package com.example.project;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;

public abstract class GameLevel extends AppCompatActivity {
    public QuestionsData questions = new QuestionsData();
    private ArrayList<Question> questionStatistics = new ArrayList<Question>();
    protected Question mQuestion;
    protected int sizeOfLevel;
    protected int questionNumber = 0;
    protected int mLevel;
    protected int succeededQuestions = 0;
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
    protected boolean mIsAudioResourcesFree;
    protected String mUserType;

    protected void moveToHomePage(String iCurrUserId, String iUserType)
    {
        Intent intent = new Intent(this, HomePage.class);
        intent.putExtra("id", iCurrUserId);
        intent.putExtra("user_type", iUserType);
        startActivity(intent);
    }

    protected void setBirdAnswerVisibility(ImageView iImageBird, TextView iTextBird, ImageView iImagePlay){
        iImageBird.setVisibility(View.VISIBLE);
        iTextBird.setVisibility(View.VISIBLE);
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
        answer.setText(R.string.answer);
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
        //mMediaRecorder.setAudioSamplingRate(16000);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setOutputFile(mPathSave);
    }

    protected void getNextQuestion(ImageView iImage, boolean isAudio){

        //continue to next question
        String imagePath;
        if (questionNumber < sizeOfLevel) {
            do {
                currQuestion = questions.getRandomQuestion(mLevel);
            } while (answeredQuestions[currQuestion.GetmId()] == 1);
            answeredQuestions[currQuestion.GetmId()] = 1;
            if (isAudio){
                mQuestion = new AudioRecognitionQuestion(currQuestion);
                imagePath = ((AudioRecognitionQuestion) mQuestion).GetmImageClue();
                answer.setText(R.string.answer_audio);
            }
            else{
                mQuestion = new PictureRegocnitionQuestion(currQuestion);
                imagePath = ((PictureRegocnitionQuestion) mQuestion).getmImgPath();
                answer.setText(R.string.answer_picture);
            }
            Picasso.with(this).load(imagePath).into(iImage);
            answer.setText(R.string.answer);

        }
        //finished level
        else {
            String text = String.format("כל הכבוד! סיימת את שלב %d!", mLevel);
            messageToUser(text);
            // SEND to server "mQuestion.GetmScore()"
            updateScore();
            if (succeededQuestions == sizeOfLevel && mUserType.equals("student")) {
                        ArrayList<JSONObject> answers = getAnswers();
                        System.out.println(answers);
                        String url = "https://speech-rec-server.herokuapp.com/finish_level/";
                        JSONObject jsonBody = new JSONObject();
                        try {
                            jsonBody.put("user_id", mId);
                            jsonBody.put("answers", answers);
                            jsonBody.put("level", mLevel);
                            final RequestQueue queue = Volley.newRequestQueue(this);
                            System.out.println("+++++++++++++++++++++++" + jsonBody);
                            final JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            updateLevel();
                                            System.out.println("@@@@@@@@@@@@@@@@@@@ " + response);
                                        }
                                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    System.out.println("ERROR!" + error.getMessage());
                                }
                            });
                            queue.add(jsonRequest);
                            System.out.println("###############SENT");

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

            }
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
            moveToHomePage(mId, mUserType);
        }
    }

    private ArrayList<JSONObject> getAnswers(){
        JSONObject answer = null;
        ArrayList<JSONObject> answers = new ArrayList<JSONObject>();
        for(Question question : questionStatistics){
            try {
                answer = new JSONObject();
                answer.put("isAudioClueUsed", question.IsClueUsed());
                answer.put("numOfTries", question.GetmNumOfTries());
                answer.put("word", question.GetmAnswer());
                answer.put("answer", question.GetmSucceeded());
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
            answers.add(answer);
        }
        return answers;
    }

    protected void messageToUser(CharSequence text)
    {
        Context context = getBaseContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    protected void isCorrectAnswer(MediaRecorder iRecorder, final Button iButton, final ImageView iImgWord, final boolean isAudio)  {
        String url = "https://speech-rec-server.herokuapp.com/check_talking/";
        File file = new File(mPathSave);
        InputStream inFile = null;
        try {
            inFile = new FileInputStream(mPathSave);
            byte[] bytes = fileToBytes();//inputStreamToByteArray(inFile);
            //new String(bytes, "UTF-8");
            String stringBytes = null;
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
                stringBytes = Base64.getMimeEncoder().encodeToString(bytes);

            } else {
                stringBytes = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT);
            }
            try {
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("original_text", mQuestion.GetmAnswer());
                jsonBody.put("id", mId);
                jsonBody.put("audio_file", stringBytes);
                final RequestQueue queue = Volley.newRequestQueue(this);
                RequestFuture<JSONObject> future = RequestFuture.newFuture();
                System.out.println("+++++++++++++++++++++++" + jsonBody);

                final JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                System.out.println("****************************" + response);
                                try {
                                    if(response.getString("answer").toLowerCase() == "true")
                                    {
                                        System.out.println("$$$$$$$$$$$$$$ is correct");
                                        setBirdAnswerVisibility(imageGoodJob, textGoodJob, iImgWord);
                                        questionNumber++;
                                        succeededQuestions++;
                                        mQuestion.SetmSucceeded();
                                        if (isAudio){
                                            questionStatistics.add((AudioRecognitionQuestion)mQuestion);
                                        }
                                        else{
                                            questionStatistics.add((PictureRegocnitionQuestion)mQuestion);
                                        }
                                        getNextQuestion(iImgWord, isAudio);
                                    }
                                    else
                                    {
                                        System.out.println("$$$$$$$$$$$$$$ is not correct");
                                        setBirdAnswerVisibility(imageTryAgain, textTryAgain, iImgWord);
                                        mQuestion.IncreasemNumOfTries();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },  new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("ERROR!" + error.getMessage());
                    }
                });
                queue.add(jsonRequest);
                System.out.println("###############SENT");

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] fileToBytes() {
        byte[] bytes = null;
        File audioFile = new File(mPathSave);
        try {
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){

                bytes = Files.readAllBytes(audioFile.toPath());

            } else {
                System.out.println("$$$$$$$$$$$$$$$$$IN LOWER APK");
                bytes = fileToBytesLowApk();
            }
            ///This part is for debug - checks if the convertion to bytes
            ///was ok by converting the bytes to file
            //String str = new String(bytes);
            //String str = new String(bytes);
//            System.out.println("*****************************" + str);
//            writeToFile(str, PictureRecognitionLevel.this);
//            File root
//            = new File(Environment.getExternalStorageDirectory(), "Decodes");
//            if (!root.exists()) {
//                root.mkdirs();
//            }
//            File gpxfile = new File(root, "audio_decode.mp3");
//            try (FileOutputStream fos = new FileOutputStream(gpxfile.getAbsolutePath())) {
//                fos.write(bytes);
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bytes;
    }

    private void updateScore(){
        String url = "https://speech-rec-server.herokuapp.com/update_level/";// TODO
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("user_id", mId);
            jsonBody.put("add_to_score", succeededQuestions);
            final RequestQueue queue = Volley.newRequestQueue(this);
            System.out.println("+++++++++++++++++++++++" + jsonBody);
            final JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            System.out.println("@@@@@@@@@@@@@@@@@@@ " + response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("ERROR!" + error.getMessage());
                }
            });
            queue.add(jsonRequest);
            System.out.println("###############SENT");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateLevel(){
        String url = "https://speech-rec-server.herokuapp.com/update_level/";// TODO
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("user_id", mId);
            final RequestQueue queue = Volley.newRequestQueue(this);
            System.out.println("+++++++++++++++++++++++" + jsonBody);
            final JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            System.out.println("@@@@@@@@@@@@@@@@@@@ " + response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("ERROR!" + error.getMessage());
                }
            });
            queue.add(jsonRequest);
            System.out.println("###############SENT");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private byte[] fileToBytesLowApk()
    {
        File file = new File(mPathSave);
        int size = (int) file.length();
        byte[] bytes = null;
        try {
            bytes = new byte[size];
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            bytes = null;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            bytes = null;
        }

        return bytes;
    }
}
