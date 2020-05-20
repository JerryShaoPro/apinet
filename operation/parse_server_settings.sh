#!/bin/bash

export server_port=8888
export server_monitor_port=8889

server_settings_file_path="./conf/server.settings.yml"
if [ ! -f "$server_settings_file_path" ]; then
	server_settings_file_path="./target/classes/server.settings.yml"
fi

parse_server_port_conf() {
	if [ ! -f "$server_settings_file_path" ]; then
		return
	fi
	
	new_server_port=`sed -n -e '/\s*server\.port/p' $server_settings_file_path | awk -F':' '{print $2}' | sed 's/\s*//g'`
	new_server_monitor_port=`sed -n -e '/\s*server\.monitor\.port/p' $server_settings_file_path | awk -F':' '{print $2}' | sed 's/\s*//g'`
	if [ ! -z "$new_server_port" ]; then
		server_port="$new_server_port"
	fi
	if [ ! -z "$new_server_monitor_port" ]; then
		server_monitor_port="$new_server_monitor_port"
	fi
}

parse_server_port_conf