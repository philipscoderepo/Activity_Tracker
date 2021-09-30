package com.example.activity_tracker;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    public class Week {
        String date;
        Hashtable<String, Day> days;
    }

    public class Day{
        String name;
        Boolean completed;
        Vector<Activity> activities;

        Day(String name, Vector<Activity> activities, Boolean completed){
            this.name = name;
            this.activities = activities;
            this.completed = completed;
        }

        void addActivity(Activity activity){
            activities.addElement(activity);
        }

        void checkDayComplete(){
            //Only call this function in a runnable thread
            for(Activity a : this.activities){
                if(!a.completed){
                    this.completed = false;
                    return;
                }
            }
            this.completed = true;
        }
    }

    public class Activity{
        String name;
        Boolean completed;

        Activity(){
            name = " ";
            completed = false;
        }

        Activity(String name, String activityType, String value, Boolean completed){
            this.name = name;
            this.completed = completed;
        }
    }

    Week week;
    String currentDay;
    String today;
    final String pathName = "data/data/com.example.portable_pocket_coach/files/";
    final String fileExtension = ".dat";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createWeek();
        loadWeek("1");
        //Get what day it is currently
        today = LocalDate.now().getDayOfWeek().name();
        today = today.toLowerCase();
        StringBuffer buffer = new StringBuffer(today);
        buffer.setCharAt(0,  Character.toUpperCase(today.charAt(0)));
        today = buffer.toString();
        currentDay = today;
        loadTodaysActivities(today);
    }

    String getCurrentWeekSunday(){
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        String lastSunday = (String) df.format(cal.getTime());
        Log.i("Info", "GetCurrentWeekSunday(): " + lastSunday);
        return lastSunday;
    }

    //OnCreate Functions
    void createWeek(){
        //Initialize the week object as a new week
        week = new Week();

        week.date  = getCurrentWeekSunday();
        //Allocate space for the days hashtable
        week.days = new Hashtable<String, Day>();
        //Add the seven days into the table and write the day files
        week.days.put("Sunday", new Day("Sunday", new Vector<Activity>(), false));
        writeDayFile("Sunday");

        week.days.put("Monday", new Day("Monday", new Vector<Activity>(), false));
        writeDayFile("Monday");

        week.days.put("Tuesday", new Day("Tuesday", new Vector<Activity>(), false));
        writeDayFile("Tuesday");

        week.days.put("Wednesday", new Day("Wednesday", new Vector<Activity>(), false));
        writeDayFile("Wednesday");

        week.days.put("Thursday", new Day("Thursday", new Vector<Activity>(), false));
        writeDayFile("Thursday");

        week.days.put("Friday", new Day("Friday", new Vector<Activity>(), false));
        writeDayFile("Friday");

        week.days.put("Saturday", new Day("Saturday", new Vector<Activity>(), false));
        writeDayFile("Saturday");

        Log.i("Info", week.days.get("Sunday").name);
    }

    //File Writing Functions
    void writeDay(Day day, OutputStreamWriter oStream) throws IOException {
        //Have the function update whether the day is completed or not before writing to the file
        day.checkDayComplete();
        oStream.write(day.completed.toString());
        oStream.write('\n');
        //Each new activity will be written on it's own line
        for(Activity a : day.activities){
            oStream.write(a.name + ",");
            oStream.write(a.completed.toString());
            oStream.write('\n');
        }
    }

    void writeDayFile(String day){
        try {
            //The full path name with file should look like:
            //"data/data/com.example.portable_pocket_coach/files/week1/Sunday.dat"
            File dir = new File(pathName + week.date + "/");
            //use the pathname and datafile name to locate the data file
            File dataFile = new File(pathName + week.date + "/" + day + fileExtension);
            //Check to see if the file exists
            if(!dataFile.exists()){
                Log.i("Info", "CreateWeek(): data file doesn't exist, writing to file");
                //If not then make the directory and the new file
                dir.mkdirs();
                dataFile.createNewFile();
                //Write the empty object to the file
                FileOutputStream fileOut = new FileOutputStream(pathName + week.date + "/" + day + fileExtension);
                OutputStreamWriter oStream = new OutputStreamWriter(fileOut);
                Thread write = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            writeDay(week.days.get(day), oStream);
                            oStream.close();
                            fileOut.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                write.start();
            }else{
                Log.i("Info", "CreateWeek(): data file exists, returning");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Log.i("Info", "CreateWeek(): Aborted IOException");
            e.printStackTrace();
        }
    }

    void updateDayFile(String day){
        try {
            File dir = new File(pathName + week.date + "/");
            //use the pathname and datafile name to locate the data file
            File dataFile = new File(pathName + week.date + "/" + day + fileExtension);
            //Check to see if the file exists
            if(!dataFile.exists()){
                //If not then make the directory and the new file
                dir.mkdirs();
                dataFile.createNewFile();
            }
            else{
                //Overwrite the file
                dataFile.delete();
                dataFile.createNewFile();
            }

            FileOutputStream fileOut = new FileOutputStream(pathName + week.date + "/" + day + fileExtension);
            OutputStreamWriter oStream = new OutputStreamWriter(fileOut);
            Thread write = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        writeDay(week.days.get(day), oStream);
                        oStream.close();
                        fileOut.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            write.start();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //File Loading Functions
    void readDayFile(Day day) throws IOException {
        Thread read = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(pathName + week.date + "/" + day.name + fileExtension));
                    String ss = bufferedReader.readLine();
                    day.completed = Boolean.valueOf(ss);
                    Vector<Activity> activities = new Vector<Activity>();
                    while((ss = bufferedReader.readLine()) != null){
                        String[] s = ss.split(",");
                        Activity a = new Activity(s[0], s[1], s[2], Boolean.valueOf(s[3]));
                        activities.addElement(a);
                    }
                    day.activities = activities;
                    bufferedReader.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        read.start();
    }

    void loadWeek(String weekNum){
        try{
            readDayFile(week.days.get("Sunday"));
            readDayFile(week.days.get("Monday"));
            readDayFile(week.days.get("Tuesday"));
            readDayFile(week.days.get("Wednesday"));
            readDayFile(week.days.get("Thursday"));
            readDayFile(week.days.get("Friday"));
            readDayFile(week.days.get("Saturday"));
        }catch (IOException e){
            e.printStackTrace();
        }

        Log.i("Info", "LoadWeek(): week loaded, test value: " + week.days.get("Sunday").name);
    }

    public void loadTodaysActivities(String day){
        try{
            readDayFile(week.days.get(today));
            addToggleButtonsToView(week.days.get(day), findViewById(R.id.activitiesMainLayout));
            Log.i("Info", "LoadTodaysActivities(): for " + today);
        }catch (IOException e){
            e.printStackTrace();
            Log.i("Info", "LoadTodaysActivities(): Buttons could not be loaded for " + today);
        }

    }

    //Content Switching / Button Functions
    public void switchToWeekActvity(View v){
        //used by the "this week" and "back" button to switch to the activity_week layout
        try{
            //Since activities can be updated on the activity_main layout the file for the current day has to be updated
            updateDayFile(today);
        }catch (NullPointerException e){
            Log.i("Info","switchToWeekActvity(): UpdateDayFile() failed");
        }
        setContentView(R.layout.activity_week);
        //Update the day text to be the current week's beginning date
        TextView dayText = findViewById(R.id.dayTextView);
        dayText.setText(week.date);
    }

    public void backButton(View v){
        //Might be replaced by using the switchToWeekActvity function twice
        try{
            updateDayFile(currentDay);
        }catch (NullPointerException e){
            Log.i("Info","switchToWeekActvity(): UpdateDayFile() failed");
        }
        setContentView(R.layout.activity_week);
    }

    public void switchToMainActivity(View v){
        //used by the "today" button to load the current day's activities and update the layout to main
        setContentView(R.layout.activity_main);
        currentDay = today;
        loadTodaysActivities(today);
    }

    public void switchToDayActivity(){
        //only called from the edit button functions
        setContentView(R.layout.activity_day);
        TextView dayText = findViewById(R.id.dayTextView);
        dayText.setText(currentDay);
        try{
            readDayFile(week.days.get(currentDay));
            addToggleButtonsToView(week.days.get(currentDay), findViewById(R.id.activitiesWorkoutLayout));
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void loadActivities(View v){
        //LoadActivities only works with the buttons on the week view because of the switch case
        setContentView(R.layout.activity_day);
        TextView dayText = findViewById(R.id.dayTextView);
        switch (v.getId()){
            case R.id.sundayButton:
                dayText.setText("Sunday");
                currentDay = "Sunday";
                break;
            case R.id.mondayButton:
                dayText.setText("Monday");
                currentDay = "Monday";
                break;
            case R.id.tuesdayButton:
                dayText.setText("Tuesday");
                currentDay = "Tuesday";
                break;
            case R.id.wednesdayButton:
                dayText.setText("Wednesday");
                currentDay = "Wednesday";
                break;
            case R.id.thursdayButton:
                dayText.setText("Thursday");
                currentDay = "Thursday";
                break;
            case R.id.fridayButton:
                dayText.setText("Friday");
                currentDay = "Friday";
                break;
            case R.id.saturdayButton:
                dayText.setText("Saturday");
                currentDay = "Saturday";
        }

        try{
            readDayFile(week.days.get(currentDay));
            addToggleButtonsToView(week.days.get(currentDay), findViewById(R.id.activitiesWorkoutLayout));
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void addNewActivity(View v){
        setContentView(R.layout.activity_new_activity);
    }


    public void editButtonChangeView(View v){
        setContentView(R.layout.activity_edit_day);
        addButtonsToEditView(week.days.get(currentDay), findViewById(R.id.activitiesWorkoutLayout));
        TextView dayText = findViewById(R.id.dayTextView);
        dayText.setText(currentDay);
    }

    public void submitButtonEditView(Integer currentButtonIndex, Activity a){
        week.days.get(currentDay).activities.elementAt(currentButtonIndex).name = a.name;
        updateDayFile(currentDay);
        switchToDayActivity();
    }

    public void deleteButton(Integer currentButtonIndex){
        week.days.get(currentDay).activities.removeElementAt(currentButtonIndex);
        updateDayFile(currentDay);
        switchToDayActivity();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void submitNewActivity(View v){
        Activity newActivity = new Activity();

        //Get data fields before switching back to the activity_day context
        EditText activityName = findViewById(R.id.activityNameEditText);
        try{
            newActivity.name = Integer.toString(week.days.get(currentDay).activities.size() + 1) + ". " + activityName.getText().toString();
        }catch (NullPointerException e){
            Log.i("Info", "SubmitNewActivity() activites is empty");
            newActivity.name = Integer.toString(1) + ". " + activityName.getText().toString();
        }

        //Switch the the activity_day context to add the button
        setContentView(R.layout.activity_day);
        //Configure the button properties
        ToggleButton tb = new ToggleButton(this);
        tb.setId(View.generateViewId());
        tb.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        changeToggleButtonColor(tb);
        tb.setText(newActivity.name);
        tb.setTextOff(newActivity.name);
        tb.setTextOn(newActivity.name + " Completed!");

        //Add the function the buttons will use to change color
        tb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeToggleButtonColor(tb);
            }
        });

        //Initialize the completed variable as false
        newActivity.completed = false;
        //Add the new activity to the week object for that specific day
        try{
            week.days.get(currentDay).activities.addElement(newActivity);
        }catch (NullPointerException e){
            Log.i("Info", week.days.get(currentDay).name);
            week.days.get(currentDay).addActivity(newActivity);
            e.printStackTrace();
        }
        //Add the current toggle buttons to view if there are any already in the week object
        addToggleButtonsToView(week.days.get(currentDay), findViewById(R.id.activitiesWorkoutLayout));
        //Update the day file
        updateDayFile(currentDay);
    }

    //Activity Buttons Functions
    public void editButton(View v){
        //Button is passed as a view from an onclick listener added in the addButtonsToEditView function
        Button b = (Button)v;
        //Get the index of the button from it's text
        String[] s = b.getText().toString().split("\\.");
        Log.i("Info", "editButton(): Index " + s[0]);
        //This index will determine which button is being modified
        Integer currentButtonIndex = Integer.parseInt(s[0]) - 1;
        setContentView(R.layout.activity_edit_activity);
        Activity a = week.days.get(currentDay).activities.elementAt(currentButtonIndex);
        //Update the text views to what the activity stores
        EditText activityName = findViewById(R.id.activityNameEditTextEditView);
        String[] name = a.name.split("\\.");
        activityName.setText(name[1]);

        Button submit = findViewById(R.id.submitButtonEditView);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                a.name = (currentButtonIndex + 1) + ". " + activityName.getText().toString();
                submitButtonEditView(currentButtonIndex, a);
            }
        });

        Button delete = findViewById(R.id.deleteButton);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteButton(currentButtonIndex);
            }
        });
    }

    void addButtonsToEditView(Day day, LinearLayout activitiesLayout){
        try{
            //If the result comes out to null, the catch clause will just have the function return, doing nothing
            if(day.activities != null && day.activities.size() != 0){
                Handler handler = new Handler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        for(Activity a : day.activities){
                            Button b = new Button(MainActivity.this);
                            b.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            //Add the button to the layout
                            activitiesLayout.addView(b);
                            b.setText(a.name);
                            if(a.completed){
                                b.setBackgroundColor(Color.rgb(62, 140, 62));
                            }else{
                                b.setBackgroundColor(Color.rgb(66, 64, 64));
                            }
                            //Add the function the buttons will use to change color
                            Button finalb = b;
                            b.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    editButton(v);
                                }
                            });
                        }
                        Log.i("Info", "AddToggleButtonsToView: " + day.activities.size() + " activity(s) added");
                    }
                });
            }
            Log.i("Info", "AddToggleButtonsToView(): Activities is null for " + day.name);
        }catch (NullPointerException e){
            e.printStackTrace();
            Log.i("Info", "AddToggleButtonsToView() NullPointerException: Something in Activities is null for " + day.name);
        }
    }

    public void changeToggleButtonColor(View v){
        ToggleButton tb = (ToggleButton)v;

        if(tb.isChecked()){
            tb.setBackgroundColor(Color.rgb(62, 140, 62));
            String[] s = tb.getTextOff().toString().split("\\.");
            Log.i("Info", "ChangeToggleButtonColor(): Index " + s[0]);
            week.days.get(currentDay).activities.elementAt(Integer.parseInt(s[0]) - 1).completed = true;
        }else{
            tb.setBackgroundColor(Color.rgb(66, 64, 64));
            try{
                String[] s = tb.getTextOff().toString().split("\\.");
                week.days.get(currentDay).activities.elementAt(Integer.parseInt(s[0]) - 1).completed = false;
            }catch (ArrayIndexOutOfBoundsException e){
                Log.i("Info", "Index out of range");
            }catch (NumberFormatException e){
                Log.i("Info", "Number format incorrect");
            }
        }

    }

    void addToggleButtonsToView(Day day, LinearLayout activitiesLayout){
        try{
            //If the result comes out to null, the catch clause will just have the function return, doing nothing
            if(day.activities != null && day.activities.size() != 0){
                Handler handler = new Handler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        for(Activity a : day.activities){
                            ToggleButton tb = new ToggleButton(MainActivity.this);
                            tb.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            //Add the button to the layout
                            activitiesLayout.addView(tb);
                            tb.setText(a.name);
                            tb.setTextOff(a.name);
                            tb.setTextOn(a.name + " Completed!");
                            tb.setChecked(a.completed);
                            if(a.completed){
                                tb.setBackgroundColor(Color.rgb(62, 140, 62));
                            }else{
                                tb.setBackgroundColor(Color.rgb(66, 64, 64));
                            }
                            //Add the function the buttons will use to change color
                            ToggleButton finalTb = tb;
                            tb.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    changeToggleButtonColor(finalTb);
                                }
                            });
                        }
                        Log.i("Info", "AddToggleButtonsToView: " + day.activities.size() + " activity(s) added");
                    }
                });
            }
            Log.i("Info", "AddToggleButtonsToView(): Activities is null for " + day.name);
        }catch (NullPointerException e){
            e.printStackTrace();
            Log.i("Info", "AddToggleButtonsToView() NullPointerException: Something in Activities is null for " + day.name);
        }
    }
}