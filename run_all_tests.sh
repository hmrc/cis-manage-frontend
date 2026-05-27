#!/usr/bin/env bash

sbt clean update scalafmtAll columnarFmt compile coverage test it/test coverageOff coverageReport
