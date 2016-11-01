/*
 * Copyright (c) 2016 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.medusa.tools;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * Created by hansolo on 01.11.16.
 */
public class MovingAverage {
    private final Queue<Data> window;
    private       int         numberPeriod;
    private       double      sum;


    // ******************** Constructors **************************************
    public MovingAverage() {
        this(10);
    }
    public MovingAverage(final int NUMBER_PERIOD) {
        assert (NUMBER_PERIOD > 0 && NUMBER_PERIOD <= 1000) : "Number period must be between 1 - 1000";
        numberPeriod = NUMBER_PERIOD;
        window       = new ConcurrentLinkedQueue<>();
    }


    // ******************** Methods *******************************************
    public void addValue(final Data DATA) {
        sum += DATA.getValue();
        window.add(DATA);
        if (window.size() > numberPeriod) {
            sum -= window.remove().getValue();
        }
    }

    public Queue<Data> getWindow() { return new LinkedList<>(window); }

    public double getAverage() {
        if (window.isEmpty()) return 0; // technically the average is undefined
        return (sum / window.size());
    }

    public double getTimeBasedAverageOf(final Duration DURATION) {
        assert !DURATION.isNegative() : "Time period must be positive";
        double average = window.stream()
                               .filter(v -> v.getTimestamp().isAfter(Instant.now().minus(DURATION)))
                               .mapToDouble(Data::getValue)
                               .average()
                               .getAsDouble();
        return average;
    }

    public void reset() { window.clear(); }
}
