#!/bin/bash
# Script for Packet Loss

# Corrupts % of packets
sudo tc qdisc add dev eth0 root netem loss $1%