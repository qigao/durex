#!/bin/bash
parentDir=$(cd "$(dirname "$1")" && pwd -P)/$(basename "$1")
dotEnv="$parentDir.env"
echo $dotEnv
if [ -f $dotEnv ]
then
  export $(cat $dotEnv | sed 's/#.*//g' | xargs)
fi
