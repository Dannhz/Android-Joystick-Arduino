//LIBS
#include <SoftwareSerial.h>

#define intervaloMsg 3500 // tempo em ms para realizar o teste de latência Mobile <-> Gamepad <-> Drone
unsigned long msAtual; // obtém o milissegundo atual em cada loop.
unsigned long ultimaMsg = 0; // armazena o momento em que foi realizado o ultimo ping.
bool btLigado = false; // liga ao desbloquear através do app mobile.
String comandoRec = ""; // comando recebido do celular.
int msgRec[4] = {0, 0, 0, 0}; // Vetor com as mensagens recebidas do celular.
//[0] = Throttle / Altura
//[1] = Yaw / Rotação
//[2] = Pitch / Movimento Frente/Trás
//[3] = Comandos Extra. --------
//  (0) - N/M.
//  (1) - Ligar.
//  (2) - Desligar.
//  (3) - Retorno do ping.

int msgEnv[4] = {0, 0, 0, 0}; // Vetor com as mensagens enviadas.
//[0] = Ping cel/gamepad  (-1) Para solicitação
//[1] = Ping gamepad/drone
//[2] = Bateria gamepad
//[3] = Bateria drone

//OBJETOS
SoftwareSerial btSerial(3, 4);

void setup() {

  Serial.begin(9600);
  Serial.println("Iniciado");
  btSerial.begin(9600);

}

void loop() {
  msAtual = millis();

  if(btLigado){
    if(msAtual - ultimaMsg >= intervaloMsg){
      Serial.println("Teoricamente enviarei uma msg!!");
      sendToPhone();
      ultimaMsg = msAtual;
    }
  }

  if (mobileListener()) {
    if (converteCmdRecebido(comandoRec)) {
      Serial.print("Throttle = " + String(msgRec[0]) + " - " );
      Serial.print("Yaw = " + String(msgRec[1]) + " - " );
      Serial.print("Pitch = " + String(msgRec[2]) + " - " );
      Serial.println("Comando = " + String(msgRec[3]));

      if (msgRec[3] == 1) {
        Serial.println("Gamepad iniciado.");
        btLigado = true;
      }
    }
    comandoRec = "";
  }

  if (msgRec[3] = 1) {

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
  if (comandos.startsWith("{") && comandos.endsWith(";}")) { // Caso o comando comece com '{' e termine com ';}' é sinal de que não houve perda no dado recebido.
    for (int indice = 1; indice < comandos.length() - 1; indice++) {
      if (comandos[indice] != ';') {
        buff += comandos[indice];
      } else {
        if (contagem <= 3) { // Verifica se não excedeu o limite de 4 comandos recebidos.
          msgRec[contagem] = buff.toInt();
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
  String msgEnviada = "[";
  for (int i = 0; i < sizeof(msgEnv) / sizeof(int); i++) {
    msgEnviada += String(msgEnv[i]) + ";";
  }
  msgEnviada += "]";
  Serial.println("Msg enviada: " + msgEnviada);
  btSerial.println(msgEnviada);

  delay(10);
}
