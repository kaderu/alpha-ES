#!/bin/bash
# START DEMO
#set -o errexit
set -o nounset
#The vars can be used
#--------------------------
# $def_app_id
# $def_app_name
# $def_app_domain
# $def_app_deploy_path
# $def_path_app_log
# $def_path_app_data
# $def_group_id
# $def_instance_id
# $def_instance_name
# $def_instance_path
# $def_host_ip
#--------------------------
#
function check_instance
{
    pgrep -lf "elasticsearch" >/dev/null # $def_instance_path show be replace by your own pattern
}
function start_instance
{
    local -i retry=0
    if check_instance; then
        echo "ERROR: instance process has already been started" >&2
        exit 1
    fi
    SCRIPT_PATH=`dirname $0`
    cd $SCRIPT_PATH
    flag=$(<service.txt)
    if [ -z "$flag" ]; then
        echo "Warning: service flag is empty, shell will exit, please check! "
        exit
    fi
    flag=$flag | sed 's/ *$//'
    echo $flag
    nohup ./elasticsearch -d >& /dev/null
    sleep 1
    while true; do
        if check_instance; then
            echo "Instance started successfully"
            break
        elif (( retry == 200 ));then
            echo "ERROR: starting up instance has timed out" >&2
            exit 1
        else
            echo -n "."
            sleep 0.5
            retry=$(( $retry + 1 ))
        fi
    done
}
start_instance

