#!/bin/bash
# STOP DEMO
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
    pgrep -lf "elasticsearch" >/dev/null  # $def_instance_path show be replace by your own pattern
}
function stop_instance
{
    local -i retry=0
    if ! check_instance; then
        echo "WARNING: instance process not found, nothing to stop" >&2
        exit 0
    fi
    pkill -f "elasticsearch" # $def_instance_path show be replace by your own pattern
    while (( retry < 20 )); do
        if ! check_instance; then
            echo "Instance stopped successfully"
            return
        else
            echo -n "."
            sleep 0.5
            retry=$(( $retry + 1 ))
        fi
    done
    echo "ERROR: instance process still alive, sending SIGKILL ..." >&2
    pkill -9 -f "elasticsearch" # $def_instance_path show be replace by your own pattern
    exit $?
}
stop_instance