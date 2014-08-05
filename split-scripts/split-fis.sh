#!/bin/bash

. `dirname $0`/split.sh

expressions="Camel pool- ajp- Thread- http- Timer- Incoming-  OOB- WorkerThread FD_SOCK RMI SessionManager JBoss RequestController ConnectionTable main-FastReceiver SnmpPortal ServerSocketRefresh Messaging control JMX"

tdfile=$1

split "${tdfile}" "${expressions}"