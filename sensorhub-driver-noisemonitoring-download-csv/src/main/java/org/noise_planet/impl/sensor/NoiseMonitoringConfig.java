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

import org.sensorhub.api.config.DisplayInfo;
import org.sensorhub.api.config.DisplayInfo.Required;
import org.sensorhub.api.sensor.PositionConfig.LLALocation;
import org.sensorhub.api.sensor.SensorConfig;


public class NoiseMonitoringConfig extends SensorConfig
{
    
    @Required
    @DisplayInfo(desc="Serial number of the station used to generate its unique ID")
    public String serialNumber = "B8-27-EB-74-CD-A8";

    @DisplayInfo(desc = "Connection timeout to fetch the data in ms")
    @DisplayInfo.ValueRange(min = 1)
    public int httpTimeout = 5000;
    
    @DisplayInfo(desc="Station Location")
    public LLALocation location = new LLALocation();

    @DisplayInfo(label="Http weather server url", desc = "Pull CSV records from this url")
    public String httpWeatherStationUrl = "http://127.0.0.1:8085/weather";

    @DisplayInfo(label="Http fast acoustic server url", desc = "Pull CSV records from this url")
    public String httpFastAcousticStationUrl = "http://127.0.0.1:8090/fast";

    @DisplayInfo(label="Http slow acoustic server url", desc = "Pull CSV records from this url")
    public String httpSlowAcousticStationUrl = "http://127.0.0.1:8090/slow";

    @DisplayInfo(label="Http samples acoustic server url", desc = "Pull CSV records from this url")
    public String httpSamplesAcousticStationUrl = "http://127.0.0.1:8090/samples";

    @DisplayInfo(label="Http node state server url", desc = "Pull CSV records from this url")
    public String httpStateStationUrl = "";

    @DisplayInfo(label = "How many 125ms results to embed into one record")
    @DisplayInfo.ValueRange(min = 1)
    public int fastValuesPerDataRecord = 80;

    @DisplayInfo(label = "How many 1s results to embed into one record")
    @DisplayInfo.ValueRange(min = 1)
    public int slowValuesPerDataRecord = 10;

    public String getHttpWeatherStationUrl() {
        return httpWeatherStationUrl;
    }

    public String getHttpFastAcousticStationUrl() {
        return httpFastAcousticStationUrl;
    }

    public String getHttpSlowAcousticStationUrl() {
        return httpSlowAcousticStationUrl;
    }

    public String getHttpStateStationUrl() {
        return httpStateStationUrl;
    }

    public String getHttpSamplesAcousticStationUrl() {
        return httpSamplesAcousticStationUrl;
    }

    public NoiseMonitoringConfig()
    {
        location.lat = 34.8038;        
        location.lon = -86.7228;      
        location.alt = 0.000;
    }

    
    @Override
    public LLALocation getLocation()
    {
        return location;
    }
}
