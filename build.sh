#!/usr/bin/env bash

CURRENT_PROJECT=${PWD##*/}
CURRENT_REV="$(git rev-parse --short HEAD)"
docker build . -t "local/$CURRENT_PROJECT:$CURRENT_REV" && docker tag local/$CURRENT_PROJECT:$CURRENT_REV local/$CURRENT_PROJECT:latest
