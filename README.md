metrics-aspectj [![Build Status](https://travis-ci.org/mattcarrier/metrics-aspectj.png)](https://travis-ci.org/mattcarrier/metrics-aspectj)
===============

About
--------
Extremely small wrapper library that provides an aspect-oriented method for implanting [codahale/metrics](http://github.com/codahale/metrics "Metrics") based on [aspectj](http://eclipse.org/aspectj "AspectJ"). The library surrounds all annotated methods with a lightweight around advice.

Usage
---------
Extend MetricAdvice to provide an aspect scope:

    @Aspect
    public class MetricAdviceImpl extends MetricAdvice {
        @Pointcut("within(org.mattcarrier.metrics.aspectj..*)")
        public void scope() {
        }
    }

Annotate methods to add metrics:

    ...
    @Timed
    @Metered
    @ExceptionMetered
    public void allTheMetrics() {
        throw new RuntimeException("burp");
    }
    ...

Weave the aspects (example is of compile-time weave using maven):

    ...
    <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>aspectj-maven-plugin</artifactId>
        <version>1.4</version>
        <configuration>
            <showWeaveInfo>true</showWeaveInfo>
            <source>${compile.version}</source>
            <target>${compile.version}</target>
            <Xlint>ignore</Xlint>
            <complianceLevel>${compile.version}</complianceLevel>
            <encoding>UTF-8</encoding>
            <verbose>false</verbose>
        </configuration>
        <executions>
            <execution>
                <goals>
                    <goal>compile</goal>
                    <goal>test-compile</goal>
                </goals>
            </execution>
        </executions>
        <dependencies>
            <dependency>
                <groupId>org.aspectj</groupId>
                <artifactId>aspectjrt</artifactId>
                <version>${aspectj.version}</version>
            </dependency>
            <dependency>
                <groupId>org.aspectj</groupId>
                <artifactId>aspectjtools</artifactId>
                <version>${aspectj.version}</version>
            </dependency>
        </dependencies>
    </plugin>
    ...

License
-----------
Apache Software License 2.0<br>
Copyright (c) 2013 Matt Carrier
