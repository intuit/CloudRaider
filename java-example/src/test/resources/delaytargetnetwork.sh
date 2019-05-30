#!/bin/bash
# Script for delaying network traffic to specific downsteam service
ip=`sudo nslookup $1 | grep "Address: " | awk -F'Address: ' '{print $2}'`
sudo tc qdisc add dev eth0 root handle 1: prio
sudo tc qdisc add dev eth0 parent 1:3 handle 30: netem delay 50000ms
sudo tc filter add dev eth0 protocol ip parent 1:0 prio 3 u32 match ip dst $ip/32 flowid 1:3
