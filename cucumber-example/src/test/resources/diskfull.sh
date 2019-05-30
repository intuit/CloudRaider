#! /bin/bash

# this script will execute with two paramters 
# first parameter will take the path (root or app)
# second parameter will take the size


if [ $# -eq 0 ]
  then
	echo "No arguments supplied. applying default values."
	GBs=${2-10} #default: create 10GB file
else
	GBs=$2 #default: create 10GB file
fi

TEMP_FILE="remove_me"

sudo dd if=/dev/zero of=/$1/$TEMP_FILE bs=1M count=$((1000*GBs)) oflag=direct

