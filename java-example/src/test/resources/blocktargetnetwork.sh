#!/bin/bash
# Script for blocking traffic to specific downstream service
sudo iptables -A OUTPUT -p tcp -d $1 -j DROP
