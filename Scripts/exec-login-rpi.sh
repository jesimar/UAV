#!/bin/bash
#Author: Jesimar da Silva Arantes
#Date: 29/05/2018
#Last Update: 03/07/2018
#Description: Script that automatically login in to the Raspberry Pi 2 Companion Computer (CC) via SSH.
#Descrição: Script que faz login automático no Companion Computer (CC) Raspberry Pi 2 através de SSH.

if [ -z $1 ]
then
	#IP_RPI=192.168.0.2      #IP to network JeitoCaetano
	#IP_RPI=192.168.205.102  #IP to network LCR
	#IP_RPI=172.28.107.227   #IP to network eduroam
	#IP_RPI=192.168.0.102    #IP to network RedeDrone
	#IP_RPI=192.168.43.133   #IP to network redeDroneC (cell phone veronica)
	IP_RPI=192.168.43.8     #IP to network RedeDroneCjes (cell phone jesimar)
	#IP_RPI=10.42.0.199      #IP to network RedeDronePC
else
	IP_RPI=$1
fi

USER=pi
SENHA='raspberry'

sshpass -p $SENHA  ssh $USER@$IP_RPI
