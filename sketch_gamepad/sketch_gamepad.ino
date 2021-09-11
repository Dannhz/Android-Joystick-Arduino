//LIBS
#include <SoftwareSerial.h>
#include <RF24.h>

//VARIÁVEIS
  String comandoRec = ""; // comando recebido do celular.
  bool ligado = false; // liga ao desbloquear através do app mobile.
  unsigned long ultimoPing = 0; // armazena o momento em que foi realizado o ultimo ping.
  unsigned long msAtual; // obtém o milissegundo atual em cada loop.
  int pingDelay; // tempo em ms 
  String dadoEnvio; // dado que será enviado ao drone
  String dadoRecebido; // dado que será recebido do drone

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
  #define intervaloMs 2000 // tempo em ms para realizar o teste contínuo de latência.
  #define btTx 3 // Pinagem do Tx
  #define btRx 4 // Pinagem do Rx
  const byte enderecos[][6] = {"1node", "2node"};
  byte radioID = 0;

  //OBJETOS
  SoftwareSerial btSerial(btTx, btRx); 
  RF24 radio(7, 8);


void setup() {
  Serial.begin(9600);
  btSerial.begin(9600);

  //COMUNICAÇÃO - GAMEPAD/DRONE -----------
  #if radioID == 0
  radio.openWritingPipe(enderecos[0]);
  radio.openReadingPipe(1, enderecos[1]);

  #else
  radio.openWritingPipe(enderecos[1]);
  radio.openReadingPipe(1, enderecos[0]);
  #endif

  radio.startListening();
}

void loop() {
  msAtual = millis();
  
  if(mobileListener()) { // Caso receba um comando, chama a função para tratá-lo.
    comandoRec = mobileHandler(comandoRec);
  } 
  if(droneListener()){
    Serial.println("Recebi dado");
  }
  
  if(ligado){
    if(msAtual - ultimoPing >= intervaloMs){
      encaminharComando("{ping}");
      ultimoPing = msAtual;
    }
  }
}
  
bool mobileListener(){ // Faz uma leitura contínua dos comandos recebidos pelo celular.
  if (btSerial.available() > 0) {
    while (btSerial.available()) {
      char caracter = btSerial.read();
      comandoRec += caracter;
      delay(1);
    }
    return true;
  }else return false;
}

String mobileHandler(String cmd){ //Tratamento do comando recebido pelo celular.
  
  if (cmd.indexOf(CMD_LIGA) >= 0){
    ligado = true;
    Serial.println("Ligado");
    }
  if (cmd.indexOf(CMD_DESLIGA) >= 0){
    ligado = false;
    Serial.println("Desligado");
    }

  if(ligado){
    if (cmd.indexOf(CMD_CIMA_1) >= 0) {
      Serial.println("Subir + ");
      encaminharComando(CMD_CIMA_1);
    }
    if (cmd.indexOf(CMD_CIMA_2) >= 0) {
      Serial.println("Subir ++ ");
      encaminharComando(CMD_CIMA_2);
    }
    if (cmd.indexOf(CMD_BAIXO_1) >= 0) {
      Serial.println("Descer + ");
      encaminharComando(CMD_BAIXO_1);
    }
    if (cmd.indexOf(CMD_BAIXO_2) >= 0) {
      Serial.println("Descer ++ ");
      encaminharComando(CMD_BAIXO_2);
    }
    if (cmd.indexOf(CMD_ESTAB) >= 0 ) {
      Serial.println("Estabilizar");
      encaminharComando(CMD_CIMA_2);
    }
    if (cmd.indexOf(CMD_PARADO) >= 0 ) {
      Serial.println("Parado");
      encaminharComando(CMD_PARADO);
    }
    if (cmd.indexOf(CMD_FRENTE) >= 0) {
      Serial.println("Frente + ");
      encaminharComando(CMD_FRENTE);
    }
    if (cmd.indexOf(CMD_TRAS) >= 0) {
      Serial.println("Trás +");
      encaminharComando(CMD_TRAS);
    }
    if (cmd.indexOf(CMD_ROT_ESQ) >= 0) {
      Serial.println("Rotacionar Esq.");
      encaminharComando(CMD_ROT_ESQ);
    }
    if (cmd.indexOf(CMD_ROT_DIR) >= 0) {
      Serial.println("Rotacionar Dir.");
      encaminharComando(CMD_ROT_DIR);
    }
  }
    return "";
}

bool droneListener(){ // Faz uma leitura contínua das mensagens recebidas pelo drone (Bateria, ping);
  if(radio.available()){
    return true;
  }
  return false;
}

void checkPing(){ // encaminha o pacote de dados recebido do drone ao celular.
  pingDelay = msAtual - ultimoPing;
  btSerial.println("[" + String(pingDelay) + "}");
}

void encaminharComando(String codComando){
  radio.stopListening();
  while(true){
    if(radio.write(&codComando, sizeof(String))){
      break;
    }
  }
  radio.startListening();
}
