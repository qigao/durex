#!/bin/bash
while getopts h:p: flag
do
  case "${flag}" in
    h) host=${OPTARG};;
    p) port=${OPTARG};;
  esac
done
until nc  -z -v -w30 $host $port; do
  sleep 5
  echo "Waiting for DB to come up..."
done
