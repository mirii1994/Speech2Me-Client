package com.example.project;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class SignUp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Button btnSignUp = (Button)findViewById(R.id.buttonSignup);
        final EditText textViewRequest = (EditText) findViewById(R.id.editTextRequest);
        ImageView imageViewArrowBack = (ImageView) findViewById(R.id.imgArrowBack);
        final EditText editTextFirstName = (EditText) findViewById(R.id.editTextFirstName);
        final EditText editTextLastName = (EditText) findViewById(R.id.editTextLastName);
        final EditText editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        final EditText editTextPassword = (EditText)findViewById(R.id.editTextPassword);
        final Switch switchIsStudent = (Switch) findViewById(R.id.switchSignupAsStudent);
        final EditText editTextTeacherId = (EditText)findViewById(R.id.editTextTeacherId);


        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateUserDetails(editTextFirstName, editTextLastName, editTextEmail,
                                    editTextPassword, switchIsStudent.isChecked(), editTextTeacherId)){
                    String firstName = editTextFirstName.getText().toString();
                    String lastName = editTextLastName.getText().toString();
                    String email = editTextEmail.getText().toString();
                    String password = editTextPassword.getText().toString();
                    String teacherId = editTextTeacherId.getText().toString();

                    User currUser;
                    Intent intent;
                    if(switchIsStudent.isChecked())
                    {
                        //TODO: insert to students db
                        //TODO: insert to teacher to students db
                        //id = what we got from db. then insert to constructor
                        currUser = new Student(email, password, firstName,
                                                        lastName, teacherId);
                        InsertNewStudentToDataBase(currUser);
                        intent = new Intent(SignUp.this, StudentHomePage.class);
                        intent.putExtra("id", currUser.getmId());
                        startActivity(intent);
                    }
                    else
                    {
                        //TODO: insert to teacher db
                        //id = what we got from db. then insert to constructor
                        currUser = new Teacher(email, password, firstName, lastName);
                        intent = new Intent(SignUp.this, TeacherHomePage.class);
                        intent.putExtra("id", currUser.getmId());
                        startActivity(intent);
                    }
                }
                else
                {
                    Context context = getApplicationContext();
                    CharSequence text = "Some details were invalid";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
            }
        });

        switchIsStudent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editTextTeacherId.setText("");
                editTextTeacherId.setEnabled(isChecked);
            }
        });

        imageViewArrowBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                startActivity(new Intent(SignUp.this, LoginPage.class));
            }
        });
        /*RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://www.google.com";
// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        textViewRequest.setText(response.substring(0,500));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                textViewRequest.setText("Error!");
            }
        });
        queue.add(stringRequest);*/
    }



    private boolean validateUserDetails(EditText iFirstName, EditText iLastName, EditText iEmail,
                                        EditText iPassword, boolean iIsStudent, EditText iTeacherID){
        return validateName(iFirstName) &&
                                    validateName(iLastName) &&
                                    validateMail(iEmail) &&
                                    validatePassword(iPassword) &&
                                    validateTeacherId(iIsStudent, iTeacherID);
    }

    private boolean validateName(EditText iName){
        boolean isValidName = false;

        if(iName.equals("") || iName == null || !(AppUtils.IsLetters(iName.getText().toString()))) {
            iName.setBackgroundColor(Color.RED);
            iName.setText("");
        }
        else {
            isValidName = true;
            iName.setBackgroundColor(Color.TRANSPARENT);
        }

        return isValidName;
    }

    private boolean validateMail(EditText iEmail){
        boolean isValid = false;

        if(iEmail.getText().toString() != null)
        {
            if(!Patterns.EMAIL_ADDRESS.matcher(iEmail.getText().toString()).matches()){
                iEmail.setBackgroundColor(Color.RED);
            }
            else
            {
                iEmail.setBackgroundColor(Color.TRANSPARENT);
                isValid = true;
            }
        }

        return isValid;
    }

    private boolean validatePassword(EditText iPassword){
        boolean isValid = false;

        if(iPassword.getText().toString().matches("")){
            iPassword.setBackgroundColor(Color.RED);
        }
        else if(iPassword.getText().toString().length() > 8){
            iPassword.setBackgroundColor(Color.RED);
            iPassword.setText("");
            iPassword.setHint("Up to 8 characters");
        }
        else
        {
            iPassword.setBackgroundColor(Color.TRANSPARENT);
            iPassword.setHint("Password");
            isValid = true;
        }

        return isValid;
    }

    private boolean validateTeacherId(boolean iIsStudent, EditText iTeacherId){
        boolean isValid = true;
        String teacherId = iTeacherId.getText().toString();

        if(iIsStudent)
        {
            if(teacherId.matches("") || !isTeacherIdExists(teacherId))
            {
                iTeacherId.setBackgroundColor(Color.RED);
                isValid = false;
            }
            else
            {
                iTeacherId.setBackgroundColor(Color.TRANSPARENT);
            }
        }

        return isValid;
    }

    private boolean isTeacherIdExists(String iTeacherId){
        //TODO: the real check
        return true;
    }

    private void InsertNewStudentToDataBase(User iUser)
    {
        try {
            //JSONObject request = new JSONObject(new Gson().toJson(iUser, User.class));
            //System.out.print(request);

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("first_name", iUser.getmFirstName());
            jsonBody.put("last_name", iUser.getmLastName());
            jsonBody.put("password", iUser.getmPassword());
            jsonBody.put("user_type", iUser.getmType());
            jsonBody.put("email", iUser.getmEmail());
            jsonBody.put("user_type", "student");
            RequestQueue queue = Volley.newRequestQueue(this);
            String url ="https://speech-rec-server.herokuapp.com/user_signup/";
// Request a string response from the provided URL.
            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            System.out.print(response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.print("ERROR!");
                }
            });
            queue.add(jsonRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }


        //System.out.print(reques);
        //JSONparser json = (JSONObject)parser.parse(request);

    }

    //TODO: app design and colors

}
