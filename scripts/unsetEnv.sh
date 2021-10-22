#!/bin/bash
parentDir=$(cd "$(dirname "$1")" && pwd -P)/$(basename "$1")
dotEnv="$parentDir.env"
echo $dotEnv
if [ -f $dotEnv ]
then
  unset $(grep -v '^#' $dotEnv | sed -E 's/(.*)=.*/\1/' | xargs)
fi
