#!/bin/bash

# Adds {some}ms +- {some}ms of latency to each packet
#tc qdisc add dev eth0 root latency delay $1ms $2ms
sudo tc qdisc add dev eth0 root netem delay $1ms $2ms