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

package org.noise_planet.impl.sensor;

import net.opengis.swe.v20.DataBlock;
import org.sensorhub.api.common.SensorHubException;
import org.sensorhub.api.sensor.SensorDataEvent;
import org.sensorhub.impl.sensor.AbstractSensorModule;
import org.vast.sensorML.SMLHelper;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * <p>
 * Driver implementation outputting weather data (temperature, humidity)
 * </p>
 *
 * @author Nicolas Fortin, UMRAE Ifsttar
 */
public class NoiseMonitoringSensor extends AbstractSensorModule<NoiseMonitoringConfig>
{
    WeatherOutput weatherDataInterface;
    SlowAcousticOutput slowAcousticDataInterface;
    FastAcousticOutput fastAcousticDataInterface;
    SamplesAcousticOutput samplesAcousticDataInterface;
    StateOutput stateOutput;
    // Send sensor location after this delay in millisecond
    private static final int LOCATION_UPDATE_DELAY = 15000;

    
    
    @Override
    public void init() throws SensorHubException
    {
        super.init();
        
        // generate identifiers
        generateUniqueID("urn:osh:sensor:noisemonitoring:", config.serialNumber);
        generateXmlID("NOISE_MONITORING_STATION_", config.serialNumber);
        
        // init main data interface
        weatherDataInterface = new WeatherOutput(this);
        slowAcousticDataInterface = new SlowAcousticOutput(this);
        fastAcousticDataInterface = new FastAcousticOutput(this);
        samplesAcousticDataInterface = new SamplesAcousticOutput(this);
        stateOutput = new StateOutput(this);
        addOutput(weatherDataInterface, false);
        addOutput(slowAcousticDataInterface, false);
        addOutput(fastAcousticDataInterface, false);
        addOutput(samplesAcousticDataInterface, false);
        addOutput(stateOutput, false);
        weatherDataInterface.init();
        slowAcousticDataInterface.init();
        fastAcousticDataInterface.init();
        samplesAcousticDataInterface.init();
        stateOutput.init();
    }


    @Override
    protected void updateSensorDescription()
    {
        synchronized (sensorDescLock)
        {
            super.updateSensorDescription();
            
            if (!sensorDescription.isSetDescription())
                sensorDescription.setDescription("Weather station gathering measurements from http endpoint");
            
            SMLHelper helper = new SMLHelper(sensorDescription);
            helper.addSerialNumber(config.serialNumber);
        }
    }


    @Override
    public void start() throws SensorHubException
    {
        if (weatherDataInterface != null) {
            weatherDataInterface.start();
        }
        if (slowAcousticDataInterface != null) {
            slowAcousticDataInterface.start();
        }
        if (fastAcousticDataInterface != null) {
            fastAcousticDataInterface.start();
        }
        if (samplesAcousticDataInterface != null) {
            samplesAcousticDataInterface.start();
        }
        new Timer().schedule(new TimerTask() {
                                 @Override
                                 public void run() {
                                     // Refresh sensor location
                                     eventHandler.publishEvent(new SensorDataEvent(System.currentTimeMillis(), locationOutput, locationOutput.getLatestRecord()));
                                 }
                             }
                             , LOCATION_UPDATE_DELAY);
    }
    

    @Override
    public void stop() throws SensorHubException
    {
        if (weatherDataInterface != null) {
            weatherDataInterface.stop();
        }
        if(slowAcousticDataInterface != null) {
            slowAcousticDataInterface.stop();
        }
        if (fastAcousticDataInterface != null) {
            fastAcousticDataInterface.stop();
        }
        if (samplesAcousticDataInterface != null) {
            samplesAcousticDataInterface.stop();
        }
    }
    

    @Override
    public void cleanup() throws SensorHubException
    {
       
    }
    
    
    @Override
    public boolean isConnected()
    {
        return true;
    }
}

