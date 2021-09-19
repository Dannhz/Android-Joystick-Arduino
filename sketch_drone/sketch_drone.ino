//LIBS
#include <RF24.h>

//VARIÁVEIS
int comandoRec;
unsigned long msAtual; // obtém o milissegundo atual em cada loop.

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

//CONSTANTES
const byte enderecoRecebimento[6] = "65852";
//OBJETOS
RF24 radio(9, 10);

void setup() {
  Serial.begin(9600);
  Serial.println("Drone iniciado.");
  radio.begin();
  radio.openReadingPipe(0, enderecoRecebimento);
  radio.startListening();
}

void loop() {
  msAtual = millis();

  radioListener();
}


void radioListener() {
  if (radio.available()) {
    radio.read(&comandoRec, sizeof(int));
    radioHandler(String(comandoRec));
  }
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
}
