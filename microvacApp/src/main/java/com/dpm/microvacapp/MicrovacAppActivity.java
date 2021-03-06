package com.dpm.microvacapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


public class MicrovacAppActivity extends Activity implements SensorEventListener{

    private final String LOG_TAG = MicrovacAppActivity.class.getSimpleName();

    private final static int DEFAULT_PORT = 333;

    private final static String KEY_MOTION_STATUS = "motion_status";
    private final static String KEY_ADDRESS = "address";

    private final static int MOTION_STATUS_STOP = 0;
    private final static int MOTION_STATUS_FORWARDS = 1;
    private final static int MOTION_STATUS_BACKWARDS = 2;
    private final static int MOTION_STATUS_LEFT = 3;
    private final static int MOTION_STATUS_RIGHT = 4;

    private int _motionStatus = MOTION_STATUS_STOP;

    private RobotCommander _robotCommander;
    private SensorManager _sensorManager;
    private Sensor _sensor;

    private final static double TRIGGER_ANGLE = Math.toRadians(45);
    private final static double STOP_ANGLE = Math.toRadians(30);

    private View[] _driveButtons;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Log.d(LOG_TAG, "onCreate()");

        _sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        _sensor = _sensorManager != null
                ? _sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
                : null;
        if(_sensor == null){
            Log.i(LOG_TAG, "No sensor found.");
            Toast.makeText(this, "No sensor", Toast.LENGTH_SHORT).show();

            CheckBox checkBox = (CheckBox)findViewById(R.id.gestureDrivenCheckBox);
            checkBox.setVisibility(View.INVISIBLE);
            checkBox.setChecked(false);
        }

        _robotCommander = new RobotCommander();

        TextView ipEditText = (TextView)findViewById(R.id.ipEditText);
        if(savedInstanceState != null) {
            _motionStatus = savedInstanceState.getInt(KEY_MOTION_STATUS, MOTION_STATUS_STOP);
            ipEditText.setText(savedInstanceState.getCharSequence(KEY_ADDRESS));
        }else {
            ipEditText.setText(getPreferences(MODE_PRIVATE).getString(KEY_ADDRESS, ""));
        }

