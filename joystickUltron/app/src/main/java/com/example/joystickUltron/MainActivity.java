package com.example.joystickUltron;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {
    ImageView imgIcDrone;
    ImageView imgIcGamepad;
    ImageView imgBatDrone;
    ImageView imgBatGamepad;

    ImageView imgPingGamepad;

    TextView txtPing;

    private static boolean temaDark = false;
    private static boolean firstLoop = true;
    ImageButton btnCredits;
    ImageButton btnConexao;
    ImageButton btnTheme;

    public int[] comandos = {0,0,0,0};
    public int[] comandosAnteriores = {1,1,1,1};

    public static boolean pressionado = false;
    public static final String CMD_DESLIGA = "100";
    public static final String CMD_LIGA = "101";
    private static final String CMD_CIMA_1 = "110";
    private static final String CMD_CIMA_2 = "111";
    private static final String CMD_BAIXO_1 = "120";
    private static final String CMD_BAIXO_2 = "121";
    private static final String CMD_ROT_ESQ = "130";
    private static final String CMD_ROT_DIR = "140";
    private static final String CMD_FRENTE = "150";
    private static final String CMD_TRAS = "160";
    private static final String CMD_ESTAB = "198";
    private static final String CMD_PARADO = "199";

    SwipeButton swipeButton;

    JoystickView leftJoystick;
    JoystickViewRight rightJoystick;

    public static ConnectedThread connectedThread;

    private static final int SOLICITA_ATIVACAO = 1;
    private static final int SOLICITA_CONEXAO = 2;
    private static final int MESSAGE_READ = 3;

    Handler handlerCmdEnvio;
    Handler handlerBt;
    StringBuilder dadosBluetooth = new StringBuilder();


    public static String ultimoComandoEsq = "";
    public static String ultimoComandoDir = "";


    public static boolean conexao = false;
    private static String MAC = null;
    UUID MEU_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    BluetoothAdapter meuBluetoothAdapter = null;
    BluetoothDevice meuDevice = null;
    BluetoothSocket meuSocket = null;



    @SuppressLint("HandlerLeak")
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

        imgIcDrone = (ImageView) findViewById((R.id.imgIcDrone));
        imgIcGamepad = (ImageView) findViewById((R.id.imgIcGamepad));
        imgBatDrone = (ImageView) findViewById((R.id.imgBateriaDrone));
        imgBatGamepad = (ImageView) findViewById((R.id.imgBateriaGamepad));
        imgPingGamepad = (ImageView) findViewById((R.id.imgPingGamepad));

        txtPing = (TextView) findViewById((R.id.txtPing));
        btnTheme = (ImageButton) findViewById(R.id.btnTheme);
        btnCredits = (ImageButton) findViewById(R.id.btnCredits);

        btnConexao = (ImageButton) findViewById(R.id.btnConexao);

        leftJoystick = (JoystickView) findViewById(R.id.leftJoystick);
        rightJoystick = (JoystickViewRight) findViewById(R.id.rightJoystick);


        leftJoystick.corBtn = temaDark? "#191919" : "#626262";
        leftJoystick.corBtnBorder = temaDark? "#5727A6": "#81b7ff";

        rightJoystick.corBtn = temaDark? "#191919" : "#626262";
        rightJoystick.corBtnBorder = temaDark? "#5727A6": "#81b7ff";

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
            public void onValueChanged(int xPower, int yPower) {
                comandos[2] = yPower;

            }
        }, rightJoystick.DEFAULT_LOOP_INTERVAL);

        leftJoystick.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int xPower, int yPower) {
                comandos[0] = yPower;
                comandos[1] = xPower;

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
                        connectedThread.enviar(CMD_DESLIGA);
                        meuSocket.close();
                        conexao = false;
                        btnConexao.setImageResource(R.drawable.ic_bluetooth_disabled);
                        btnConexao.setBackgroundResource(R.drawable.rounded_btn_bt_off);
                        btnTheme.setAlpha(1f);
                        swipeButton.setAlpha(.3f);
                        btnConexao.setColorFilter(Color.parseColor("#FF6A6A"));
                        txtPing.setText("");
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

        handlerBt = new Handler(){
            @SuppressLint("HandlerLeak")
            @Override
            public void handleMessage(@NonNull Message msg) {
                if(msg.what == MESSAGE_READ){
                    String recebidos = (String)msg.obj;
                    dadosBluetooth.append(recebidos);
                    int fimInformacao = dadosBluetooth.indexOf("]");

                    if(fimInformacao > 0){
                        String dadosCompletos = dadosBluetooth.substring(0, fimInformacao);
                        int tamInformacao = dadosCompletos.length();
                        Log.d("dados completos", dadosCompletos);
                        if(dadosBluetooth.charAt(0) == '['){ // Comandos que começam com Chaves são pedidos de Ping
                            String dadosFinais = dadosBluetooth.substring(1, tamInformacao);
                            Log.d("Recebidos", dadosFinais);
                        }


                        dadosBluetooth.delete(0, dadosBluetooth.length());
                    }
                }
            }
        };

        handlerCmdEnvio = new Handler();
        handlerCmdEnvio.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(conexao && pressionado) {
                    if(comandosAnteriores[0] != comandos[0] ||
                            comandosAnteriores[1] != comandos[1] ||
                            comandosAnteriores[2] != comandos[2]) {
                        enviaMensagem();
                    }
                }
                handlerCmdEnvio.postDelayed(this, 30);
            }
        }, 0);
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
                        comandos[3] = 1;
                        enviaMensagem();
                        Toast.makeText(getApplicationContext(), "Conexão com o gamepad feita com sucesso. Destrave o botão de segurança para sincronizar com o drone.", Toast.LENGTH_LONG).show();
                    }catch (IOException erro){
                        conexao = false;
                        Toast.makeText(getApplicationContext(), "Não foi possível conectar, verifique se o gamepad está adequadamente ligado ou tente novamente.", Toast.LENGTH_LONG).show();
                    }

                }else{
                    Toast.makeText(getApplicationContext(), "Falha ao obter o Endereço MAC", Toast.LENGTH_LONG).show();
                }
        }
    }

    public void enviaMensagem(){
        String msgEnviada = "{";
        for (int comando : comandos) {
            msgEnviada += comando + ";";
        }
        msgEnviada += "}";
        Log.d("Valor atual:", msgEnviada);
        connectedThread.enviar(msgEnviada);
        comandosAnteriores[0] = comandos[0];
        comandosAnteriores[1] = comandos[1];
        comandosAnteriores[2] = comandos[2];
        comandosAnteriores[3] = comandos[3];
        comandos[3] = 0;
    }

    public void changeIcon(ImageView iconBat, int status){
        switch(status){
            case 0:
                iconBat.setColorFilter(R.attr.colorSecondary);
                iconBat.setImageResource(R.drawable.ic_bateria0);
                break;
            case 1:
                iconBat.setColorFilter(Color.RED);
                iconBat.setImageResource(R.drawable.ic_bateria1);
                break;
            case 2:
                iconBat.setColorFilter(Color.YELLOW);
                iconBat.setImageResource(R.drawable.ic_bateria2);
                break;
            case 3:
                iconBat.setColorFilter(Color.GREEN);
                iconBat.setImageResource(R.drawable.ic_bateria3);
                break;
        }
    }

    public void openCredits(){
        Intent intent = new Intent(this, Credits.class);
        startActivity(intent);
    }

    public class ConnectedThread extends Thread {
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
        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);

                    String dadosBt = new String(mmBuffer, 0, numBytes);

                    // Send the obtained bytes to the UI activity.
                    handlerBt.obtainMessage(MESSAGE_READ, numBytes, -1, dadosBt).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
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