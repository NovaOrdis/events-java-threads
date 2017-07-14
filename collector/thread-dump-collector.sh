#!/bin/bash

#
# see help() below
#
VERSION=2

#
# configuration
#

#
# the directory to write thread dumps into. If it does not exist, it will be created; the default
# behavior if not specified is to create a dated sub-directory in the current directory every
# time the script is executed. Overridden by the command line argument --dir=...
#
dir=

#
# the interval (in seconds) between successive readings
#
interval=30

#
# regular expression used to select the java process we want to take thread dumps of. Overridden by
# command line argument --regex=...
#
regex="java.*D.Standalone"

VERBOSE=false


function help() {

cat <<EOF

A script that invokes jstack and top periodically on the java process selected by a regular
expression applied to the process list. Thread dump outputs and top outputs are collected in
two different files, created the specified target directory. If the java process is restarted
and the PID changes, new files will be created, but otherwise results of successive readings
will accumulate in the same files. The script expects to find  JAVA_HOME set to the correct
value, and it will used JAVA_HOME to locate jstack in \$JAVA_HOME/bin. However, the script can
be configured to use a custom Java home directory with --java-home=...

WARNING: A failure to take a thread dump on the Java process does not interrupt the main loop.
Failure details are logged in the thread dump file instead. For this reason, it is best if the
thread dump file content is checked before launching this program in background and leaving it
unattended.

Usage:

    ./thread-dump-collector.sh [--dir=<target-directory>] [--regex=...] [--interval=<secs>]

where:

    --dir=<target-directory> specifies the directory to write the thread dump files into.
      If the directory does not exist, it will be created. If the configuration option is
      not explicitly specified, the default behavior is to create a dated sub-directory in
      the current directory every time the script is executed.

    --regex="<regex>" specifies a grep regular expression that will be used to select java
      process we want to take thread dumps of. Example:

            java.*myjar\.jar

    --interval=<interval-in-secs> the interval between successive readings, in seconds.
      The default value is 30 seconds.

    --java-home=<java-home-directory> a directory to be used as JAVA_HOME, thus ignoring the
      value of JAVA_HOME, if exists.

    -v, --verbose the script will generate debug logging.

All configuration parameters specified described are also declared in the script itself and
have the defaults mentioned in their description. If a configuration parameter is declared
on command line, that value takes precedence over the one declared in the script.

Auxiliary commands:

    help - this help

    pid - displays the PID that will be used by jstack after applying the regular expression
        on the process list. Useful to test the regular expression.

EOF
}

function debug() {

    ${VERBOSE} && echo "$@" 1>&2;
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

#
# the function MUST not exit, as we want to run it in a loop. However, it should return 0 on success or non-zero on
# failure. If the collection commands fail, the jstack-and-top-snapshot() execution will be considered a success,
# but the failure output will also be redirected in the data files.
#
function jstack-and-top-snapshot() {

    debug "executing jstack-and-top-snapshot()"

    local pid=$1
    local output_filename_prefix=$2
    local output_dir=$3
    local java_home=$4

    [ -z "${pid}" ] && { echo "jstack-and-top-snapshot(): pid not provided" 1>&2; return 1; }
    [ -z "${output_filename_prefix}" ] && { echo "jstack-and-top-snapshot(): output_filename_prefix not provided" 1>&2; return 1; }
    [ -z "${output_dir}" ] && { echo "jstack-and-top-snapshot(): output_dir not provided" 1>&2; return 1; }
    [ -d ${output_dir} ] || { echo "jstack-and-top-snapshot(): output_dir ${output_dir} not a valid directory" 1>&2; return 1; }
    [ -z "${java_home}" ] && { echo "jstack-and-top-snapshot(): java_home not provided" 1>&2; return 1; }

    local timestamp=$(date)

    local thread_dump_file_name="${output_filename_prefix}-jstack.out"
    local top_output_file_name="${output_filename_prefix}-top.out"

    echo "${timestamp}" >> ${output_dir}/${thread_dump_file_name}

    debug "executing ${java_home}/bin/jstack -l ${pid} 2>&1 >> ${output_dir}/${thread_dump_file_name}"

    local residual_output
    residual_output=$(${java_home}/bin/jstack -l ${pid} 2>&1 >> ${output_dir}/${thread_dump_file_name})
    [ -n "${residual_output}" ] && echo "${residual_output}" >>  ${output_dir}/${thread_dump_file_name}

    echo "${timestamp}" >> ${output_dir}/${top_output_file_name}
    residual_output=$(top -b -n 1 -H -p ${pid} 2>&1 >> ${output_dir}/${top_output_file_name})
    [ -n "${residual_output}" ] && echo "${residual_output}" >>  ${output_dir}/${top_output_file_name}

    return 0
}

function main() {

    local command
    local java_home

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
        elif [[ "$1" =~ --java-home= ]]; then
            java_home=${1:12}
        elif [ "$1" = "-v" -o "$1" = "--verbose" ]; then
            VERBOSE=true
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

    [ -z "${java_home}" ] && java_home=${JAVA_HOME}

    [ -z "${java_home}" ] && { echo "[error]: JAVA_HOME environment variable not set and no --java-home= command line argument was used" 1>&2; exit 1; }

    [ -x ${java_home}/bin/jstack ] || { echo "[error]: ${java_home}/bin/jstack does not exist or it is not an executable file" 1>&2; exit 1; }

    dir=$(create-directory-if-does-not-exist "${dir}") || exit 1

    local current_pid
    local output_filename_prefix
    first=true

    while [ true ]; do

        local pid

        pid=$(java-pid "${regex}")

        debug "current pid: ${current_pid}, java pid: ${pid}"

        if [ -z "${pid}" ]; then

            #
            # we weren't able to get a java pid from the configured regular expression, that may mean the
            # JVM is being bounced, so keep looping until explicitly stopped
            #

            current_pid=""
            echo -n "?"
            sleep ${interval}
            continue;
        fi

        #
        # we have what is seems to be a valid PID
        #

        if [ "${current_pid}" != "${pid}" ]; then
            current_pid=${pid}
            output_filename_prefix=$(date +'%y.%m.%d-%H.%M')-${current_pid}
            debug "file name initialized to ${output_filename_prefix}..."
        fi

        jstack-and-top-snapshot "${current_pid}" "${output_filename_prefix}" "${dir}" "${java_home}"

        if ${first}; then
            echo -n "collecting thread dumps in ${dir}, interval ${interval} seconds "
            first=false
        fi

        echo -n "."
        sleep ${interval}
    done

}

main $@