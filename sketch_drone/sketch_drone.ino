//LIBS
#include <RF24.h>
#include <Wire.h>

//VARIÁVEIS
bool ligado = false;
int comandoRec;
int ultimoComandoRec;
unsigned long msAtual; // obtém o milissegundo atual em cada loop.
int AcX,AcY,AcZ,Tmp,GyX,GyY,GyZ; //Variaveis para armazenar valores dos sensores

//CÓDIGO DE COMANDOS
#define CMD_DESLIGA "100"
#define CMD_LIGA "101"
#define CMD_CIMA_1 "110"
#define CMD_CIMA_2 "111"
#define CMD_BAIXO_1 "120"
#define CMD_BAIXO_2 "121"
#define CMD_ROT_ESQ "130"
#define CMD_ROT_DIR "140"
#define CMD_FRENTE "150"
#define CMD_TRAS "160"
#define CMD_ESTAB "198"
#define CMD_PARADO "199"
#define CMD_LIGA_DRONE "500"

//CONSTANTES
#define intervaloBateria 5000 // Tempo em ms para mensurar a % da bateria atual do drone
const uint64_t pipes[2] = { 0xF0F0F0F0E1LL, 0xF0F0F0F0D2LL };
//const int MPU=0x68; //Endereco I2C do MPU6050

//OBJETOS
RF24 radio(7, 8);

void setup() {
  Serial.begin(9600);
  Serial.println("Drone iniciado.");

  //MPU
//  Wire.begin();
//  Wire.beginTransmission(MPU);
//  Wire.write(0x6B); 
//  //Inicializa o MPU-6050
//  Wire.write(0); 
//  Wire.endTransmission(true);

  //Radio
  radio.begin();
  radio.setChannel(100);
  radio.openWritingPipe(pipes[0]);
  radio.openReadingPipe(1, pipes[1]);
  radio.startListening();
}

void loop() {
  msAtual = millis();
//  
////  updateAccelValues();
//
  delay(50);
  radioListener();
//delay(3000);
//  transmit();
}

bool radioListener() {
  if (radio.available()) {
    radio.read(&comandoRec, sizeof(int));
    radioHandler(String(comandoRec));
    
  }
  return false;
}

void radioHandler(String cmd) {
  Serial.println(cmd);

  if (cmd.indexOf(CMD_LIGA) >= 0) {
    Serial.println("RECEBI O COMANDO: LIGAR");
  }
  if (cmd.indexOf(CMD_DESLIGA) >= 0) {
    Serial.println("RECEBI O COMANDO: DESLIGAR");
  }
  if (cmd.indexOf(CMD_CIMA_1) >= 0) {
    Serial.println("RECEBI O COMANDO: CIMA 1");
  }
  if (cmd.indexOf(CMD_CIMA_2) >= 0) {
    Serial.println("RECEBI O COMANDO: CIMA 2");
  }
  if (cmd.indexOf(CMD_BAIXO_1) >= 0) {
    Serial.println("RECEBI O COMANDO: BAIXO 1");
  }
  if (cmd.indexOf(CMD_BAIXO_2) >= 0) {
    Serial.println("RECEBI O COMANDO: BAIXO 2");
  }
  if (cmd.indexOf(CMD_ROT_ESQ) >= 0) {
    Serial.println("RECEBI O COMANDO: ROT - ESQUERDA");
  }
  if (cmd.indexOf(CMD_ROT_DIR) >= 0) {
    Serial.println("RECEBI O COMANDO: ROT - DIREITA");
  }
  if (cmd.indexOf(CMD_FRENTE) >= 0) {
    Serial.println("RECEBI O COMANDO: FRENTE");
  }
  if (cmd.indexOf(CMD_TRAS) >= 0) {
    Serial.println("RECEBI O COMANDO: TRAS");
  }
  if (cmd.indexOf(CMD_ESTAB) >= 0) {
    Serial.println("RECEBI O COMANDO: ESTAB");

  }
  if (cmd.indexOf(CMD_PARADO) >= 0) {
    Serial.println("RECEBI O COMANDO: PARADO");
  }

  if (cmd.indexOf(CMD_LIGA_DRONE) >= 0) {
    transmit(500);
    delay(300);
  }
}

void updateAccelValues(){ // Atualiza os valores do giroscópio e acelerômetro.
//  Wire.beginTransmission(MPU);
//  Wire.write(0x3B);  // starting with register 0x3B (ACCEL_XOUT_H)
//  Wire.endTransmission(false);
//  //Solicita os dados do sensor
//  Wire.requestFrom(MPU,14,true);  
//  //Armazena o valor dos sensores nas variaveis correspondentes
//  AcX=Wire.read()<<8|Wire.read();  //0x3B (ACCEL_XOUT_H) & 0x3C (ACCEL_XOUT_L)     
//  AcY=Wire.read()<<8|Wire.read();  //0x3D (ACCEL_YOUT_H) & 0x3E (ACCEL_YOUT_L)
//  AcZ=Wire.read()<<8|Wire.read();  //0x3F (ACCEL_ZOUT_H) & 0x40 (ACCEL_ZOUT_L)
//  Tmp=Wire.read()<<8|Wire.read();  //0x41 (TEMP_OUT_H) & 0x42 (TEMP_OUT_L)
//  GyX=Wire.read()<<8|Wire.read();  //0x43 (GYRO_XOUT_H) & 0x44 (GYRO_XOUT_L)
//  GyY=Wire.read()<<8|Wire.read();  //0x45 (GYRO_YOUT_H) & 0x46 (GYRO_YOUT_L)
//  GyZ=Wire.read()<<8|Wire.read();  //0x47 (GYRO_ZOUT_H) & 0x48 (GYRO_ZOUT_L)
}

void transmit(int cmd){ // transmite o comando para o drone via nrf24l01.
  delay(30);
  radio.stopListening();
  radio.write(&cmd, sizeof(int));
  radio.startListening();
  delay(30);
}
