//LIBS
#include <RF24.h>
#include <Wire.h>

//VARIÁVEIS
bool ligado = false;
int msgRecGamepad[4] = {0, 0, 0, 0};
#define THROTTLE 0 //[0] = Throttle / Altura
#define YAW 1 //[1] = Yaw / Rotação
#define PITCH 2 //[2] = Pitch / Movimento Frente/Trás
#define COMANDO_REC 3//[3] = Comandos Extra. --------

int msgEnvGamepad = 10; // Valor atual da bateria que será utilizado como retorno à solicitação de ping do gamepad

unsigned long msAtual; // obtém o milissegundo atual em cada loop.
unsigned long ultimaRecepcao = 0;
int AcX, AcY, AcZ, Tmp, GyX, GyY, GyZ; //Variaveis para armazenar valores dos sensores


//CONSTANTES
#define intervaloRecepcao 50 // tempo em ms para realizar a leitura dos dados recebidos via rádio.
#define intervaloBateria 5000 // Tempo em ms para mensurar a % da bateria atual do drone
const uint64_t pipes[2] = { 0xF0F0F0F0E1LL, 0xF0F0F0F0D2LL };
//const int MPU=0x68; //Endereco I2C do MPU6050

//OBJETOS
RF24 radio(7, 8);

void setup() {
  Serial.begin(9600);
  Serial.println("Drone iniciado.");

  //Radio
  radio.begin();
  radio.setChannel(100); // Ajuda a evitar interferências de roteadores.
  radio.openWritingPipe(pipes[0]);
  radio.openReadingPipe(0, pipes[1]);
  radio.startListening();

  //MPU
  //  Wire.begin();
  //  Wire.beginTransmission(MPU);
  //  Wire.write(0x6B);
  //  //Inicializa o MPU-6050
  //  Wire.write(0);
  //  Wire.endTransmission(true);


}

void loop() {
  msAtual = millis();

  if (msAtual - ultimaRecepcao > intervaloRecepcao) {
    recepcao();
    Serial.println("THROTTLE = " + String(msgRecGamepad[0]) + ", TAW = " +
                   String(msgRecGamepad[1]) + ", PITCH = " +
                   String(msgRecGamepad[2]) + ", CMD = " +
                   String(msgRecGamepad[3]));

    if (msgRecGamepad[3] == 2) {
      Serial.println("Retornando msg...");
      transmissao();
    }
    ultimaRecepcao = msAtual;
  }
}

void recepcao() {
  if (radio.available()) {
    radio.read(&msgRecGamepad, sizeof(msgRecGamepad));
  }
}

void transmissao() {
  radio.stopListening();
  while (radio.write(&msgEnvGamepad, sizeof(msgEnvGamepad))) {}
  msgRecGamepad[3] = 0;
  Serial.println("RETORNANDO PING");
  radio.startListening();
}

void updateAccelValues() { // Atualiza os valores do giroscópio e acelerômetro.
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
