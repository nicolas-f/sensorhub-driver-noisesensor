/*
* BSD 3-Clause License
*
* Copyright (c) 2018, Ifsttar Wi6labs LS2N
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
*  Redistributions of source code must retain the above copyright notice, this
*   list of conditions and the following disclaimer.
*
*  Redistributions in binary form must reproduce the above copyright notice,
*   this list of conditions and the following disclaimer in the documentation
*   and/or other materials provided with the distribution.
*
*  Neither the name of the copyright holder nor the names of its
*   contributors may be used to endorse or promote products derived from
*   this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
* SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
* CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
* OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
*/
package org.noise_planet.impl.sensor.weather;

import org.sensorhub.impl.sensor.AbstractSensorOutput;
import org.sensorhub.api.sensor.SensorDataEvent;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.DataEncoding;
import net.opengis.swe.v20.Quantity;
import org.vast.swe.SWEHelper;


public class WeatherOutput extends AbstractSensorOutput<WeatherSensor>
{
    DataComponent weatherData;
    DataEncoding weatherEncoding;
    Timer timer;
    Random rand = new Random();
    
    // reference values around which actual values vary
    double tempRef = 20.0;
    double pressRef = 1013.0;
    double windSpeedRef = 5.0;
    double directionRef = 0.0;
    
    // initialize then keep new values for each measurement
    double temp = tempRef;
    double press = pressRef;
    double windSpeed = windSpeedRef;
    double windDir = directionRef;

    
    public WeatherOutput(WeatherSensor parentSensor)
    {
        super(parentSensor);
    }


    @Override
    public String getName()
    {
        return "weather";
    }


    protected void init()
    {
        SWEHelper fac = new SWEHelper();
        
        // build SWE Common record structure
    	weatherData = fac.newDataRecord(5);
        weatherData.setName(getName());
        weatherData.setDefinition("http://sensorml.com/ont/swe/property/Weather");
        weatherData.setDescription("Weather measurements");
        
        // add time, temperature, pressure, wind speed and wind direction fields
        weatherData.addComponent("time", fac.newTimeStampIsoUTC());
        weatherData.addComponent("temperature", fac.newQuantity(SWEHelper.getPropertyUri("AirTemperature"), "Air Temperature", null, "Cel"));
        weatherData.addComponent("pressure", fac.newQuantity(SWEHelper.getPropertyUri("AtmosphericPressure"), "Air Pressure", null, "hPa"));
        weatherData.addComponent("windSpeed", fac.newQuantity(SWEHelper.getPropertyUri("WindSpeed"), "Wind Speed", null, "m/s"));
        
        // for wind direction, we also specify a reference frame
        Quantity q = fac.newQuantity(SWEHelper.getPropertyUri("WindDirection"), "Wind Direction", null, "deg");
        q.setReferenceFrame("http://sensorml.com/ont/swe/property/NED");
        q.setAxisID("z");
        weatherData.addComponent("windDirection", q);
     
        // also generate encoding definition
        weatherEncoding = fac.newTextEncoding(",", "\n");
    }

    
    private void sendMeasurement()
    {                
        // generate new weather values
        double time = System.currentTimeMillis() / 1000.;
        
        // temperature; value will increase or decrease by less than 0.1 deg
        temp += variation(temp, tempRef, 0.001, 0.1);
        
        // pressure; value will increase or decrease by less than 20 hPa
        press += variation(press, pressRef, 0.001, 0.1);
        
        // wind speed; keep positive
        // vary value between +/- 10 m/s
        windSpeed += variation(windSpeed, windSpeedRef, 0.001, 0.1);
        windSpeed = windSpeed < 0.0 ? 0.0 : windSpeed; 
        
        // wind direction; keep between 0 and 360 degrees
        windDir += 1.0 * (2.0 * Math.random() - 1.0);
        windDir = windDir < 0.0 ? windDir+360.0 : windDir;
        windDir = windDir > 360.0 ? windDir-360.0 : windDir;
        
        parentSensor.getLogger().trace(String.format("temp=%5.2f, press=%4.2f, wind speed=%5.2f, wind dir=%3.1f", temp, press, windSpeed, windDir));
        
        // build and publish datablock
        DataBlock dataBlock = weatherData.createDataBlock();
        dataBlock.setDoubleValue(0, time);
        dataBlock.setDoubleValue(1, temp);
        dataBlock.setDoubleValue(2, press);
        dataBlock.setDoubleValue(3, windSpeed);
        dataBlock.setDoubleValue(4, windDir);
        
        // update latest record and send event
        latestRecord = dataBlock;
        latestRecordTime = System.currentTimeMillis();
        eventHandler.publishEvent(new SensorDataEvent(latestRecordTime, WeatherOutput.this, dataBlock));
    }
    
    
    private double variation(double val, double ref, double dampingCoef, double noiseSigma)
    {
        return -dampingCoef*(val - ref) + noiseSigma*rand.nextGaussian();
    }


    protected void start()
    {
        if (timer != null)
            return;
        timer = new Timer();
        
        // start main measurement generation thread
        TimerTask task = new TimerTask() {
            public void run()
            {
                sendMeasurement();
            }            
        };
        
        timer.scheduleAtFixedRate(task, 0, (long)(getAverageSamplingPeriod()*1000));        
    }


    @Override
    protected void stop()
    {
        if (timer != null)
        {
            timer.cancel();
            timer = null;
        }
    }


    @Override
    public double getAverageSamplingPeriod()
    {
    	// sample every 1 second
        return 1.0;
    }


    @Override
    public DataComponent getRecordDescription()
    {
        return weatherData;
    }


    @Override
    public DataEncoding getRecommendedEncoding()
    {
        return weatherEncoding;
    }
}
