# WORK IN PROGRESS

# CO2 Sensor program
You need to set the wifi ssid and password in the program, but part of the program also works without it. The ESP8266 will measure the CO2 concentration and the temperature as well as the humidity once per second. This data can then be called up via the sensor's website. All data is also shown on the display.


# The Sensorbox

The Sensorbox can be found here: https://www.thingiverse.com/jannis1602


<img src="https://user-images.githubusercontent.com/63098334/118721074-addcd480-b82a-11eb-88e6-029365b56ccb.jpg" width="400">
<img src="https://user-images.githubusercontent.com/63098334/118721090-b503e280-b82a-11eb-996c-d8ba4c107b9e.jpg" width="400">


# Program for collecting and evaluating values
The java program collects the data of all confugured sensors in the same network x times per minute. The collected values are then displayed graphically.
