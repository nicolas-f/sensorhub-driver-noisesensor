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

import net.opengis.swe.v20.Count;
import net.opengis.swe.v20.DataArray;
import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.DataEncoding;
import net.opengis.swe.v20.DataRecord;
import net.opengis.swe.v20.DataType;
import org.sensorhub.api.sensor.SensorDataEvent;
import org.sensorhub.impl.sensor.AbstractSensorOutput;
import org.vast.data.CountImpl;
import org.vast.data.DataArrayImpl;
import org.vast.swe.SWEHelper;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;


public class FastAcousticOutput extends AbstractSensorOutput<NoiseMonitoringSensor>
{
    private DataComponent acousticData;
    private DataEncoding acousticEncoding;
    private Timer timer;
    // Number of Fast measurement to store into a Document
    public static final int FAST_COUNT_IN_DATARECORD = 8;
    
    private List<String> cachedLines = new ArrayList<>(FAST_COUNT_IN_DATARECORD);
    public static final float[] freqs = new float[]{20, 25, 31.5f, 40, 50, 63, 80, 100, 125, 160, 200, 250, 315, 400, 500, 630, 800, 1000, 1250, 1600, 2000, 2500, 3150, 4000, 5000, 6300, 8000, 10000, 12500};

    FastAcousticOutput(NoiseMonitoringSensor parentSensor)
    {
        super(parentSensor);
    }


    @Override
    public String getName()
    {
        return "acoustic_fast";
    }


    protected void init()
    {
        SWEHelper fac = new SWEHelper();
        
        // build SWE Common record structure
    	acousticData = fac.newDataRecord();
        acousticData.setName(getName());
        acousticData.setDefinition("http://sensorml.com/ont/swe/property/Acoustic");
        acousticData.setDescription("Acoustic indicators measurements");

        Count elementCount = fac.newCount();
        elementCount.setValue(FAST_COUNT_IN_DATARECORD); // FAST_COUNT_IN_DATARECORDx125ms

        // add time, temperature, pressure, wind speed and wind direction fields
        acousticData.addComponent("time", fac.newTimeStampIsoUTC());
        acousticData.addComponent("leq", fac.newArray(elementCount, "leq", fac.newQuantity(SWEHelper.getPropertyUri("dBsplFast"), "Leq", null, "dB", DataType.FLOAT)));
        acousticData.addComponent("laeq", fac.newArray(elementCount, "laeq", fac.newQuantity(SWEHelper.getPropertyUri("dBsplFast"), "LAeq", null, "dB(A)", DataType.FLOAT)));
        for(double freq : freqs) {
            String name = "leq_" + Double.valueOf(freq).intValue();
            acousticData.addComponent(name, fac.newArray(elementCount, name, fac.newQuantity(SWEHelper.getPropertyUri("dBsplFast"), name, null, "dB", DataType.FLOAT)));
        }


        // also generate encoding definition
        acousticEncoding = fac.newTextEncoding(",", "\n");
    }

    static void initCacheValues(Map<String, List<Float>> cachedValues) {
        cachedValues.clear();
        cachedValues.put("leq", new ArrayList<Float>(FAST_COUNT_IN_DATARECORD));
        cachedValues.put("laeq", new ArrayList<Float>(FAST_COUNT_IN_DATARECORD));
        for (float freq : freqs) {
            cachedValues.put(Float.valueOf(freq).toString(), new ArrayList<Float>(FAST_COUNT_IN_DATARECORD));
        }
    }

    public List<DataBlock> parseResult(List<String> cachedLines) throws IOException {
        if(cachedLines.size() < FAST_COUNT_IN_DATARECORD) {
            return new ArrayList<>();
        }
        List<DataBlock> dataBlockList = new ArrayList<>();
        DataBlock dataBlock = acousticData.createDataBlock();
        Map<String, List<Float>> cachedValues = new HashMap<>();

        // Init cache
        initCacheValues(cachedValues);

        int storedResults = 0;
        int idCol = 0;
        while (!cachedLines.isEmpty()) {
            String line = cachedLines.remove(0);
            StringTokenizer tokenizer = new StringTokenizer(line, ",");
            if(storedResults == 0) {
                // Time UTC
                dataBlock.setDoubleValue(idCol++, Double.valueOf(tokenizer.nextToken()));
            } else {
                tokenizer.nextToken();
            }
            // Leq
            cachedValues.get("leq").add(Float.valueOf(tokenizer.nextToken()));
            // Laeq
            cachedValues.get("laeq").add(Float.valueOf(tokenizer.nextToken()));

            // Leq by freq
            for (float freq : freqs) {
                cachedValues.get(Float.valueOf(freq).toString()).add(Float.valueOf(tokenizer.nextToken()));
                //dataBlock.setFloatValue(idCol++, Float.valueOf(tokenizer.nextToken()));
            }
            storedResults++;
            if(storedResults == FAST_COUNT_IN_DATARECORD) {
                for(float val : cachedValues.get("leq")) {
                    dataBlock.setFloatValue(idCol++, val);
                }
                for(float val : cachedValues.get("laeq")) {
                    dataBlock.setFloatValue(idCol++, val);
                }
                for (float freq : freqs) {
                    for(float val : cachedValues.get(Float.valueOf(freq).toString())) {
                        dataBlock.setFloatValue(idCol++, val);
                    }
                }
                // Push block
                dataBlockList.add(dataBlock);
                if(cachedLines.size() < FAST_COUNT_IN_DATARECORD) {
                    break;
                } else {
                    idCol = 0;
                    storedResults = 0;
                    initCacheValues(cachedValues);
                    dataBlock = acousticData.createDataBlock();
                }
            }
        }
        return dataBlockList;
    }

    public String getUrl() {
        return getParentModule().getConfiguration().httpFastAcousticStationUrl;
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
            while ((line = rd.readLine()) != null) {
                cachedLines.add(line);
            }
            List<DataBlock> dataBlockList = parseResult(cachedLines);
            rd.close();
            if(!dataBlockList.isEmpty()) {
                // update latest record and send event
                latestRecord = dataBlockList.get(dataBlockList.size() - 1);
                latestRecordTime = (long)dataBlockList.get(dataBlockList.size() - 1).getDoubleValue(0);
                eventHandler.publishEvent(new SensorDataEvent(latestRecordTime,
                        FastAcousticOutput.this, dataBlockList.toArray(new DataBlock[dataBlockList.size()])));
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
        return 5.0;
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
