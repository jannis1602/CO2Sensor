# CO2 Sensor program
You need to set the wifi ssid and password in the program, but part of the program also works without it. The ESP8266 will measure the CO2 concentration and the temperature as well as the humidity once per second. This data can then be called up via the sensor's website but it is also shown on the display.


# The Sensorbox

The Sensorbox can be found here: https://www.thingiverse.com/jannis1602


<img src="https://user-images.githubusercontent.com/63098334/118721074-addcd480-b82a-11eb-88e6-029365b56ccb.jpg" width="400">
<img src="https://user-images.githubusercontent.com/63098334/118721090-b503e280-b82a-11eb-996c-d8ba4c107b9e.jpg" width="400">


# Program for collecting and evaluating data
The java program collects the data of all confugured sensors in the same network and save it all two minutes. The collected values are then displayed graphically.
In the config.txt file you need to list the ESP8266-IPs and their names. ``192.168.178.100#room1`` (one sensor per line)

To load old data just run the program with the date as parameter: ``CO2-Sensor-Server.jar 2021-05-19``
