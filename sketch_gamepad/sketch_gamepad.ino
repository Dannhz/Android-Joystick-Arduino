/**
   Gamepad - DRONE ULTRON
   Trabalho de Conclusão de Curso - Etec Martin Luther King - 2º Semestre 2021
   Orientação de Paulo Roberto Murger Nogueira, Prof. Me. Eng.
   Sketch elaborada por Danilo Caetano. Mais detalhes em dannhz.github.io
*/

//LIBS -----------------------------------------------------------------------
#include <SoftwareSerial.h>
#include <RF24.h>
//VARIÁVEIS ------------------------------------------------------------------
unsigned long msAtual; // obtém o milissegundo atual em cada loop.
unsigned long ultimaTransmissao = 0;
unsigned long ultimaMedicao = 0;
bool btLigado = false; // liga ao desbloquear através do app mobile.
bool radioLigado = false; // Desliga ao receber um sinal através do drone.
String comandoRec = ""; // comando recebido do celular.


//VETORES DE COMUNICAÇÃO -----------------------------------------------------
int msgRecApp[4] = {0, 0, 0, 0}; // Vetor com as mensagens recebidas do celular.
#define THROTTLE 0 //[0] = Throttle / Altura
#define YAW 1 //[1] = Yaw / Rotação
#define PITCH 2 //[2] = Pitch / Movimento Frente/Trás
#define COMANDO_REC 3//[3] = Comandos Extra. --------
//  (0) - N/M.
//  (1) - Ligar.
//  (2) - Ping.
int msgEnvApp[4] = {0, 0, 0, 0}; // Vetor com as mensagens que serão enviadas ao celular
#define INDEX_PING_DRONE 0//[0] = Ping gamepad/drone
#define INDEX_BATERIA_GAMEPAD 1//[1] = Bateria gamepad
#define INDEX_BATERIA_DRONE 2//[2] = Bateria drone
#define INDEX_COMANDO_ENVIAR 3//[3] = Comandos Extra. --------
//  (0) - N/M.
//  (1) - Gamepad ligado.
//  (2) - Tentando sincronizar com o drone
//  (3) - Sincronia realizada
int msgRecDrone = 0; // Variável que armazenará a mensagem recebida pelo drone.
int msgEnvDrone[4] = {0, 0, 0, 0}; // Mesmo vetor que será intermediado ao drone.

//CONSTANTES -----------------------------------------------------------------
#define intervaloTransmissao 70
#define intervaloMedicao 6000 // De 5 em 5 segundos realiza a medição de bateria e ping do drone e gamepad.
#define pinBateria A0
const uint64_t pipes[2] = { 0xF0F0F0F0E1LL, 0xF0F0F0F0D2LL };
//OBJETOS --------------------------------------------------------------------
SoftwareSerial btSerial(3, 4);
RF24 radio(7, 8);

void setup() {
  pinMode(pinBateria, INPUT);
  Serial.begin(9600);
  btSerial.begin(9600);
  radio.begin();
  radio.setChannel(100);
  radio.openWritingPipe(pipes[1]);
  radio.openReadingPipe(0, pipes[0]);
  radio.startListening();
  Serial.println("Gamepad iniciado...");
}

void loop() {
  msAtual = millis();

  if (btLigado) {

    //TRANSMISSAO DE DADOS AO DRONE ----------------------------
    if (msAtual - ultimaTransmissao >= intervaloTransmissao) {
      //      Serial.println("enviar msg ao drone");
      transmissao();
      ultimaTransmissao = msAtual;
      recepcao();
    }

    //MEDIÇÃO DE BATERIA E LATÊNCIA GAMEPAD-DRONE --------------
    if (msAtual - ultimaMedicao >= intervaloMedicao) {      
      medicao();
      ultimaMedicao = msAtual;
    }

  }


  if (mobileListener()) {
    if (converteCmdRecebido(comandoRec)) {
      msgEnvDrone[THROTTLE] = msgRecApp[THROTTLE];
      msgEnvDrone[YAW] = msgRecApp[YAW];
      msgEnvDrone[PITCH] = msgRecApp[PITCH];
      Serial.print("Throttle = " + String(msgRecApp[THROTTLE]) + " - " );
      Serial.print("Yaw = " + String(msgRecApp[YAW]) + " - " );
      Serial.print("Pitch = " + String(msgRecApp[PITCH]) + " - " );
      Serial.println("Comando = " + String(msgRecApp[COMANDO_REC]));

      if (msgRecApp[3] == 1) {
        Serial.println("Gamepad iniciado.");
        msgEnvApp[3] = 1;
        btLigado = true;
      }

      if (msgRecApp[3] == 2) {
        sendToPhone();
      }
    }
    comandoRec = "";
  }

  if (msgRecApp[3] = 1) {

  }
}

