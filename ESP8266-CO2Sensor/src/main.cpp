#include "DHT.h"
#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
#include <Wire.h>
#include "SSD1306Ascii.h"
#include "SSD1306AsciiWire.h"


#define CO2_IN 15  // pin for CO2-pwm reading

//OLED Display
#define I2C_ADDRESS 0x3C
#define SCREEN_WIDTH 128 // OLED display width in pixels
#define SCREEN_HEIGHT 64 // OLED display height in pixels
SSD1306AsciiWire oled;
// Temperature sensor
DHT dht(2, DHT11);

/*
 * Atmospheric CO2 Level..............400ppm
 * Average indoor co2.............350-450ppm
 * Maxiumum acceptable co2...........1000ppm
 * Dangerous CO2 levels.............>2000ppm
 */

//WIFI
const char *ssid = "SSID";          // The SSID (name) of the Wi-Fi network you want to connect to
const char *password = "PASSWORD"; // The password of the Wi-Fi network
const char *hostname = "ESP8266-0";          // The ESP8266 hostname
IPAddress staticIP(172, 16, 30, 63);         // statische IP des NodeMCU ESP8266
IPAddress subnet(255, 255, 0, 0);            // Subnetzmaske des Netzwerkes
IPAddress gateway(192, 16, 30, 1);           // IP-Adresse des Router
IPAddress dns(192, 16, 30, 1);               // DNS Server

//VAR
float humidity = 0;
float tWert = 0;
int temperaturWert = 20;
float temperaturWerte[4] = {20, 20, 20, 20};
int PPM = 0;
float prozent = 0;
unsigned long ZeitMikrosekunden;
unsigned long ZeitMillisekunden;
int Messbereich = 5000; //or 3000PPM
int heatupTime = 120;
ESP8266WebServer server(80);

int readCO2PWM()
{
  unsigned long th, tl, ppm_pwm = 0;
  do
  {
    th = pulseIn(CO2_IN, HIGH, 1004000) / 1000;
    tl = 1004 - th;
    ppm_pwm = 5000 * (th - 2) / (th + tl - 4);
  } while (th == 0);
  // Serial.print("PPM PWM: ");
  // Serial.println(ppm_pwm);
  return ppm_pwm;
}

void setup()
{
  Serial.begin(9600);
  pinMode(CO2_IN, INPUT); // CO2 Sensor
  dht.begin(); // Temperatur Sensor

  Wire.begin(); //OLED Display
  Wire.setClock(400000L);
  oled.begin(&Adafruit128x64, I2C_ADDRESS);

  //WIFI config
  WiFi.softAPdisconnect(true); // deactivate AC-Point
  WiFi.begin(ssid, password);
  WiFi.hostname(hostname);
  WiFi.config(staticIP, gateway, subnet, dns);

  oled.setFont(System5x7);
  oled.clear();
  oled.println("Hostname: " + WiFi.hostname());
  oled.println("IP: " + WiFi.localIP().toString());
  oled.println("SSID: " + String(ssid));
  oled.println();
  oled.print("connecting.");
  Serial.print("Connecting to WiFi.");

  int i = 0;                            //test ende nach 15 sec
  while (WiFi.status() != WL_CONNECTED) // nach 1  min starten!
  {
    delay(1000);
    Serial.print(".");
    oled.print(".");
    if(i==10)
      oled.println("");
    i++;
    if (i > 20) // tries 20 seconds to connect to the wifi
      break;    // otherwise it runs without wifi
  }

  oled.setFont(System5x7);
  oled.clear();
  if (WiFi.status() == WL_CONNECTED)
  {
    oled.println("Name: " + String(hostname));
    oled.println("IP: " + staticIP.toString());
  }
  else
    oled.println("NO WIFI!");
  Serial.print("IP Address: ");
  Serial.println(WiFi.localIP());
  Serial.println("");

// WEBSERVER
  server.on("/", []() {
    server.send(200, "html", "Sensorwerte: \n#humidity: " + String(humidity) + " \n#temperature: " + temperaturWert + " \n#CO2-Konzentration: " + PPM); //co2konz
  });
  //Reset sensor over website?
  server.begin();
  //WEBSERVER END

  // Heatup CO2 Sensor
  while (heatupTime > 0)
  {
    heatupTime--;
    delay(1000);
    oled.setFont(System5x7);
    oled.clear();
    if (WiFi.status() == WL_CONNECTED)
    {
      oled.println("Name: " + String(hostname));
      oled.println("IP: " + staticIP.toString());
    }
    else
      oled.println("NO WIFI!");
    oled.println(" ");
    oled.println(" ");
    oled.print("     Heatup: ");
    oled.print(heatupTime);
    oled.print(" s");
    Serial.println(heatupTime);
  }
}

void loop()
{
  delay(4000);
  humidity = dht.readHumidity();
  tWert = dht.readTemperature();
  int newPPM = readCO2PWM();
  if (newPPM > 100)
    PPM = newPPM;
  // Serial.println(PPM);

  Serial.print(F("Humidity: "));
  Serial.print(humidity);
  Serial.print(F("% "));
  Serial.print("  Temperature: ");
  Serial.print(temperaturWert);
  Serial.print(F("°C "));
  Serial.print("  CO2: ");
  Serial.print(PPM);
  Serial.println("ppm ");

  temperaturWerte[0] = temperaturWerte[1];
  temperaturWerte[1] = temperaturWerte[2];
  temperaturWerte[2] = temperaturWerte[3];
  temperaturWerte[3] = tWert;
  temperaturWert = (int)((temperaturWerte[0] + temperaturWerte[1] + temperaturWerte[2] + temperaturWerte[3]) / 4);
  server.handleClient();

  oled.setFont(System5x7); 
  oled.clear();         

  if (WiFi.status() == WL_CONNECTED)
  {
    oled.println("Name: " + String(hostname));
    oled.println("IP: " + staticIP.toString());
  }
  else
    oled.println("NO WIFI!");
  oled.println();
  oled.println("Feuchtigkeit: " + String(humidity) + "%");
  oled.println("Temperatur: " + String(temperaturWert) + " C");
  oled.println("CO2-Konz: " + String(PPM) + " ppm");
}
