package com.example.joystickUltron;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.zerokol.views.joystickView.JoystickView;
import com.zerokol.views.joystickView.JoystickViewRight;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {
    private static boolean temaDark = false;
    private static boolean firstLoop = true;
    ImageButton btnCredits;
    ImageButton btnConexao;
    ImageButton btnTheme;

    SwipeButton swipeButton;

    JoystickView leftJoystick;
    JoystickViewRight rightJoystick;

    ConnectedThread connectedThread;

    private static final int SOLICITA_ATIVACAO = 1;
    private static final int SOLICITA_CONEXAO = 2;

    public static String ultimoComandoEsq = "";
    public static String ultimoComandoDir = "";


    public static boolean conexao = false;
    private static String MAC = null;
    UUID MEU_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    BluetoothAdapter meuBluetoothAdapter = null;
    BluetoothDevice meuDevice = null;
    BluetoothSocket meuSocket = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeButton = (SwipeButton) findViewById((R.id.swipe_btn));
        if(firstLoop){
            AppCompatDelegate.setDefaultNightMode(temaDark == false?
                    AppCompatDelegate.MODE_NIGHT_NO :
                    AppCompatDelegate.MODE_NIGHT_YES);
            firstLoop = false;
        }


        btnTheme = (ImageButton) findViewById(R.id.btnTheme);
        btnCredits = (ImageButton) findViewById(R.id.btnCredits);

        btnConexao = (ImageButton) findViewById(R.id.btnConexao);

        leftJoystick = (JoystickView) findViewById(R.id.leftJoystick);
        rightJoystick = (JoystickViewRight) findViewById(R.id.rightJoystick);


        leftJoystick.corBtn = temaDark? "#222222" : "#626262";
        leftJoystick.corBtnBorder = temaDark? "#166749": "#81b7ff";

        rightJoystick.corBtn = temaDark? "#222222" : "#626262";
        rightJoystick.corBtnBorder = temaDark? "#166749": "#81b7ff";

        btnCredits.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    btnCredits.setScaleX(1.1f);
                    btnCredits.setScaleY(1.1f);
                }
                if(event.getAction() == MotionEvent.ACTION_UP){
                    btnCredits.setScaleX(1f);
                    btnCredits.setScaleY(1f);
                }
                return false;
            }
        });
        btnTheme.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    btnTheme.setScaleX(1.1f);
                    btnTheme.setScaleY(1.1f);
                }
                if(event.getAction() == MotionEvent.ACTION_UP){
                    btnTheme.setScaleX(1f);
                    btnTheme.setScaleY(1f);
                }
                return false;
            }
        });
        btnConexao.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    btnConexao.setScaleX(1.1f);
                    btnConexao.setScaleY(1.1f);
                }
                if(event.getAction() == MotionEvent.ACTION_UP){
                    btnConexao.setScaleX(1f);
                    btnConexao.setScaleY(1f);
                }
                return false;
            }
        });

        btnTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!conexao) {
                    if (temaDark) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    }
                    leftJoystick.postInvalidate();
                    SwipeButton.trancado = true;
                    temaDark = !temaDark;
                }
            }
        });

        rightJoystick.setOnJoystickMoveListener(new JoystickViewRight.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int angle, int power, int direction) {
                if(conexao && !SwipeButton.trancado) {
                    if ((angle == 0)) {
                        if ((power > 40) && ultimoComandoDir != "f+") {
                            connectedThread.enviar("f+");
                            ultimoComandoDir = "f+";
                        }
                    }
                    if ((angle == 180)) {
                        if ((power > 40) && ultimoComandoDir != "f-") {
                            connectedThread.enviar("f-");
                            ultimoComandoDir = "f-";
                        }
                    }
                    if((power <= 40) && (ultimoComandoDir != "p")){
                        connectedThread.enviar("p");
                        ultimoComandoDir = "p";
                    }
                }
            }
        }, rightJoystick.DEFAULT_LOOP_INTERVAL);

        leftJoystick.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int angle, int power, int direction) {

                if(conexao && !SwipeButton.trancado) {
                    if ((angle < -43) && (angle > -137) && (power > 40) && ultimoComandoEsq != "r-") {
                        connectedThread.enviar("r-");
                        ultimoComandoEsq = "r-";
                    }
                    if ( (angle < 137) && (angle > 43)&& (power > 40) && ultimoComandoEsq != "r+") {
                        connectedThread.enviar("r+");
                        ultimoComandoEsq = "r+";
                    }
                    if ((angle > -50 && angle < 50)) {
                        if ((power > 40) && (power < 65) && ultimoComandoEsq != "a+") {
                            connectedThread.enviar("a+");
                            ultimoComandoEsq = "a+";
                        }
                        if (power >= 65 && ultimoComandoEsq != "A+") {
                            connectedThread.enviar("A+");
                            ultimoComandoEsq = "A+";
                        }
                    }
                    if ((angle < -130 || angle > 130)) {
                        if ((power > 40) && (power < 65) && ultimoComandoEsq != "a-") {
                            connectedThread.enviar("a-");
                            ultimoComandoEsq = "a-";
                        }
                        if (power >= 65 && ultimoComandoEsq != "A-") {
                            connectedThread.enviar("A-");
                            ultimoComandoEsq = "A-";
                        }
                    }
                    if((power <= 40) && (ultimoComandoEsq != "e")){
                        connectedThread.enviar("e");
                        ultimoComandoEsq = "e";
                    }
                }
            }
        }, leftJoystick.DEFAULT_LOOP_INTERVAL);


        btnCredits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCredits();
            }
        });
        meuBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(meuBluetoothAdapter == null){
            Toast.makeText(getApplicationContext(), "Seu dispositivo não possui Bluetooth.", Toast.LENGTH_LONG).show();
        } else if(!meuBluetoothAdapter.isEnabled()){
            Intent ativaBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(ativaBluetooth, SOLICITA_ATIVACAO);
        }

        btnConexao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(conexao){
                    //desconectar
                    try{
                        meuSocket.close();
                        conexao = false;
                        btnConexao.setImageResource(R.drawable.ic_bluetooth_disabled);
                        btnConexao.setBackgroundResource(R.drawable.rounded_btn_bt_off);
                        btnTheme.setAlpha(1f);
                        swipeButton.setAlpha(.3f);
                        btnConexao.setColorFilter(Color.parseColor("#FF6A6A"));
                        Toast.makeText(getApplicationContext(), "Desconectado", Toast.LENGTH_LONG).show();
                    } catch (IOException erro){
                        Toast.makeText(getApplicationContext(), "Ocorreu um erro." + erro, Toast.LENGTH_LONG).show();
                    }

                }else{
                    //conectar
                    Intent abreLista = new Intent(MainActivity.this, ListaDispositivos.class);
                    startActivityForResult(abreLista, SOLICITA_CONEXAO);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case SOLICITA_ATIVACAO:
                if (resultCode == Activity.RESULT_OK){
                    Toast.makeText(getApplicationContext(), "O bluetooth foi ativado.", Toast.LENGTH_LONG).show();

                }else{
                    Toast.makeText(getApplicationContext(), "Bluetooth não ativado. O app será encerrado.", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            case SOLICITA_CONEXAO:
                if(resultCode == Activity.RESULT_OK){
                    MAC = data.getExtras().getString(ListaDispositivos.ENDERECO_MAC);
                    meuDevice = meuBluetoothAdapter.getRemoteDevice(MAC);
                    try {
                        meuSocket = meuDevice.createRfcommSocketToServiceRecord(MEU_UUID);
                        meuSocket.connect();
                        conexao = true;
                        btnConexao.setImageResource(R.drawable.ic_bluetooth_connected);
                        btnConexao.setBackgroundResource(R.drawable.rounded_btn_bt_on);
                        btnConexao.setColorFilter(Color.parseColor("#6FFF6A"));
                        btnTheme.setAlpha(.3f);
                        swipeButton.setAlpha(1f);
                        connectedThread = new ConnectedThread(meuSocket);
                        connectedThread.start();
                        Toast.makeText(getApplicationContext(), "Conectado com: " + MAC, Toast.LENGTH_LONG).show();

                    }catch (IOException erro){
                        conexao = false;
                        Toast.makeText(getApplicationContext(), "Erro ocorrido. Detalhes: " + erro, Toast.LENGTH_LONG).show();
                    }

                }else{
                    Toast.makeText(getApplicationContext(), "Falha ao obter o Endereço MAC", Toast.LENGTH_LONG).show();
                }
        }
    }

    public void openCredits(){
        Intent intent = new Intent(this, Credits.class);
        startActivity(intent);
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) { }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        // Call this from the main activity to send data to the remote device.
        public void enviar(String dadosEnviar) {
            byte[] msgBuffer = dadosEnviar.getBytes();
            try {
                mmOutStream.write(msgBuffer);

            } catch (IOException e) { }
        }
    }
}