bool mobileListener() { // Faz uma leitura contínua dos comandos recebidos pelo celular.
  if (btSerial.available() > 0) {
    while (btSerial.available()) {
      char caracter = btSerial.read();
      comandoRec += caracter;
      delay(1);
    }
    return true;
  } else return false;
}

bool converteCmdRecebido(String comandos) {
  String buff = "";
  int contagem = 0;
  if (comandos.startsWith("{") && comandos.endsWith(";}")) { // Caso o comando comece com '{' e termine com ';}' indica que não houve perda no dado recebido.
    for (int indice = 1; indice < comandos.length() - 1; indice++) {
      if (comandos[indice] != ';') {
        buff += comandos[indice];
      } else {
        if (contagem <= 3) { // Verifica se não excedeu o limite de 4 comandos recebidos.
          msgRecApp[contagem] = buff.toInt();
          contagem++;
          buff = "";
        } else {
          return false;
        }
      }
    }
  } else {
    return false;
  }

  return contagem == 4 ? true : false; // Deve ter chegado 4 comandos distintos, caso contrário é descartado o resultado obtido.
}

void sendToPhone() { // Envia o vetor (em formato de texto) para o celular.

  String msgEnvAppiada = "[" + String(msgEnvApp[INDEX_PING_DRONE]) + ";";
  msgEnvAppiada += String(msgEnvApp[INDEX_BATERIA_GAMEPAD]) + ";";
  msgEnvAppiada += String(msgEnvApp[INDEX_BATERIA_DRONE]) + ";";
  msgEnvAppiada += String(msgEnvApp[INDEX_COMANDO_ENVIAR]) + ";]";

  //  for (int i = 0; i < sizeof(msgEnvApp) / sizeof(int); i++) {
  //    msgEnvAppiada += String(msgEnvApp[i]) + ";";
  //  }
  //  msgEnvAppiada += "]";

  btSerial.println(msgEnvAppiada);
  msgEnvApp[INDEX_COMANDO_ENVIAR] = 0;
  delay(5);
}

void transmissao() {
  radio.stopListening();
  while (radio.write(&msgEnvDrone, sizeof(msgEnvDrone))) {}
  msgEnvDrone[3] = 0;
  radio.startListening();
}

void recepcao() {
  if (radio.available()) {
    delay(1);
    radio.read(&msgRecDrone, sizeof(msgRecDrone));
  }
  if (msgRecDrone != 0) {
    msgRecDrone = 0;
    msgEnvApp[INDEX_COMANDO_ENVIAR] = 2;
    Serial.println("Recebi o retorno do ping");
    msgEnvApp[INDEX_PING_DRONE] = millis() - ultimaMedicao;
  }
}

void medicao() {
  msgEnvDrone[3] = 2;
  Serial.println("Pingando agora...");
  float bateriaGamepadAtual = analogRead(pinBateria) * (5.0 / 1023);
  if (bateriaGamepadAtual >= 3.5) {
    msgEnvApp[INDEX_BATERIA_GAMEPAD] = 3;
  }
  else if (bateriaGamepadAtual >= 3) {
    msgEnvApp[INDEX_BATERIA_GAMEPAD] = 2;
  }
  else {
    msgEnvApp[INDEX_BATERIA_GAMEPAD] = 1;
  }

  float bateriaDroneAtual = random(20, 48) / 10;
  if (bateriaDroneAtual >= 3.5) {
    msgEnvApp[INDEX_BATERIA_DRONE] = 3;
  }
  else if (bateriaDroneAtual >= 3) {
    msgEnvApp[INDEX_BATERIA_DRONE] = 2;
  }
  else {
    msgEnvApp[INDEX_BATERIA_DRONE] = 1;
  }
}
