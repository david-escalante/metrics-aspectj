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
package com.astonish.metrics.aspectj;

import static com.codahale.metrics.MetricRegistry.name;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer.Context;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;

/**
 * Aspect that provides appropriate around-advice for any metric-annotated method.
 */
@Aspect
public abstract class MetricAdvice {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricAdvice.class);

    /**
     * Provides the scope in which to weave the around-advice.
     */
    @Pointcut("")
    public abstract void scope();

    /**
     * Any method annotated by {@link Timed}.
     */
    @Pointcut("execution(@com.codahale.metrics.annotation.Timed * *.*(..))")
    public void timedMethod() {
    }

    /**
     * Any method annotated by {@link Metered}.
     */
    @Pointcut("execution(@com.codahale.metrics.annotation.Metered * *.*(..))")
    public void meteredMethod() {
    }

    /**
     * Any method annotated by {@link ExceptionMetered}.
     */
    @Pointcut("execution(@com.codahale.metrics.annotation.ExceptionMetered * *.*(..))")
    public void exceptionMeteredMethod() {
    }

    /**
     * Around advice for any metric-annotated {@link Method}.
     * @param pjp
     *            the {@link ProceedingJoinPoint}
     * @return the {@link Method} return object
     * @throws Throwable
     *             if any exception occurs
     */
    @Around("scope() && (timedMethod() || meteredMethod() || exceptionMeteredMethod())")
    public final Object aroundMetricAnnotatedMethod(ProceedingJoinPoint pjp) throws Throwable {
        final Method method = retrieveMethod(pjp);

        handleMeteredMethod(method);
        final ExceptionMeter exceptionMeter = handleExceptionMeteredMethod(method);
        final Context timerCtxt = handleTimedMethod(method);
        try {
            return pjp.proceed();
        } catch (Throwable t) {
//@formatter:off
            if ((null != exceptionMeter)
                    && (exceptionMeter.getExceptionClass().isAssignableFrom(t.getClass())
                            || (t.getCause() != null
                            && exceptionMeter.getExceptionClass().isAssignableFrom(t.getCause().getClass())))) {
                exceptionMeter.getMeter().mark();
            }
//@formatter:on

            throw t;
        } finally {
            if (null != timerCtxt) {
                timerCtxt.stop();
            }
        }
    }

    /**
     * Checks if method is timed, and if so returns the registered timer or creates a new one and registers it.
     * @param method
     *            the {@link Method}
     * @return the {@link Context} or null if method is not timed
     */
    private Context handleTimedMethod(Method method) {
        final Timed anno = method.getAnnotation(Timed.class);
        if (null != anno) {
            return MetricRegistryStore.getInstance().getRegistry()
                    .timer(chooseName(anno.name(), anno.absolute(), method, "timed")).time();
        }

        return null;
    }

    /**
     * Checks if method is metered, and if so marks the registered meter or creates a new one, registers it, and marks
     * it.
     * @param method
     *            the {@link Method}
     */
    private void handleMeteredMethod(Method method) {
        final Metered anno = method.getAnnotation(Metered.class);
        if (null != anno) {
            MetricRegistryStore.getInstance().getRegistry()
                    .meter(chooseName(anno.name(), anno.absolute(), method, "metered")).mark();
        }
    }

    /**
     * Checks if method is exception metered, and if so returns the registered meter or creates a new one and registers
     * it.
     * @param method
     *            the {@link Method}
     * @return the {@link ExceptionMeter} or null if method is not exception metered
     */
    private ExceptionMeter handleExceptionMeteredMethod(Method method) {
        final ExceptionMetered anno = method.getAnnotation(ExceptionMetered.class);
        if (null != anno) {
            return new ExceptionMeter(MetricRegistryStore.getInstance().getRegistry()
                    .meter(chooseName(anno.name(), anno.absolute(), method, anno.cause().getSimpleName())),
                    anno.cause());
        }

        return null;
    }

    /**
     * Retrieves the {@link Method} from the {@link ProceedingJoinPoint}.
     * @param pjp
     *            the {@link ProceedingJoinPoint}
     * @return the {@link Method}
     */
    private Method retrieveMethod(ProceedingJoinPoint pjp) {
        final MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        Method method = methodSignature.getMethod();

        if (method.getDeclaringClass().isInterface()) {
            try {
                method = pjp.getTarget().getClass()
                        .getDeclaredMethod(pjp.getSignature().getName(), method.getParameterTypes());
            } catch (final SecurityException e) {
                LOGGER.warn("Could not retrieve method[{}] for metric interrogation.", method.getName(), e);
            } catch (final NoSuchMethodException e) {
                LOGGER.warn("Could not retrieve method[{}] for metric interrogation.", method.getName(), e);
            }
        }

        return method;
    }

    /**
     * Derived from chooseName function in metrics-jersey.
     * @param explicitName
     *            the explicit name
     * @param absolute
     *            whether the name is absolute
     * @param method
     *            the target {@link Method}
     * @param suffixes
     *            the suffixes to add
     * @return the name of the metric
     */
    private String chooseName(String explicitName, boolean absolute, Method method, String... suffixes) {
        if (explicitName != null && !explicitName.isEmpty()) {
            if (absolute) {
                return explicitName;
            }
            return name(method.getDeclaringClass(), explicitName);
        }
        return name(name(method.getDeclaringClass(), method.getName()), suffixes);
    }
}

/**
 * Encapsulates a {@link Meter} and an exception class.
 */
class ExceptionMeter {
    private final Meter meter;
    private final Class<? extends Throwable> exceptionClass;

    /**
     * @param meter
     *            the {@link Meter}
     * @param exceptionClass
     *            the exception class
     */
    ExceptionMeter(Meter meter, Class<? extends Throwable> exceptionClass) {
        this.meter = meter;
        this.exceptionClass = exceptionClass;
    }

    /**
     * @return the meter
     */
    protected Meter getMeter() {
        return meter;
    }

    /**
     * @return the exceptionClass
     */
    protected Class<? extends Throwable> getExceptionClass() {
        return exceptionClass;
    }
}
