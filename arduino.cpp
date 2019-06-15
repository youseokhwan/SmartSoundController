#include <SoftwareSerial.h>

int Sound_Sensor = A0;
int voltage;
SoftwareSerial btSerial(2, 3);

void setup() {
    Serial.begin(9600);
    btSerial.begin(9600);
}

void loop() {
    voltage = analogRead(Sound_Sensor);

    Serial.print("Voltage: ");
    Serial.println(voltage);

    btSerial.println(voltage);

    delay(1000);
}