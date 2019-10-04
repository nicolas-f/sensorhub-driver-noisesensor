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

import net.opengis.swe.v20.*;
import org.sensorhub.api.sensor.SensorDataEvent;
import org.sensorhub.impl.sensor.AbstractSensorOutput;
import org.vast.swe.SWEHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;


public class SamplesAcousticOutput extends AbstractSensorOutput<NoiseMonitoringSensor>
{
    private DataComponent acousticData;
    private DataEncoding acousticEncoding;
    private Timer timer;

    SamplesAcousticOutput(NoiseMonitoringSensor parentSensor)
    {
        super(parentSensor);
    }


    @Override
    public String getName()
    {
        return "acoustic_samples";
    }


    protected void init()
    {
        SWEHelper fac = new SWEHelper();
        
        // build SWE Common record structure
    	acousticData = fac.newDataRecord(2);
        acousticData.setName(getName());
        acousticData.setDefinition("http://sensorml.com/ont/swe/property/Acoustic");
        acousticData.setDescription("Encrypted acoustic measurements");

        Count elementCount = fac.newCount();
        elementCount.setValue(1);
        
        // add time, temperature, pressure, wind speed and wind direction fields
        acousticData.addComponent("time", fac.newTimeStampIsoUTC());
        acousticData.addComponent("samples", fac.newText("samples", "samples", "Encrypted audio samples"));

        // also generate encoding definition
        acousticEncoding = fac.newTextEncoding(",", "\n");
    }

    public String getUrl() {
        return getParentModule().getConfiguration().httpSamplesAcousticStationUrl;
    }
    
    private void sendMeasurement()
    {
        try {
            URL url = new URL(getUrl());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(getParentModule().getConfiguration().httpTimeout);
            conn.setRequestMethod("GET");
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            DataBlock dataBlock = acousticData.createDataBlock();
            List<DataBlock> dataBlockList = new ArrayList<>();
            while ((line = rd.readLine()) != null) {
                StringTokenizer tokenizer = new StringTokenizer(line, ",");
                dataBlock.setDoubleValue(0, Double.valueOf(tokenizer.nextToken()));
                dataBlock.setStringValue(1, tokenizer.nextToken());
                // Push block
                dataBlockList.add(dataBlock);
            }
            rd.close();
            if(!dataBlockList.isEmpty()) {
                // update latest record and send event
                latestRecord = dataBlockList.get(dataBlockList.size() - 1);
                latestRecordTime = (long)dataBlockList.get(dataBlockList.size() - 1).getDoubleValue(0);
                eventHandler.publishEvent(new SensorDataEvent(System.currentTimeMillis(),
                        SamplesAcousticOutput.this, dataBlockList.toArray(new DataBlock[dataBlockList.size()])));
            }
        } catch (IOException ex) {
            parentSensor.getLogger().error("Error while receiving data", ex);
        }
    }


    protected void start()
    {
        if (timer != null || getUrl().isEmpty())
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
    	// approximate update interval in second
        return 60.0;
    }


    @Override
    public DataComponent getRecordDescription()
    {
        return acousticData;
    }


    @Override
    public DataEncoding getRecommendedEncoding()
    {
        return acousticEncoding;
    }
}
