#!/bin/bash

#show the status of the tiger search, including the size of the directory, the port
#the tomcat listen at, the database configuration and the running status

enter_current_dir() {
	OLD_DIR=`pwd`
	DIRNAME=`dirname $0`
	cd $DIRNAME
	CUR_DIR=`pwd`
}

get_pid() {
	pid=`ps aux | grep ApiServer | grep -v grep | grep "$CUR_DIR" | awk '{print $2}'`
}

#whether the apiserver is running
get_running_status() {
	is_running=`ps aux | grep ApiServer | grep -v grep | grep "$CUR_DIR"`
	if [ -z "$is_running" ]; then
		echo "running status: stopped"
	else
		echo "running status: running"
	fi
}

#get process status
get_process_status() {
	if [ -z "$pid" ]; then
		return
	fi

	echo "process information:"
	echo "	cmdline     : `cat /proc/$pid/cmdline`"
	echo "	environment : `cat /proc/$pid/environ`"
	mem_infos="`cat /proc/$pid/status | grep -E \"(VmSize|VmRSS)\"`"
	echo "	meminfors   : "
	IFS="
"
	for mem_info in $mem_infos; do
		echo "		$mem_info"
	done
}

#get the db configuration
get_dbconfig_status() {
	cd "$CUR_DIR/conf/"
	
	if [ ! -f "mysql_data_source.properties" ]; then
		return
	fi
	
	dos2unix -q mysql_data_source.properties
	mysql_host=`sed -n -r '/^\s*mysql\.host\s*=\s*(.*)\s*$/s//\1/p' mysql_data_source.properties | sed 's/ //g'`
	host_ip=`ping -c 1 "$mysql_host" | head -n 1 | awk '{print $3}'`
	echo "		mysql_host: $mysql_host$host_ip"
	cd $CUR_DIR
}

#get the size of the directory
get_size_status() {
	echo "the dir size: `du -sh`"	
}

#get the status of the api server 
get_apiserver_status() {
	if [ -z "$pid" ]; then
		return
	fi

	listeners=`netstat -nlt -p |  grep "$pid" | grep -v ":$pid"`
	IFS="
"
	echo "api server listener ports: "
	for listener in $listeners; do
		echo "	`echo $listener | awk '{print $4}'`"
	done
}

#get the deploy information
get_deploy_status() {
	echo "deploy node: `hostname`"
	echo "deploy path: $CUR_DIR" 
}

if [ $# -ge 1 ]; then
	COMMAND=$1
else
	COMMAND="simple"
fi

enter_current_dir

get_running_status

if [ $COMMAND == "detail" ]; then 
	get_deploy_status
	get_pid
	get_process_status
	get_dbconfig_status
	get_size_status
	get_apiserver_status
fi
