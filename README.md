# sensorhub-driver-noisesensor

NoiseSensor driver for OpenSensorHub

This driver fetch CSV data using http protocol. Retrieved data are pushed into OpenSensorHub storage.

# Weather Sensor Driver

This driver read the system temperature (cpu or motherboard), external temperature and humidity each minute.

Http server source code:
https://github.com/nicolas-f/sensorhub-driver-noisesensor/blob/master/sensorhub-driver-weather/src/main/python/temphum.py

# Slow acoustic driver

This driver read the leq, laeq each row is 1 second. Http server source code:
https://github.com/nicolas-f/noisesensor/blob/master/service/noisesensor.py

# Fast acoustic driver

This driver read the Leq, and third octave leq 20Hz-12.5kHz (sample rate 32 kHz). Each row is 125ms.
