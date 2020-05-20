#!/bin/bash

DIR_NAME=`dirname $0`
cd $DIR_NAME
THIS=`pwd`

current_apiserver_version=1.0.0-SNAPSHOT
templates_dir=./templates

get_current_apiserver_version() {
	current_apiserver_version=`cat ../pom.xml | sed -n '/<\/version>/{p;q}' | sed 's/\s*<version>\([^<]*\)<\/version>/\1/'`
}

create_mvn_api_project() {
	#首先生成项目目录
	prepare_project_dir
	#准备配置文件
	prepare_conf_for_maven
	#准备pom文件
	get_current_apiserver_version
	cat $templates_dir/pom.xml | sed -e 's/\${apiserver_new_version}/'"$current_apiserver_version"'/' -e 's/\${pacake-name}/'"$package_name"'/' -e 's/\${artifact-id}/'"$artifact_id"'/'\
		-e 's/\${apiserver-new-version}/'"$current_apiserver_version"'/' > pom.xml.new
	mv pom.xml.new $artifact_id/pom.xml
	
	#最后准备运维脚本和自动发布脚本
	prepare_operation_scripts
	cp $templates_dir/maven_dist.sh $artifact_id/dist.sh
	
	echo "finish creating api project for maven"
}

create_ant_api_project() {
	#首先生成项目目录
	prepare_project_dir
	#准备配置文件
	prepare_conf_for_ant
	#准备lib
	prepare_apiserver_jar
	prepare_project_lib
	cp ../target/apiserver*.jar $artifact_id/lib/
	cp ../lib/* $artifact_id/lib/
	#准备default.properties和build.xml
	cat $templates_dir/default.properties | sed 's/\${artifact-id}/'"$artifact_id"'/' > default.properties.new
	mv default.properties.new $artifact_id/default.properties
	cat $templates_dir/build.xml | sed 's/\${artifact-id}/'"$artifact_id"'/' > build.xml.new
	mv build.xml.new $artifact_id/build.xml
	
	#最后准备运维脚本和自动发布脚本
	prepare_operation_scripts
	cp $templates_dir/ant_dist.sh $artifact_id/dist.sh
	
	echo "finish creating api project for ant"
}

prepare_project_lib() {
	cd ..
	mvn org.apache.maven.plugins:maven-dependency-plugin:2.4:copy-dependencies -DoutputDirectory=./lib
	cd -
}

prepare_apiserver_jar() {
	cd ..
	mvn package -Dmaven.test.skip=true -Dmaven.javadoc.skip=true
	cd -
}

prepare_conf_for_ant() {
	cd ../src/main/resources
	svn_url=`svn info | sed -n 's/^URL:\s*\(.*\)/\1/p'`
	cd -
	
	svn export --force $svn_url $artifact_id/conf/
}

prepare_conf_for_maven() {
	cd ../src/main/resources
	svn_url=`svn info | sed -n 's/^URL:\s*\(.*\)/\1/p'`
	cd -

	svn export --force $svn_url $artifact_id/src/main/resources
	
	cd ../src/test/resources/
	svn_url=`svn info | sed -n 's/^URL:\s*\(.*\)/\1/p'`
	cd -
	
	svn export --force $svn_url $artifact_id/src/test/resources
}

prepare_src_for_ant() {
	root_src_code_dir_level=`echo $package_name | sed 's/\./\//g'`
	
	src_code_dir="$artifact_id/src/java/$root_src_code_dir_level"
	mkdir -p $src_code_dir
	mkdir -p $artifact_id/conf
	mkdir -p $artifact_id/lib
	mkdir -p $artifact_id/src/test
	
	cat $templates_dir/HelloWorld.java | sed 's/\${pacake-name}/'"$package_name"'/' > HelloWorld.java
	mv HelloWorld.java $src_code_dir
}

prepare_src_for_maven() {
	root_src_code_dir_level=`echo $package_name | sed 's/\./\//g'`
	
	src_code_dir="$artifact_id/src/main/java/$root_src_code_dir_level"
	mkdir -p $src_code_dir
	mkdir -p $artifact_id/src/test/java
	mkdir -p $artifact_id/src/test/resources
	
	cat $templates_dir/HelloWorld.java | sed 's/\${pacake-name}/'"$package_name"'/' > HelloWorld.java
	mv HelloWorld.java $src_code_dir
}

prepare_operation_scripts() {
	mkdir -p $artifact_id/operation
	cp ../operation/* $artifact_id/operation/
	mv $artifact_id/operation/operation.sh $artifact_id/
	mv $artifact_id/operation/status.sh $artifact_id/
}

prepare_project_dir() {
	if [ -d "$artifact_id" ]; then
		rm -rf $artifact_id
	fi

	mkdir $artifact_id
	mkdir $artifact_id/logs
	
	if [ "$build_type" == "ant" ]; then
		prepare_src_for_ant
	else #maven type
		prepare_src_for_maven
	fi
}

usage() {
	echo "Usage: $0 type(ant|maven) package_name artifact_id"
	echo
	echo "Example: bash $0 maven com.wintim.tiger.apiserver tiger-apiserver"
}

if [ $# != 3 ]; then
	usage
	exit 0
fi

build_type=$1
package_name=$2
artifact_id=$3

cd ..
mvn clean
cd -

if [ "$build_type" == "ant" ]; then
	create_ant_api_project
elif [ "$build_type" == "maven" ]; then
	create_mvn_api_project
else
	echo "Can not build for $build_type type"
	usage
	exit 1
fi

