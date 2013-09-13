/**
 * Copyright (C) 2013 Matthew R Carrier (mcarrieruri@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mattcarrier.metrics.aspectj;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mattcarrier.metrics.aspectj.MetricRegistryStore;
import org.mattcarrier.metrics.aspectj.metric.Metriced;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

public class MetricAdviceTest {
    private Metriced metriced;

    @BeforeClass
    public static void setMetricRegistry() {
        MetricRegistryStore.getInstance().setRegistry(new MetricRegistry());
    }

    @Before
    public void setup() {
        metriced = new Metriced();
    }

    @After
    public void tearDown() {
        MetricRegistryStore.getInstance().getRegistry().remove(Metriced.ALL + Metriced.EXCEPTION_METERED);
        MetricRegistryStore.getInstance().getRegistry().remove(Metriced.ALL + Metriced.METERED);
        MetricRegistryStore.getInstance().getRegistry().remove(Metriced.ALL + Metriced.TIMED);
        MetricRegistryStore.getInstance().getRegistry().remove(Metriced.EXCEPTION_METERED);
        MetricRegistryStore.getInstance().getRegistry().remove(Metriced.METERED);
        MetricRegistryStore.getInstance().getRegistry().remove(Metriced.TIMED);
    }

    @Test
    public void timed() throws InterruptedException {
        metriced.timed();
        final Timer timer = MetricRegistryStore.getInstance().getRegistry().getTimers().get(Metriced.TIMED);
        assertEquals(1, timer.getCount());

        assertNull(MetricRegistryStore.getInstance().getRegistry().getMeters()
                .get(Metriced.ALL + Metriced.EXCEPTION_METERED));
        assertNull(MetricRegistryStore.getInstance().getRegistry().getMeters().get(Metriced.ALL + Metriced.METERED));
        assertNull(MetricRegistryStore.getInstance().getRegistry().getTimers().get(Metriced.ALL + Metriced.TIMED));
        assertNull(MetricRegistryStore.getInstance().getRegistry().getMeters().get(Metriced.EXCEPTION_METERED));
        assertNull(MetricRegistryStore.getInstance().getRegistry().getMeters().get(Metriced.METERED));
    }

    @Test
    public void metered() {
        metriced.metered();
        final Meter meter = MetricRegistryStore.getInstance().getRegistry().getMeters().get(Metriced.METERED);
        assertEquals(1, meter.getCount());

        assertNull(MetricRegistryStore.getInstance().getRegistry().getMeters()
                .get(Metriced.ALL + Metriced.EXCEPTION_METERED));
        assertNull(MetricRegistryStore.getInstance().getRegistry().getMeters().get(Metriced.ALL + Metriced.METERED));
        assertNull(MetricRegistryStore.getInstance().getRegistry().getTimers().get(Metriced.ALL + Metriced.TIMED));
        assertNull(MetricRegistryStore.getInstance().getRegistry().getMeters().get(Metriced.EXCEPTION_METERED));
        assertNull(MetricRegistryStore.getInstance().getRegistry().getTimers().get(Metriced.TIMED));
    }

    @Test
    public void exceptionMetered() {
        try {
            metriced.exceptionMetered();
            fail("exception should have been thrown");
        } catch (RuntimeException e) {
            // supposed to happen
        }

        final Meter meter = MetricRegistryStore.getInstance().getRegistry().getMeters().get(Metriced.EXCEPTION_METERED);
        assertEquals(1, meter.getCount());

        assertNull(MetricRegistryStore.getInstance().getRegistry().getMeters()
                .get(Metriced.ALL + Metriced.EXCEPTION_METERED));
        assertNull(MetricRegistryStore.getInstance().getRegistry().getMeters().get(Metriced.ALL + Metriced.METERED));
        assertNull(MetricRegistryStore.getInstance().getRegistry().getTimers().get(Metriced.ALL + Metriced.TIMED));
        assertNull(MetricRegistryStore.getInstance().getRegistry().getMeters().get(Metriced.METERED));
        assertNull(MetricRegistryStore.getInstance().getRegistry().getTimers().get(Metriced.TIMED));
    }

    @Test
    public void allMetrics() {
        try {
            metriced.allTheMetrics();
            fail("exception should have been thrown");
        } catch (RuntimeException e) {
            // supposed to happen
        }

        final Meter exceptionMeter = MetricRegistryStore.getInstance().getRegistry().getMeters()
                .get(Metriced.ALL + Metriced.EXCEPTION_METERED);
        final Meter meter = MetricRegistryStore.getInstance().getRegistry().getMeters()
                .get(Metriced.ALL + Metriced.METERED);
        final Timer timer = MetricRegistryStore.getInstance().getRegistry().getTimers()
                .get(Metriced.ALL + Metriced.TIMED);
        assertEquals(1, meter.getCount());
        assertEquals(1, exceptionMeter.getCount());
        assertEquals(1, timer.getCount());

        assertNull(MetricRegistryStore.getInstance().getRegistry().getMeters().get(Metriced.EXCEPTION_METERED));
        assertNull(MetricRegistryStore.getInstance().getRegistry().getMeters().get(Metriced.METERED));
        assertNull(MetricRegistryStore.getInstance().getRegistry().getTimers().get(Metriced.TIMED));
    }
}
