#!/usr/bin/env bash

sbt clean update compile scalafmtAll coverage test it/test coverageOff coverageReport
