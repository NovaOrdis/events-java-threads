#!/bin/bash

. `dirname $0`/split.sh

expressions="pool- HornetQ-client-global-threads HornetQ-remoting-threads-HornetQServerImpl OOB- FD_SOCK Incoming- Timer- Thread- JBoss RMI New ajp- http- WorkManager Dispatch ConnectionTable main-FastReceiver RequestController-"

tdfile=$1

split "${tdfile}" "${expressions}"