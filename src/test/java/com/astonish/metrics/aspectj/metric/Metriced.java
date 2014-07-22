/**
 * Copyright (C) 2013 Astonish Results (mcarrier@astonish.com)
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
package com.astonish.metrics.aspectj.metric;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;

public class Metriced {
    public static final String ALL = "all";
    public static final String TIMED = "timed";
    public static final String METERED = "metered";
    public static final String EXCEPTION_METERED = "exceptionmetered";

    @Timed(name = ALL + TIMED, absolute = true)
    @Metered(name = ALL + METERED, absolute = true)
    @ExceptionMetered(name = ALL + EXCEPTION_METERED, absolute = true)
    public void allTheMetrics() {
        throw new RuntimeException("burp");
    }

    @Timed(name = TIMED, absolute = true)
    public void timed() {

    }

    @Metered(name = METERED, absolute = true)
    public void metered() {

    }

    @ExceptionMetered(name = EXCEPTION_METERED, absolute = true)
    public void exceptionMetered() {
        throw new RuntimeException("burp");
    }
}
