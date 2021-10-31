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
import java.util.Arrays;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {
    ImageView imgBatDrone;
    ImageView imgBatGamepad;

    ImageView imgPingGamepad;
    ImageView imgPingDrone;


    TextView txtPingGamepad;
    TextView txtPingDrone;

    private static boolean temaDark = false;
    private static boolean firstLoop = true;
    ImageButton btnCredits;
    ImageButton btnConexao;
    ImageButton btnTheme;

    public int[] comandos = {0,0,0,0};
    public int[] comandosAnteriores = {1,1,1,1};

    public static int contagem = 0;
    public static long ping;
    public static boolean pressionado = false;
    public static boolean permissaoDeEnvio = false;
    public static boolean destrancado = false;
    public static boolean 

    SwipeButton swipeButton;

    JoystickView leftJoystick;
    JoystickViewRight rightJoystick;

    public static ConnectedThread connectedThread;

    private static final int SOLICITA_ATIVACAO = 1;
    private static final int SOLICITA_CONEXAO = 2;
    private static final int MESSAGE_READ = 3;

    Handler handlerCmdEnvio;
    Handler handlerBt;
    Handler handlerSolicitaPing;
    Runnable runSolicitaPing;
    Runnable runCmdEnvio;


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


        if(firstLoop){
            AppCompatDelegate.setDefaultNightMode(temaDark == false?
                    AppCompatDelegate.MODE_NIGHT_NO :
                    AppCompatDelegate.MODE_NIGHT_YES);
            firstLoop = false;
        }

        imgBatDrone = (ImageView) findViewById((R.id.imgBateriaDrone));
        imgBatGamepad = (ImageView) findViewById((R.id.imgBateriaGamepad));
        imgPingGamepad = (ImageView) findViewById((R.id.imgPingGamepad));
        imgPingDrone = (ImageView) findViewById((R.id.imgPingDrone));

        txtPingGamepad = (TextView) findViewById((R.id.txtPingGamepad));
        txtPingDrone = (TextView) findViewById((R.id.txtPingDrone));
        btnTheme = (ImageButton) findViewById(R.id.btnTheme);
        btnCredits = (ImageButton) findViewById(R.id.btnCredits);
        btnConexao = (ImageButton) findViewById(R.id.btnConexao);
        swipeButton = (SwipeButton) findViewById((R.id.swipe_btn));

        leftJoystick = (JoystickView) findViewById(R.id.leftJoystick);
        rightJoystick = (JoystickViewRight) findViewById(R.id.rightJoystick);


        leftJoystick.corBtn = temaDark? "#191919" : "#626262";
        leftJoystick.corBtnBorder = temaDark? "#5727A6": "#81b7ff";

        rightJoystick.corBtn = temaDark? "#191919" : "#626262";
        rightJoystick.corBtnBorder = temaDark? "#5727A6": "#81b7ff";


        rightJoystick.setOnJoystickMoveListener(new JoystickViewRight.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int xPower, int yPower) {
                if(destrancado) comandos[2] = yPower;

            }
        }, rightJoystick.DEFAULT_LOOP_INTERVAL);
        leftJoystick.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int xPower, int yPower) {
                if(destrancado) comandos[0] = yPower;
                if(destrancado) comandos[1] = xPower;

            }
        }, leftJoystick.DEFAULT_LOOP_INTERVAL);

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



        handlerCmdEnvio = new Handler();
        handlerSolicitaPing = new Handler();


        handlerBt = new Handler(){
            @SuppressLint("HandlerLeak")
            @Override
            public void handleMessage(@NonNull Message msg) {
                if(msg.what == MESSAGE_READ){
                    String recebidos = (String)msg.obj;
                    dadosBluetooth.append(recebidos);
                    int fimInformacao = dadosBluetooth.indexOf("]");

                    if(fimInformacao > 0){
                        String[] arrInformacao = new String[4];
                        try {
                            setTxtPing((System.currentTimeMillis() - ping - 40), txtPingGamepad, imgPingGamepad);
                            String dadosCompletos = dadosBluetooth.substring(0, fimInformacao);
                            int indiceInicio = dadosCompletos.indexOf('[');
                            int tamInformacao = dadosCompletos.length();
                            arrInformacao = String.valueOf(dadosBluetooth).
                                    substring(indiceInicio + 1, tamInformacao - 1).
                                    split(";");
                            int[] msgRec = new int[4];
                            for (int i = 0; i < 4; i++) {
                                msgRec[i] = Integer.parseInt(arrInformacao[i]);
                            }
                            contagem = 0;

                            setTxtPing(msgRec[0], txtPingDrone, imgPingDrone);
                            setBateriaIcone(msgRec[1], imgBatGamepad);
                            setBateriaIcone(msgRec[2], imgBatDrone);


                        }catch (Exception ex) {}

                        finally {
                            Log.d("Mensagem recebida", Arrays.toString(arrInformacao));
                            dadosBluetooth.delete(0, dadosBluetooth.length());
                        }
                    }
                }
            }
        };

    }

    @Override
    protected void onResume() {
        handlerCmdEnvio.postDelayed(runCmdEnvio = new Runnable() {
            @Override
            public void run() {
                if(conexao && (pressionado || permissaoDeEnvio)) {
                    if((comandosAnteriores[0] != comandos[0] ||
                            comandosAnteriores[1] != comandos[1] ||
                            comandosAnteriores[2] != comandos[2] ||
                            comandosAnteriores[3] != comandos[3]) || permissaoDeEnvio) {
                        connectedThread.enviarMensagem();
                    }
                    permissaoDeEnvio = false;
                }
                handlerCmdEnvio.postDelayed(this, 40);
            }
        }, 0);

        handlerSolicitaPing.postDelayed(runSolicitaPing = new Runnable() {
            @Override
            public void run() {
                if(conexao) {
                    contagem++;
                    if (contagem >= 2) {
                        //setTxtPing(-1);
                    }
                }
                comandos[3] = 2;
                permissaoDeEnvio = true;
                ping = System.currentTimeMillis();
                handlerSolicitaPing.postDelayed(this, 5000);
            }
        }, 0);
        super.onResume();
    }

    @Override
    protected void onPause() {
        handlerSolicitaPing.removeCallbacks(runSolicitaPing);
        handlerCmdEnvio.removeCallbacks(runCmdEnvio);

        super.onPause();
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
                        connectedThread.enviarMensagem();
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

    public void setTxtPing(long valorPing, TextView txtPing, ImageView imgPing) {
        txtPing.setText(String.valueOf(valorPing) + "ms");
        if(valorPing > 600){
            txtPing.setTextColor(Color.RED);
            imgPing.setColorFilter(Color.RED);
        }else if(valorPing <= 600 && valorPing > 240){
            txtPing.setTextColor(Color.YELLOW);
            imgPing.setColorFilter(Color.YELLOW);

        }else if(valorPing >= 0){
            txtPing.setTextColor(Color.GREEN);
            imgPing.setColorFilter(Color.GREEN);
        }else {
            txtPing.setText("  !!!");
            txtPing.setTextColor(Color.RED);
            imgPing.setColorFilter(Color.RED);
        }
    }

    public void setBateriaIcone(int status, ImageView iconBat){
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
        public void enviarMensagem() {
            String msgEnviada = "{";
            for (int comando : comandos) {
                msgEnviada += comando + ";";
            }
            msgEnviada += "}";
            byte[] msgBuffer = msgEnviada.getBytes();
            try {
                mmOutStream.write(msgBuffer);
                Log.d("Mensagem enviada", msgEnviada);
                comandosAnteriores[0] = comandos[0];
                comandosAnteriores[1] = comandos[1];
                comandosAnteriores[2] = comandos[2];
                comandosAnteriores[3] = comandos[3];
                comandos[3] = 0;

            } catch (IOException e) { }
        }
    }
}