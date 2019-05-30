#!/bin/bash
# Script for resetting delay of network traffic to specific downsteam service
sudo tc qdisc del dev eth0 root
