#!/bin/bash

./gradlew jmhJar -Porm=5.6
java -jar basic/target/libs/hibernate-orm-benchmark-basic-1.0-SNAPSHOT-jmh.jar QueryEntityLazyInitCollectionLoop -f 2 -prof gc -prof "async:rawCommand=alloc,wall;event=cpu;output=jfr;dir=/tmp;libPath=${ASYNC_PROFILER_HOME}/lib/libasyncProfiler.so" -pcount=100

java -cp ${ASYNC_PROFILER_HOME}/lib/converter.jar jfr2flame --alloc --total /tmp/org.hibernate.benchmark.queryl1hit.QueryEntityLazyInitCollectionLoop.single-Throughput/jfr-cpu.jfr alloc5.html
java -cp ${ASYNC_PROFILER_HOME}/lib/converter.jar jfr2flame --state default /tmp/org.hibernate.benchmark.queryl1hit.QueryEntityLazyInitCollectionLoop.single-Throughput/jfr-cpu.jfr cpu5.html
java -cp ${ASYNC_PROFILER_HOME}/lib/converter.jar jfr2flame --state runnable,sleeping /tmp/org.hibernate.benchmark.queryl1hit.QueryEntityLazyInitCollectionLoop.single-Throughput/jfr-cpu.jfr wall5.html