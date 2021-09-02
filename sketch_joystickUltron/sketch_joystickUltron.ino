//LIBS
#include <SoftwareSerial.h>
#include <AcceleroMMA7361.h>

//VARIÁVEIS
  String comandoRec = ""; // comando recebido do celular.
  bool ligado = false; // liga ao desbloquear através do app mobile.
  unsigned long ultimoPing = 0; // armazena o momento em que foi realizado o ultimo ping.
  unsigned long ultimoAngulo = 0; // armazena o momento em que houve a ultima verificação de angulo.
  unsigned long msAtual; // obtém o milissegundo atual em cada loop.
  int pingDelay; // tempo em ms 

  int x;
  int y;
  int z;

  
  //CONSTANTES
  #define intervaloMs 2000 // tempo em ms para realizar o teste contínuo de latência.
  #define intervaloAcelerometro 250 // tempo em ms para a verificação do ângulo do drone.
  #define btTx 10 // Pinagem do Tx
  #define btRx 11 // Pinagem do Rx

  //OBJETOS
  SoftwareSerial btSerial(btTx, btRx); 
  AcceleroMMA7361 accelero;


void setup() {
  Serial.begin(9600);
  btSerial.begin(9600);

  accelero.begin(4, 5, 6, 7, A0, A1, A2);
  accelero.setARefVoltage(3.3);                   //3.3V para maior precisão
  accelero.setSensitivity(LOW);                   //maior sensibilidade 
  accelero.calibrate();
}

void loop() {
  msAtual = millis();
  
  if(cmdListener()) {comandoRec = cmdHandler(comandoRec);} // Caso receba um comando, chama a função para tratá-lo.
  
  if(ligado){
    if(msAtual - ultimoPing >= intervaloMs){
      btSerial.println("{ping}");
      ultimoPing = msAtual;
    }
    
    if(msAtual - ultimoAngulo >= intervaloAcelerometro){
      printAngulos();
    }
  }
}
  
bool cmdListener(){ // Faz uma leitura contínua dos comandos recebidos
  if (btSerial.available() > 0) {
    while (btSerial.available()) {
      char caracter = btSerial.read();
      comandoRec += caracter;
      delay(1);
    }
    return true;
  }else return false;
}

String cmdHandler(String cmd){ //Tratamento do comando recebido pelo celular.
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
    if (cmd.indexOf("P") >= 0 ) {Serial.println("Parado");}
    if (cmd.indexOf("f+") >= 0) {Serial.println("Frente + ");}
    if (cmd.indexOf("f-") >= 0) {Serial.println("Trás +");}
    if (cmd.indexOf("r-") >= 0) {Serial.println("Rotacionar Esq.");}
    if (cmd.indexOf("r+") >= 0) {Serial.println("Rotacionar Dir.");}
    if (cmd.indexOf("{pong}") >= 0) {checkPing();}
  }
    return "";
}

void checkPing(){ // Recebe o retorno, calcula o delay e envia novamente para ser exibido na tela do celular.
  pingDelay = msAtual - ultimoPing;
  btSerial.println("[" + String(pingDelay) + "}");
}

void printAngulos(){ // Exibe o ângulo atual.
  x = accelero.getXAccel();
  y = accelero.getYAccel();
  z = accelero.getZAccel();
  Serial.print("x: ");
  Serial.print(x);
  Serial.print(" \ty: ");
  Serial.print(y);
  Serial.print(" \tz: ");
  Serial.println(z);
  ultimoAngulo = msAtual;
}
