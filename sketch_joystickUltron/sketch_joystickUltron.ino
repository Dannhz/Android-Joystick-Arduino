#include <SoftwareSerial.h>

//VARIÁVEIS
  String comandoRec = ""; // comando recebido do celular.
  bool ligado = false; // liga ao desbloquear através do app mobile.
  unsigned long ultimoPing = 0; // armazena o momento em que foi realizado o ultimo ping.
  unsigned long msAtual; // obtém o milissegundo atual em cada loop.
  int pingDelay; // tempo em ms 
  
  //CONSTANTES
  #define intervaloMs 4000 // tempo em ms para realizar o teste contínuo de latência.
  #define btTx 10 // Pinagem do Tx
  #define btRx 11 // Pinagem do Rx
  
  //OBJETOS
  SoftwareSerial btSerial(btTx, btRx); 

void setup() {
  Serial.begin(9600);
  btSerial.begin(9600);
}

void loop() {
  msAtual = millis();
  if(checkBt()) {comandoRec = cmdHandler(comandoRec);} // Caso haja um comando recebido, chama a função para tratá-la.
  
  if(ligado){
    if(msAtual - ultimoPing >= intervaloMs){
      Serial.println("Ping...");
      btSerial.println("{ping}");
      ultimoPing = msAtual;
    }
  }
}
  
bool checkBt(){
  if (btSerial.available() > 0) {
    while (btSerial.available()) {
      char caracter = btSerial.read();
      comandoRec += caracter;
      delay(1);
    }
    return true;
  }else return false;
}

String cmdHandler(String cmd){
  if (cmd.indexOf("1") >= 0){
    ligado = true;
    Serial.println("Ligado");
    }
  if (cmd.indexOf("0") >= 0){
    ligado = false;
    Serial.println("Desligado");
    }
    
  if(ligado){
    if (cmd.indexOf("a+") >= 0) {Serial.println("Subir + ");}
    if (cmd.indexOf("A+") >= 0) {Serial.println("Subir ++ ");}
    if (cmd.indexOf("a-") >= 0) {Serial.println("Descer + ");}
    if (cmd.indexOf("A-") >= 0) {Serial.println("Descer ++ ");}
    if (cmd.indexOf("e") >= 0 ) {Serial.println("Estabilizar");}
    if (cmd.indexOf("p") >= 0 ) {Serial.println("Parado");}
    if (cmd.indexOf("f+") >= 0) {Serial.println("Frente + ");}
    if (cmd.indexOf("f-") >= 0) {Serial.println("Trás +");}
    if (cmd.indexOf("r-") >= 0) {Serial.println("Rotacionar Esq.");}
    if (cmd.indexOf("r+") >= 0) {Serial.println("Rotacionar Dir.");}
    if (cmd.indexOf("{pong}") >= 0) {checkPing();}
  }
    return "";
}

void checkPing(){
  pingDelay = msAtual - ultimoPing;
  Serial.println("Pong... " + String(pingDelay) + " ms!");
}
