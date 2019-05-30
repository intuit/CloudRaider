#! /bin/bash

# this script will execute with two paramters 
# first parameter will taking size 
# second parameter will take time


if [ $# -eq 0 ]
  then
	echo "No arguments supplied. applying default values."
	GBs=${2-10} #default: create 10GB file
	RUN_FOR_SEC=${1-300} #default: run 300 seconds
else
	GBs=$1 #default: create 10GB file
	RUN_FOR_SEC=$2 #default: run 300 seconds
fi

TEMP_FILE="remove_me"

while [ $SECONDS -lt $RUN_FOR_SEC ]; do
    dd if=/dev/zero of=$TEMP_FILE bs=1M count=$((1000*GBs)) oflag=direct
    echo "$SECONDS seconds elapsed"
    :
done

rm $TEMP_FILE
