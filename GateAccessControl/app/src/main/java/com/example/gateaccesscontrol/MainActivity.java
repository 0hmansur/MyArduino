package com.example.gateaccesscontrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;

    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonSearch;
    private Button buttonConnect;
    private TextView textViewStatus;
    private ProgressBar progressBar;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice gateDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSearch = findViewById(R.id.buttonSearch);
        buttonConnect = findViewById(R.id.buttonConnect);
        textViewStatus = findViewById(R.id.textViewStatus);
        progressBar = findViewById(R.id.progressBar);

        progressBar.setVisibility(View.GONE);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on this device",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        buttonSearch.setOnClickListener(v -> searchForGateDevice());
        buttonConnect.setOnClickListener(v -> connectToGateDevice());
    }

    private void searchForGateDevice() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        textViewStatus.setText("Searching for gate device...");
        progressBar.setVisibility(View.VISIBLE);

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName() != null && device.getName().equals("ESP32_Gate_Access")) {
                    gateDevice = device;
                    textViewStatus.setText("Gate device found: " + device.getName());
                    progressBar.setVisibility(View.GONE);
                    return;
                }
            }
        }

        textViewStatus.setText("Please pair your device with ESP32_Gate_Access in Bluetooth settings");
        progressBar.setVisibility(View.GONE);
    }

    private void connectToGateDevice() {
        if (gateDevice == null) {
            Toast.makeText(this, "Please search for gate device first",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        textViewStatus.setText("Connecting...");

        Intent intent = new Intent(MainActivity.this, ControlActivity.class);
        intent.putExtra("DEVICE_ADDRESS", gateDevice.getAddress());
        intent.putExtra("USERNAME", username);
        intent.putExtra("PASSWORD", password);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                searchForGateDevice();
            } else {
                Toast.makeText(this, "Bluetooth is required for this app",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}