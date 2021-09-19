/**
 * Sketch do Gamepad que servirá como uma espécie de "intermediador" entre o 
 * Joystick Mobile e o Próprio Drone.
 */

//LIBS
#include <SoftwareSerial.h>
#include <RF24.h>

//VARIÁVEIS
  String comandoRec = ""; // comando recebido do celular.
  bool ligado = false; // liga ao desbloquear através do app mobile.
  unsigned long ultimoPing = 0; // armazena o momento em que foi realizado o ultimo ping.
  unsigned long ultimoPiscaLed = 0; // Piscar led sem usar delay.
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
  #define intervaloMs 3000 // tempo em ms para realizar o teste de latência Mobile <-> Gamepad

  #define ledR 5
  #define ledG 6
  #define ledB 7
  const byte enderecoTransmissao[6] = "65852";

  //OBJETOS
  SoftwareSerial btSerial(3, 4); 
  RF24 radio(9, 10);

void setLedColor(byte numLed, bool manterAnterior = false){ // Altera a cor do led RGB//
  if(!manterAnterior){
    digitalWrite(ledR, 0);
    digitalWrite(ledG, 0);
    digitalWrite(ledB, 0);
  }
  digitalWrite(numLed, 1);
}

void setup() { // put your setup code here, to run once:
  pinMode(ledR, OUTPUT);
  pinMode(ledG, OUTPUT);
  pinMode(ledB, OUTPUT);
  
  Serial.begin(9600);
  btSerial.begin(9600);
  radio.begin();
  
  Serial.println("Gamepad iniciado...");
  setLedColor(ledR);
  radio.openWritingPipe(enderecoTransmissao);
}

void loop() { // put your main code here, to run repeatedly:
  msAtual = millis();
  
  if(mobileListener()) { // Caso receba um comando, chama a função para tratá-lo.
    comandoRec = mobileHandler(comandoRec);
    ultimoPiscaLed = msAtual; 
  } 
//  if(droneListener()){
//    Serial.println("Recebi dado");
//  }
//  
  if(ligado){
    if(msAtual - ultimoPing >= intervaloMs){
      btSerial.println("{ping}");
      ultimoPing = msAtual;
    }
    if(ultimoPiscaLed != 0 && msAtual - ultimoPiscaLed >= 75){
      Serial.println("Piscou");
      ultimoPiscaLed = 0;      
      setLedColor(ledG);
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

String mobileHandler(String cmd){ // Tratamento do comando recebido pelo celular.
  
  if (cmd.indexOf(CMD_LIGA) >= 0){
    ligado = true;
    setLedColor(ledG);
    Serial.println("Ligado");
    }
  if (cmd.indexOf(CMD_DESLIGA) >= 0){
    ligado = false;
    setLedColor(ledR);
    Serial.println("Desligado");
    }

  if(ligado){
    setLedColor(ledB, true);
    if (cmd.indexOf(CMD_CIMA_1) >= 0) {
      Serial.println("Subir + ");
      transmit(CMD_CIMA_1);   
    }
    if (cmd.indexOf(CMD_CIMA_2) >= 0) {
      Serial.println("Subir ++ ");
      transmit(CMD_CIMA_2);
    }
    if (cmd.indexOf(CMD_BAIXO_1) >= 0) {
      Serial.println("Descer + ");
      transmit(CMD_BAIXO_1);
    }
    if (cmd.indexOf(CMD_BAIXO_2) >= 0) {
      Serial.println("Descer ++ ");
      transmit(CMD_BAIXO_2);
    }
    if (cmd.indexOf(CMD_ESTAB) >= 0 ) {
      Serial.println("Estabilizar");
      transmit(CMD_ESTAB);
    }
    if (cmd.indexOf(CMD_PARADO) >= 0 ) {
      Serial.println("Parado");
      transmit(CMD_PARADO);
    }
    if (cmd.indexOf(CMD_FRENTE) >= 0) {
      Serial.println("Frente + ");
      transmit(CMD_FRENTE);
    }
    if (cmd.indexOf(CMD_TRAS) >= 0) {
      Serial.println("Trás +");
      transmit(CMD_TRAS);
    }
    if (cmd.indexOf(CMD_ROT_ESQ) >= 0) {
      Serial.println("Rotacionar Esq.");
      transmit(CMD_ROT_ESQ);
    }
    if (cmd.indexOf(CMD_ROT_DIR) >= 0) {
      Serial.println("Rotacionar Dir.");
      transmit(CMD_ROT_DIR);
    }
    if (cmd.indexOf("{pong}") >= 0) {checkPing();}
  }
    return "";
}

bool droneListener(){ // Faz uma leitura contínua das mensagens recebidas pelo drone (Bateria, ping);
  if(radio.available()){
    return true;
  }
  return false;
}

void checkPing(){ // Encaminha o pacote de dados recebido do drone ao celular.
  pingDelay = msAtual - ultimoPing;
  btSerial.println("[" + String(pingDelay) + "}");
}

void transmit(String codComando){ // transmite o comando para o drone via nrf24l01.
  int comandoConvertido = codComando.toInt();
  while(radio.write(&comandoConvertido, sizeof(int))){}
  Serial.println("Msg enviada.");
}
