#!/bin/bash

#
# see help() below
#
version=1

#
# configuration
#

#
# the directory to write thread dumps into. If it does not exist, it will be created; the default
# behavior if not specified is to create a dated sub-directory in the current directory every
# time the script is executed.
#
dir=

#
# the interval (in seconds) between successive readings
#
interval=30

#
# regular expression used to select the java process we want to take thread dumps of
#
regex="java.*D.Standalone"


function help() {

cat <<EOF

A script that invokes jstack periodically on the java process selected by a regular expression
applied to the process list. The thread dumps are collected in the specified target directory.
The script expects to find  JAVA_HOME set to the correct value,  and it will used JAVA_HOME to
locate jstack in \$JAVA_HOME/bin.

Usage:

    ./thread-dump-collector.sh [--dir=<target-directory>] [--regex=...] [--interval=<secs>]

where

    --dir=<target-directory> specifies the directory to write the thread dump files into.
      If the directory does not exist, it will be created. If the configuration option is
      not explicitly specified,  the default behavior is to create  a dated sub-directory
      in the current directory every time the script is executed.

    --regex="<regex>" specifies a grep regular expression that will be used to select java
      process we want to take thread dumps of. Example:

            java.*myjar\.jar

    --interval=<interval-in-secs> the interval between successive readings, in seconds.
      The default value is 30 seconds.

All configuration parameters specified described are also declared in the script itself and
have the defaults mentioned in their description. If a configuration parameter is declared
on command line, that value takes precedence over the one declared in the script.

Auxiliary commands:

    help - this help

    pid - displays the PID that will be used by jstack after applying the regular expression
        on the process list. Useful to test the regular expression.

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

function create-directory-if-does-not-exist() {

    local dir=$1

    if [ "${dir}" = "" ]; then
        dir="./"$(date +'%y.%m.%d-%H.%M.%S')"-thread-dumps"
    fi

    if [ ! -d ${dir} ]; then
        echo "creating ${dir} ..." 1>&2
        mkdir -p ${dir} || exit 1
    fi

    echo "${dir}"
}

function main() {

    local command

    while [ -n "$1" ]; do

        if [ "$1" = "help" -o "$1" = "--help" -o "$1" = "-help" -o "$1" = "-?" ]; then
            command="help"
        elif [[ "$1" =~ --dir= ]]; then
            dir=${1:6}
            [ -z "${dir}" ] && { echo "a value must follow --dir=" 1>&2; exit 1; }
            dir=${dir//\~/${HOME}}
        elif [[ "$1" =~ --regex= ]]; then
            regex=${1:8}
        elif [[ "$1" =~ --interval= ]]; then
            interval=${1:11}
        elif [ "$1" = "pid" ]; then
            command="pid"
        fi
        shift
    done

    if [ "${command}" = "help" ]; then
        help
        return 0
    elif [ "${command}" = "pid" ]; then
        java-pid "${regex}"
        return 0
    fi

    [ -z "${JAVA_HOME}" ] && { echo "[error]: JAVA_HOME environment variable not set" 1>&2; exit 1; }

    [ -x ${JAVA_HOME}/bin/jstack ] || { echo "[error]: ${JAVA_HOME}/bin/jstack does not exist or it is not an executable file" 1>&2; exit 1; }

    dir=$(create-directory-if-does-not-exist "${dir}") || exit 1

    echo ${dir}

    exit 0

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