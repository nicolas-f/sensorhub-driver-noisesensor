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
import java.util.ArrayList;
import java.util.Arrays;
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
        config.httpFastAcousticStationUrl = "";
        config.httpSlowAcousticStationUrl = "";
        config.httpWeatherStationUrl = "";
        config.id = UUID.randomUUID().toString();
        
        driver = new NoiseMonitoringSensor();
        driver.init(config);
    }

    @Test
    public void testParseAcousticFast() throws IOException {
        // Columns:
        // timestamp, leq, laeq, leq 20 Hz, leq 25 Hz, ...
        final String dataStr = "1528448319.844,5.39,-2.56,-8.39,-10.37,-6.58,-8.92,-9.53,-13.65,-15.84,-14.94,-15.69,-12.82,-13.67,-6.18,-8.08,-17.82,-17.79,-16.94,-18.00,-18.53,-20.00,-18.04,-18.38,-18.54,-17.84,-16.38,-15.44,-14.39,-13.83,-13.45,-12.27\n" +
                "1528448319.969,6.22,-1.20,-11.01,-13.61,-7.35,-7.79,-8.77,-10.69,-11.15,-14.03,-14.92,-13.64,-16.38,-10.21,-11.09,-17.53,-18.47,-15.55,-17.66,-18.30,-17.30,-17.15,-17.27,-15.31,-15.78,-15.62,-14.29,-14.25,-13.28,-12.64,-11.67\n" +
                "1528448320.094,6.29,-1.37,-9.33,-9.92,-8.04,-6.39,-6.78,-9.16,-10.75,-14.97,-16.50,-12.35,-18.36,-6.25,-9.48,-19.16,-17.87,-16.54,-17.05,-19.48,-18.13,-18.08,-17.59,-17.52,-16.60,-16.65,-15.27,-14.57,-13.45,-12.57,-11.66\n" +
                "1528448320.219,6.94,0.98,-3.37,-9.54,-3.91,-9.13,-7.25,-12.30,-14.59,-19.00,-16.08,-15.88,-16.74,-9.48,-9.27,-15.14,-14.00,-15.97,-14.09,-16.84,-13.12,-15.58,-17.39,-15.21,-16.38,-15.25,-14.96,-14.71,-13.47,-12.82,-11.93\n" +
                "1528448320.344,5.04,-1.71,-7.67,-10.30,-4.61,-15.37,-10.21,-12.94,-15.30,-19.10,-17.92,-14.04,-15.65,-8.72,-10.62,-18.74,-15.84,-17.84,-15.88,-16.82,-15.70,-17.91,-17.20,-14.80,-15.03,-12.38,-14.47,-13.58,-13.71,-12.63,-11.34\n" +
                "1528448320.469,7.79,2.33,-4.37,-3.39,-8.96,-1.13,-4.23,-5.65,-15.45,-14.11,-15.72,-11.99,-11.01,-5.17,-8.41,-15.48,-15.12,-12.95,-9.33,-12.52,-11.80,-8.38,-9.86,-8.35,-11.08,-11.03,-12.77,-12.51,-13.11,-11.60,-10.34\n" +
                "1528448320.594,14.50,8.13,3.99,6.88,10.86,-0.12,-5.59,-3.15,-5.67,-10.87,-12.65,-16.58,-15.08,-8.85,-11.33,-19.80,-16.49,-17.53,-14.74,-14.64,-16.01,-15.46,-17.41,-16.93,-16.15,-16.29,-15.33,-14.59,-13.93,-12.89,-11.70\n" +
                "1528448320.719,11.83,7.46,1.32,4.02,9.32,-3.58,-9.77,-10.44,-15.88,-18.90,-15.37,-16.49,-15.30,-8.05,-10.10,-15.85,-17.05,-15.65,-16.66,-18.33,-16.72,-18.17,-17.69,-17.69,-16.72,-16.44,-15.93,-14.21,-13.64,-12.77,-12.10\n" +
                "1528448320.844,7.82,0.32,-4.42,-3.70,-4.20,-7.12,-9.31,-7.50,-13.02,-16.12,-10.22,-14.64,-14.39,-7.48,-9.73,-16.43,-17.78,-17.82,-17.38,-19.41,-18.07,-17.50,-18.49,-16.43,-15.25,-15.48,-14.62,-14.56,-13.71,-12.38,-12.06\n" +
                "1528448320.970,4.40,-2.75,-11.35,-10.46,-10.40,-8.27,-14.47,-11.24,-15.45,-16.20,-10.49,-18.12,-16.95,-9.73,-9.38,-18.09,-17.99,-18.27,-17.47,-19.10,-17.90,-16.97,-17.72,-16.44,-15.67,-15.25,-15.37,-14.32,-13.49,-12.97,-11.94\n";
        List<String> fastResults = new ArrayList<>(Arrays.asList(dataStr.split("\n")));
        assertEquals(10, fastResults.size());
        List<DataBlock> data = driver.fastAcousticDataInterface.parseResult(fastResults);
        assertEquals(1, data.size());
        assertEquals(2, fastResults.size());
        DataBlock dataBlock = data.get(0);
        // Time
        assertEquals(1528448319.844, dataBlock.getDoubleValue(0), 1e-2);
        // leq first 125 ms
        assertEquals(5.39, dataBlock.getDoubleValue(1), 1e-2);
        // leq second 125 ms
        assertEquals(6.22, dataBlock.getDoubleValue(2), 1e-2);
        // laeq first 125 ms
        assertEquals(-2.56, dataBlock.getDoubleValue(FastAcousticOutput.FAST_COUNT_IN_DATARECORD + 1), 1e-2);
        // laeq third 125 ms
        assertEquals(-1.37, dataBlock.getDoubleValue(FastAcousticOutput.FAST_COUNT_IN_DATARECORD + 3), 1e-2);
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
        String res = "1531405786,47.27,40.60\n" +
                "1531405787,47.46,40.76\n" +
                "1531405788,53.38,46.99\n" +
                "1531405789,58.53,50.88\n" +
                "1531405790,53.53,45.19\n" +
                "1531405791,55.97,48.74\n" +
                "1531405792,50.11,42.70\n" +
                "1531405793,47.38,40.96\n" +
                "1531405794,47.51,41.01\n" +
                "1531405795,46.69,40.48\n" +
                "1531405796,46.67,40.49\n" +
                "1531405797,46.70,40.36\n" +
                "1531405798,48.23,42.68\n" +
                "1531405799,47.17,40.77\n" +
                "1531405800,46.55,40.13\n" +
                "1531405801,46.28,39.99\n" +
                "1531405802,46.01,39.47\n" +
                "1531405803,46.57,39.99\n" +
                "1531405804,52.71,48.08\n" +
                "1531405805,55.27,47.53\n" +
                "1531405806,46.55,40.00\n" +
                "1531405807,46.17,39.70\n" +
                "1531405808,45.46,39.35\n" +
                "1531405809,46.24,39.71\n" +
                "1531405810,46.49,40.03\n" +
                "1531405811,47.45,42.42\n" +
                "1531405812,46.25,39.91\n" +
                "1531405813,46.42,39.92\n" +
                "1531405814,46.68,40.10\n" +
                "1531405815,46.11,39.99\n" +
                "1531405816,57.56,50.65\n" +
                "1531405817,46.58,40.01\n" +
                "1531405818,46.08,39.55\n" +
                "1531405819,47.00,40.42\n" +
                "1531405820,47.65,40.72\n" +
                "1531405821,48.42,41.46\n" +
                "1531405822,51.48,44.02\n" +
                "1531405823,48.02,41.10\n" +
                "1531405824,48.76,42.05\n" +
                "1531405825,52.15,45.77\n" +
                "1531405826,52.46,47.08\n" +
                "1531405827,53.03,47.85\n" +
                "1531405828,49.32,42.52\n" +
                "1531405829,51.81,45.97\n" +
                "1531405830,52.71,47.51\n" +
                "1531405831,50.41,44.65\n" +
                "1531405832,52.08,46.41";
        List<String> slowResults = new ArrayList<>(Arrays.asList(res.split("\n")));
        assertEquals(47, slowResults.size());
        List<DataBlock> data = driver.slowAcousticDataInterface.parseResult(slowResults);
        assertEquals(47 - SlowAcousticOutput.SLOW_COUNT_IN_DATARECORD, slowResults.size());
        assertEquals(1, data.size());
        DataBlock dataBlock = data.get(0);
        assertEquals(1531405786, dataBlock.getDoubleValue(0), 1e-2);
        assertEquals(47.27, dataBlock.getDoubleValue(1), 1e-2);
        assertEquals(47.46, dataBlock.getDoubleValue(2), 1e-2);
        assertEquals(53.38, dataBlock.getDoubleValue(3), 1e-2);
        assertEquals(40.60, dataBlock.getDoubleValue(SlowAcousticOutput.SLOW_COUNT_IN_DATARECORD + 1), 1e-2);
        assertEquals(40.76, dataBlock.getDoubleValue(SlowAcousticOutput.SLOW_COUNT_IN_DATARECORD + 2), 1e-2);
        assertEquals(46.99, dataBlock.getDoubleValue(SlowAcousticOutput.SLOW_COUNT_IN_DATARECORD + 3), 1e-2);
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
