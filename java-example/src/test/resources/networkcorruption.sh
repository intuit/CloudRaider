#!/bin/bash
# Script for NetworkCorruption

# Corrupts % of packets
sudo tc qdisc add dev eth0 root netem corrupt $1%