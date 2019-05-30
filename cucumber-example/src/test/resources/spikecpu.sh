#!/bin/sh

# This script accept two parameters
# First Parameter = how many cores load
# Second Parameter = how much time Load before killing all

if [ $# -eq 0 ]
  then
	echo "No arguments supplied. applying default values."
	CORES=4
	WAIT_SEC=60
else
	CORES=$1
	WAIT_SEC=$2
fi

for i in `seq 1 $CORES`
do
echo "executing cpu spike on core: $i"
sudo dd if=/dev/urandom of=/dev/null &
done
sleep $WAIT_SEC
echo "done sleeping.."

