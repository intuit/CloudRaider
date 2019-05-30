#!/bin/bash

# Block traffic incomoing/outgoing for a given domain
sudo iptables -A INPUT -p tcp -d $1 -j DROP
sudo iptables -A INPUT -p udp -d $1 -j DROP

sudo iptables -A OUTPUT -p tcp -d $1 -j DROP
sudo iptables -A OUTPUT -p udp -d $1 -j DROP
