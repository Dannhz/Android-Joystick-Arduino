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
  unsigned long ultimoBateria = 0; // armazena o momento em que foi realizada a ultima medida de tensão de entrada.
  unsigned long ultimoPiscaLed = 0; // Piscar led sem usar delay.
  unsigned long msAtual; // obtém o milissegundo atual em cada loop.
  int pingDelay; // tempo em ms 
  String dadoEnvio; // dado que será enviado ao drone.
  String dadoRecebido; // dado que será recebido do drone.
  float bateriaGamepad = 0; // Armazena a tensão de entrada do pino analógio responsável pela leitura.
  float bateriaDrone = 0;  // Armazena a tensão de entrada do drone recebida via NRF24L01.


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
  #define intervaloPing 3500 // tempo em ms para realizar o teste de latência Mobile <-> Gamepad <-> Drone
  #define intervaloPiscaLed 75 // tempo em ms para o led piscar
  #define intervaloBateria 5000 // Tempo em ms para mensurar a % da bateria atual do drone e gamepad

  #define ledR 5
  #define ledG 6
  #define ledB 7
  #define pinBateria A0
  const uint64_t pipes[2] = { 0xF0F0F0F0E1LL, 0xF0F0F0F0D2LL };

  //OBJETOS
  SoftwareSerial btSerial(3, 4); 
  RF24 radio(7, 8);

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
  radio.setChannel(100);
  
  Serial.println("Gamepad iniciado...");
  setLedColor(ledR);
  radio.openWritingPipe(pipes[1]);
  radio.openReadingPipe(1, pipes[0]);
  radio.startListening();
}

void loop() { // put your main code here, to run repeatedly:
  msAtual = millis();
  
  if(mobileListener()) { // Caso receba um comando, chama a função para tratá-lo.
    comandoRec = mobileHandler(comandoRec);
    ultimoPiscaLed = msAtual; 
  } 
  if(droneListener()){
    Serial.println("Recebi dado");
  }
  
  if(ligado){
    if(msAtual - ultimoPing >= intervaloPing){
      btSerial.println("{ping}");
      ultimoPing = msAtual;
    }
    if(msAtual - ultimoBateria >= intervaloBateria){
      checkBattery();
      ultimoBateria = msAtual;
    }
      
    if(ultimoPiscaLed != 0 && msAtual - ultimoPiscaLed >= 75){
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
      transmit(CMD_CIMA_1);   
    }
    if (cmd.indexOf(CMD_CIMA_2) >= 0) {
      transmit(CMD_CIMA_2);
    }
    if (cmd.indexOf(CMD_BAIXO_1) >= 0) {
      transmit(CMD_BAIXO_1);
    }
    if (cmd.indexOf(CMD_BAIXO_2) >= 0) {
      transmit(CMD_BAIXO_2);
    }
    if (cmd.indexOf(CMD_ESTAB) >= 0 ) {
      transmit(CMD_ESTAB);
    }
    if (cmd.indexOf(CMD_PARADO) >= 0 ) {
      transmit(CMD_PARADO);
    }
    if (cmd.indexOf(CMD_FRENTE) >= 0) {
      transmit(CMD_FRENTE);
    }
    if (cmd.indexOf(CMD_TRAS) >= 0) {
      transmit(CMD_TRAS);
    }
    if (cmd.indexOf(CMD_ROT_ESQ) >= 0) {
      transmit(CMD_ROT_ESQ);
    }
    if (cmd.indexOf(CMD_ROT_DIR) >= 0) {
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

void checkPing(){ // Retorna o valor do ping ao celular.
  pingDelay = msAtual - ultimoPing;
  btSerial.println("[" + String(pingDelay) + "}");
  Serial.println("pingou");
  delay(20);
}

void checkBattery(){ // Encaminha os valores da bateria do gamepad e drone ao celular.
  bateriaGamepad = random(1,450) / 100.0;
  bateriaDrone = random(1,450) / 100.0;
  Serial.println("(" + String(bateriaGamepad) + "|" + String(bateriaDrone) + "}");
  btSerial.println("(" + String(bateriaGamepad) + "|" + String(bateriaDrone) + "}");
}

void transmit(String codComando){ // transmite o comando para o drone via nrf24l01.
  int comandoConvertido = codComando.toInt();
  radio.stopListening();
  while(radio.write(&comandoConvertido, sizeof(int))){}
  Serial.println("Comando " + codComando);
  radio.startListening();
}
