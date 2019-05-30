#!/bin/bash


# Block traffic incomoing/outgoing for a given domain
sudo iptables -D INPUT -p tcp -d $1 -j DROP
sudo iptables -D INPUT -p udp -d $1 -j DROP

sudo iptables -D OUTPUT -p tcp -d $1 -j DROP
sudo iptables -D OUTPUT -p udp -d $1 -j DROP
