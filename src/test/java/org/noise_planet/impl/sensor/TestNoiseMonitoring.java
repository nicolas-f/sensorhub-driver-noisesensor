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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sensorhub.api.common.SensorHubException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;


public class TestNoiseMonitoring
{
    NoiseMonitoringSensor driver;
    NoiseMonitoringConfig config;
        
    @Before
    public void init() throws Exception
    {
        config = new NoiseMonitoringConfig();
        config.id = UUID.randomUUID().toString();
        
        driver = new NoiseMonitoringSensor();
        driver.init(config);
    }

    @Test
    public void testParseWeather() throws IOException {
        StringReader stringReader = new StringReader("1528201993328,52.08,24.64,58.1\n");

        List<DataBlock> data = driver.weatherDataInterface.parseResult(new BufferedReader(stringReader));
        assertEquals(1, data.size());
        assertEquals(1528201993328., data.get(0).getDoubleValue(0), 1e-2);
    }


    @Test
    public void testParseAcousticSlow() throws IOException {
        StringReader stringReader = new StringReader("1528378833,25.17,17.29\n" +
                "1528378834,34.90,28.13\n" +
                "1528378835,28.44,22.93\n");

        List<DataBlock> data = driver.slowAcousticDataInterface.parseResult(new BufferedReader(stringReader));
        assertEquals(3, data.size());
        assertEquals(1528378833, data.get(0).getDoubleValue(0), 1e-2);
        assertEquals(1528378834, data.get(1).getDoubleValue(0), 1e-2);
        assertEquals(1528378835, data.get(2).getDoubleValue(0), 1e-2);
        assertEquals(25.17, data.get(0).getDoubleValue(1), 1e-2);
        assertEquals(34.90, data.get(1).getDoubleValue(1), 1e-2);
        assertEquals(28.44, data.get(2).getDoubleValue(1), 1e-2);
        assertEquals(17.29, data.get(0).getDoubleValue(2), 1e-2);
        assertEquals(28.13, data.get(1).getDoubleValue(2), 1e-2);
        assertEquals(22.93, data.get(2).getDoubleValue(2), 1e-2);
    }

    @After
    public void cleanup()
    {
        try
        {
            driver.stop();
        }
        catch (SensorHubException e)
        {
            e.printStackTrace();
        }
    }
}
