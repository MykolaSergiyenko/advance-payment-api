#!/bin/sh

export JVM_FLAGS=" -server  -Duser.timezone=UTC"

exec java ${JVM_FLAGS} \
  ${JAVA_OPTS} \
  -Djava.security.egd=file:/dev/./urandom \
  -cp /app/resources/:/app/classes/:/app/libs/* \
  "online.oboz.trip.trip_carrier_advance_payment_api.MainApplication" \
  "$@"
