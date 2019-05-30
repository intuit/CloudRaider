#!/bin/bash


# Block all traffic on specific port
sudo iptables -A INPUT -p tcp -m tcp --dport $1 -j DROP
sudo iptables -A INPUT -p udp -m udp --dport $1 -j DROP