        _driveButtons = new View[]{
                findViewById(R.id.go_forwards_button), 
                findViewById(R.id.go_backwards_button),
                findViewById(R.id.left_button),
                findViewById(R.id.right_button),
                findViewById(R.id.stop_button)
        };
    }

    private void _hideDriveButtons(){

        for (View button: _driveButtons) {
            button.setVisibility(View.INVISIBLE);
        }
    }

    private void _showDriveButtons(){

        for (View button: _driveButtons) {
            button.setVisibility(View.VISIBLE);
        }
    }

    private void _startGestureDriverIfExists(){

        if(_sensor != null) {
            _hideDriveButtons();
            _sensorManager.registerListener(this, _sensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    private void _stopGestureDriverIfExists(){

        if(_sensor != null){
            _sensorManager.unregisterListener(this, _sensor);
        }

        _showDriveButtons();
    }

    private void _tryConnect(){

        String address = ((TextView)findViewById(R.id.ipEditText)).getText().toString().trim();
        getPreferences(MODE_PRIVATE).edit().putString(KEY_ADDRESS, address).apply();
        ToggleButton toggleButton = (ToggleButton)findViewById(R.id.connect_toggle);
        if(!address.isEmpty() && toggleButton.isChecked()){
            _robotCommander.connect(address, DEFAULT_PORT);
            if(((CheckBox)findViewById(R.id.gestureDrivenCheckBox)).isChecked()) {
                _startGestureDriverIfExists();
            }
        }else {
            toggleButton.setChecked(false);
        }
    }

    private void _disconnect(){

        _stopGestureDriverIfExists();
        _robotCommander.close();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(LOG_TAG, "onSaveInstanceState()");
        if(outState != null) {
            outState.putInt(KEY_MOTION_STATUS, _motionStatus);
            outState.putCharSequence(KEY_ADDRESS, ((TextView)findViewById(R.id.ipEditText))
                    .getText());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(LOG_TAG, "onRestoreInstanceState()");
        if(savedInstanceState != null) {
            _motionStatus = savedInstanceState.getInt(KEY_MOTION_STATUS, MOTION_STATUS_STOP);
            ((TextView)findViewById(R.id.ipEditText))
                    .setText(savedInstanceState.getCharSequence(KEY_ADDRESS));
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.d(LOG_TAG, "onResume()");
        _tryConnect();
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.d(LOG_TAG, "onPause()");
        _disconnect();
    }

    @Override
    protected void onDestroy(){
        Log.d(LOG_TAG, "onDestroy()");
        super.onDestroy();
    }

    private void _sendStop(){
        if(_motionStatus != MOTION_STATUS_STOP)
        {
            _robotCommander.sendStop();
            _motionStatus = MOTION_STATUS_STOP;
        }
    }

    private void _sendForwards(){
        if(_motionStatus != MOTION_STATUS_FORWARDS)
        {
            _robotCommander.sendForwards();
            _motionStatus = MOTION_STATUS_FORWARDS;
        }
    }

    private void _sendBackwards(){
        if(_motionStatus != MOTION_STATUS_BACKWARDS)
        {
            _robotCommander.sendBackwards();
            _motionStatus = MOTION_STATUS_BACKWARDS;
        }
    }

    private void _sendTurnRight(){
        if(_motionStatus != MOTION_STATUS_RIGHT)
        {
            _robotCommander.sendTurnRight();
            _motionStatus = MOTION_STATUS_RIGHT;
        }
    }

    private void _sendTurnLeft(){
        if(_motionStatus != MOTION_STATUS_LEFT) {
            _robotCommander.sendTurnLeft();
            _motionStatus = MOTION_STATUS_LEFT;
        }
    }

    public void onForwardsButtonClick(View view)
    {
        _sendForwards();
    }

    public void onBackwardsButtonClick(View view)
    {
        _sendBackwards();
    }

    public void onStopButtonClick(View view)
    {
        _sendStop();
    }

    public void onRightButtonClick(View view)
    {
        _sendTurnRight();
    }

    public void onLeftButtonClick(View view)
    {
        _sendTurnLeft();
    }

    public void onConnectToggleClick(View view)
    {
        ToggleButton button = (ToggleButton) view;
        if(button.isChecked())
        {
            _tryConnect();
            //TODO: In case of the connection is not possible, the toggle button must be shown as off.
            findViewById(R.id.ipEditText).setEnabled(false);

        }else
        {
            Log.d(LOG_TAG, "Closing connection.");
            _disconnect();
            findViewById(R.id.ipEditText).setEnabled(true);
        }
    }

    public void onExpression0ButtonClick(View view){
        _robotCommander.sendExpression(0);
    }

    public void onExpression1ButtonClick(View view){
        _robotCommander.sendExpression(1);
    }

    public void onExpression2ButtonClick(View view){
        _robotCommander.sendExpression(2);
    }

    public void onExpression3ButtonClick(View view){
        _robotCommander.sendExpression(3);
    }

    public void onGestureDrivenCheckBoxClick(View view){

        if(((CheckBox)view).isChecked()) {
            _startGestureDriverIfExists();
        }else
        {
            _stopGestureDriverIfExists();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent)
    {
        double[] angles = new double[2];

        angles[0] = Math.atan2(sensorEvent.values[1], sensorEvent.values[2]);
        angles[1] = Math.atan2(-sensorEvent.values[0],
                Math.sqrt(sensorEvent.values[1] * sensorEvent.values[1]
                        + sensorEvent.values[2] * sensorEvent.values[2]));

        if(angles[0] > TRIGGER_ANGLE){
            _sendBackwards();
        }else if(angles[0] < -TRIGGER_ANGLE){
            _sendForwards();
        }else if(angles[1] > TRIGGER_ANGLE){
            _sendTurnRight();
        }else if(angles[1] < -TRIGGER_ANGLE){
            Log.d(LOG_TAG, "TLE");
            _sendTurnLeft();
        }else if(Math.abs(angles[0]) < STOP_ANGLE && Math.abs(angles[1]) < STOP_ANGLE){
            _sendStop();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i)
    {
        //Nothing to do
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        if (item.getItemId() == R.id.main_menu_about) {
            startActivity(new Intent(this, AboutActivity.class));
        }else
        {
                return super.onOptionsItemSelected(item);
        }

        return true;
    }
}