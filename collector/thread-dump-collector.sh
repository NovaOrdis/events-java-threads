#!/bin/bash

#
# see help() below
#
version=1

#
# configuration
#

#
# the directory to write thread dumps into. If it does not exist, it will be created
#
dir=.

#
# the interval (in seconds) between successive readings
#
interval=2

#
# regular expression used to select the java process we want to take thread dumps of
#
regex="java.*something relevant about our java process"


function help() {

cat <<EOF

A script that invokes jstack periodically and collects thread dumps in a target directory.
The script expects to find JAVA_HOME set to the correct value, and it will use the jstack
utility present under that directory.

Usage:

    ./thread-dump-collector.sh --dir=<target-directory>

where

    --dir=<target-directory> specifies the directory to write the thread dump files into.
      If the directory does not exist, it will be created.

    --regex="<regex>" specifies a grep regular expression that will be used to select java
      process we want to take thread dumps of. Example:

            java.*myjar\.jar

All configuration parameters specified described are also declared in the script itself and
have the defaults mentioned in their description. If a configuration parameter is declared
on command line, that value takes precedence over the one declared in the script.


EOF

}

function java-pid() {

    local regex="$1"

    java_pid=$(ps -ef | grep "${regex}" | grep -v grep | grep -v $0 | awk '{print $2}') || exit 1
    [ -z "${java_pid}" ] && { echo "[error]: regular expression '${regex}' did not select any process" 1>&2; exit 1; }
    java_pid="$(echo ${java_pid})"
    [ "${java_pid// /}" != "${java_pid}" ] && { echo "[error]: regular expression '${regex}' selected more than one process: ${java_pid}" 1>&2; exit 1; }
    echo ${java_pid}

}

function main() {

    while [ -n "$1" ]; do

        if [ "$1" = "help" -o "$1" = "--help" -o "$1" = "-help" -o "$1" = "-?" ]; then
            help;
            exit 0;
        elif [[ "$1" =~ --dir= ]]; then
            dir=${1:6}
            [ -z "${dir}" ] && { echo "a value must follow --dir=" 1>&2; exit 1; }
            dir=${dir//\~/${HOME}}
        elif [[ "$1" =~ --regex= ]]; then
            regex=${1:8}
        fi
        shift
    done

    [ -z "${JAVA_HOME}" ] && { echo "[error]: JAVA_HOME environment variable not set" 1>&2; exit 1; }

    [ -x ${JAVA_HOME}/bin/jstack ] || { echo "[error]: ${JAVA_HOME}/bin/jstack does not exist or it is not an executable file" 1>&2; exit 1; }

    if [ ! -d ${dir} ]; then
        echo "creating ${dir} ..."
        mkdir -p ${dir} || exit 1
    fi

    while [ true ]; do

        local java_pid
        java_pid=$(java-pid "${regex}") || exit 1
        local file_name
        file_name=$(date +'%y.%m.%d-%H.%M.%S')
        file_name="${file_name}-${java_pid}.jstack"
        ${JAVA_HOME}/bin/jstack -l ${java_pid} 2>&1 > ${dir}/${file_name} || \
            { echo "[error]: failed to run ${JAVA_HOME}/bin/jstack -l ${java_pid}" 1>&2; exit 1; }
        echo -n "."
        sleep ${interval}
    done

}

main $@