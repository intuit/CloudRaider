#!/bin/bash

# Corrupts % of packets
sudo tc qdisc add dev eth0 root netem corrupt $1%