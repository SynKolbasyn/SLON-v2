#include "BluetoothSerial.h"

#define DEBUG 1

BluetoothSerial SerialBT;

double oldDist;
double oldErr;


unsigned long dt;
unsigned long oldDt;
double i;

double c = -180;
double prevC = c;
bool stopCalc = false;

void debug(String a) {
  if (DEBUG) {
    Serial.println(a);
  }
}

double speedPID(double dist) {
  double pk = 60.0;
  double ik = 0.0;
  double dk = 0.0;

  double p = dist * pk;
  double i = 0 * ik;
  double d = (dist - oldDist) * dk;

  double u = p + i + d;

  oldDist = dist;

  if (abs(u) > 600) {
    u = u / abs(u) * 600;
  }
  debug("speedPID u = " + String(u));
  return u;
}

double degreePID(double degree, double etalonDegree) {
  dt = millis() - oldDt;

  double pk = 3.3333333333333335;
  double ik = 0.2;
  double dk = 0.1;

  double err = etalonDegree - degree ;

  double p = err * pk;
  if (isnan(i)) {
    i = (i + err * dt) * ik;
  }
  double d = (err - oldErr) / dt * dk;
  double u = p + i + d;
  
  oldErr = err;
  oldDt = millis();

  debug("degreePID u = " + String(u));
  return u;
}

double PID(double dist, double degree, double etalonDegree) {
  double u = speedPID(dist) + degreePID(degree, etalonDegree);

  debug("PID u = " + String(u));
  return u;
}

double getDist(String a) {
  String dist;
  for (int i = 0; i < a.indexOf(";"); i++) {
    dist += a[i];
  }
  debug("dist = " + dist);
  return dist.toDouble();
}

double getDegree(String a) {
  String degree;
  for (int i = a.indexOf(";") + 1; i < a.lastIndexOf(";"); i++) {
    degree += a[i];
  }
  debug("degree = " + degree);
  return degree.toDouble();
}

double getEtalonDegree(String a) {
  String etalonDegree;
  for (int i = a.lastIndexOf(";") + 1; i < a.length(); i++) {
    etalonDegree += a[i];
  }
  debug("etalonDegree = " + etalonDegree);
  return etalonDegree.toDouble();
}

String getAndroidData() {
  String s;

  if (!SerialBT.available()) {
    return "-1";
  }

  while(SerialBT.available()) {
    char c = SerialBT.read();
    s = s + c;
  }

  debug("androidData = " + s);
  return s;
}

void setup() {
  Serial.begin(115200);
  SerialBT.begin("ESP32");
  if (!SerialBT.begin("ESP32")) {
    Serial.println("An error occurred initializing Bluetooth");
  } else {
    Serial.println("Bluetooth initialized");
  }
}

void loop() {
  String a = getAndroidData();
  if (a == "-1"){
    return;
  }
  double dist = getDist(a);
  double degree = getDegree(a);
  double etalonDegree = getEtalonDegree(a);
  double u = degreePID(degree, etalonDegree);
  Serial.println(u);
  delay(100);
  Serial.println("----------------------------------------------------------------------------------------------------");
}
