package com.example.project;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//TODO: rearrange set spinners functions
public class ViewStudentsDataFragment extends Fragment {
    private Teacher mTeacher;
    private HashMap<String, String> mStudentIdsToNames = null;
    private List<String> mStudentNames = null;
    private ScrollView scrollViewDetails;
    private RelativeLayout relativeLayoutDetails;
    private Spinner spinnerLevel;
    private Spinner spinnerWord;
    private Spinner spinnerChooseStudent;

    public ViewStudentsDataFragment() {
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_view_students_data, container, false);
        //final ScrollView scrollViewDetails = (ScrollView)v.findViewById(R.id.scrollViewDetails);
        scrollViewDetails = (ScrollView)v.findViewById(R.id.scrollViewDetails);
        scrollViewDetails.setVisibility(View.INVISIBLE);
        //final RelativeLayout relativeLayoutDetails = (RelativeLayout)v.findViewById(R.id.relativeLayoutDetails);
        relativeLayoutDetails = (RelativeLayout)v.findViewById(R.id.relativeLayoutDetails);
        //final Spinner spinnerLevel = (Spinner)v.findViewById(R.id.spinnerChooseLevel);
        spinnerLevel = (Spinner)v.findViewById(R.id.spinnerChooseLevel);
        //final Spinner spinnerWord = (Spinner)v.findViewById(R.id.spinnerChooseWord);
        spinnerWord = (Spinner)v.findViewById(R.id.spinnerChooseWord);
        spinnerChooseStudent = (Spinner)v.findViewById(R.id.spinnerChooseStudent);
        relativeLayoutDetails.setVisibility(View.INVISIBLE);
        setAllStudentDetailsVisibility(View.INVISIBLE, relativeLayoutDetails);

        if(getArguments() != null)
        {
            mTeacher = getArguments().getParcelable("user");
        }
        getStudents(scrollViewDetails, relativeLayoutDetails, spinnerLevel, spinnerWord);

        return v;
    }

    private List<String> setStudents()
    {
        boolean isNeedToAddChooseStudent = true;
        String id = " ";
        String studentName = " ";
        JSONObject jsonObject = null;
        ArrayList<String> studentNames = new ArrayList<String>();
        List<String> ids = new ArrayList<String>(mStudentIdsToNames.keySet());
        //ids = mStudentIdsToNames.keySet().toArray(new String[mStudentIdsToNames.size()]);
        System.out.println("SIZE OF ids: " + mStudentIdsToNames.keySet().size());
        for(int i=0; i<mStudentIdsToNames.size(); i++)
        {
            if(isNeedToAddChooseStudent)
            {
                studentNames.add("בחר תלמיד");
                isNeedToAddChooseStudent = false;
                i--;
            }
            else
            {
                System.out.println("INDEX: " + i);
                System.out.println("ID: " + ids.get(i));
                id = ids.get(i);
                studentName = mStudentIdsToNames.get(id);
                studentNames.add(studentName);
            }
        }

        return studentNames;
    }

    private void setAllStudentDetailsVisibility(int iVisibility, RelativeLayout oRelativeLayout)
    {
        int detailsInfoCound = oRelativeLayout.getChildCount();
        for(int i=0; i<detailsInfoCound; i++)
        {
            oRelativeLayout.getChildAt(i).setVisibility(iVisibility);
        }
    }

    private void setSpinnerLevel(Spinner iSpinnerLevel)
    {
        String[] levels = {"1", "2"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, levels);
        iSpinnerLevel.setAdapter(adapter);
        iSpinnerLevel.setSelection(0);
    }

    private void setSpinnerWord(Spinner iSpinnerWord)
    {
        String[] levels = {"אבטיח", "בית"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, levels);
        iSpinnerWord.setAdapter(adapter);
        iSpinnerWord.setSelection(0);
    }

    private void getStudents(final ScrollView iScrollViewDetails, final RelativeLayout iRelativeLayoutDetails, final Spinner iSpinnerLevel,
                             final Spinner iSpinnerWord)
    {
        String url = "https://speech-rec-server.herokuapp.com/get_students_of_teacher/";
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("user_id", mTeacher.getmId());
            final RequestQueue queue = Volley.newRequestQueue(this.getContext());
            RequestFuture<JSONObject> future = RequestFuture.newFuture();
            System.out.println("+++++++++++++++++++++++" + jsonBody);

            final JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            System.out.println("****************************" + response);
                            try {
                                JSONArray students = response.getJSONArray("students");
                                setIdToStudents(students);
                                mStudentNames = setStudents();
                                setSpinners();
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
    }

    private void getLevelWords(String iLevel, String iStudentId) {
        String url = "";//TODO
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("level", iLevel);
            jsonBody.put("student_id", iStudentId);
            final RequestQueue queue = Volley.newRequestQueue(this.getContext());
            RequestFuture<JSONObject> future = RequestFuture.newFuture();
            //final JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody, future, future);
            System.out.println("+++++++++++++++++++++++" + jsonBody);

            final JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            System.out.println("****************************" + response);
                            //DO SOMETHING
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

    private void setIdToStudents(JSONArray iStudents)
    {
        String id = " ";
        String studentName = " ";
        JSONObject jsonObject = null;
        mStudentIdsToNames = new HashMap<>();
        ///TODO: get real student;
        for(int i=0; i<=mTeacher.getmNumOfStudents(); i++)
        {
            try {
                jsonObject = iStudents.getJSONObject(i);
                id = jsonObject.getString("user_id");
                System.out.println("ID FROM JSON: " + id);
                studentName = jsonObject.getString("first_name") + " " + jsonObject.getString("last_name");
                System.out.println("NAME FROM JSON: " + studentName);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
            mStudentIdsToNames.put(id, studentName);
        }

        System.out.println("NUM OF STUDENTS IN HASH MAP: " + mStudentIdsToNames.size());
    }

    private void setSpinners()
    {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, mStudentNames);
        spinnerChooseStudent.setAdapter(adapter);
        spinnerChooseStudent.setSelection(0);

        spinnerChooseStudent.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("############# IN ON ITEM SELECTED");
                System.out.println("#############" + spinnerChooseStudent.getSelectedItem());
                if(spinnerChooseStudent.getSelectedItemPosition() != 0)
                {
                    scrollViewDetails.setVisibility(View.VISIBLE);
                    relativeLayoutDetails.setVisibility(View.VISIBLE);
                    setAllStudentDetailsVisibility(View.VISIBLE, relativeLayoutDetails);
                    setSpinnerLevel(spinnerLevel);
                    setSpinnerWord(spinnerWord);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerWord.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
