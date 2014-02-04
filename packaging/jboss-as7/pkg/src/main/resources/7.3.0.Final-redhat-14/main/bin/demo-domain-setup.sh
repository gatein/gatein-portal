#!/bin/sh
BASE_DIR=`dirname "$0"`
JB_HOME=`cd "$BASE_DIR/.."; pwd`
mkdir -p "$JB_HOME/domain/servers/server-one/configuration"
mkdir -p "$JB_HOME/domain/servers/server-two/configuration"
cp -r "$JB_HOME/standalone/configuration/gatein" "$JB_HOME/domain/servers/server-one/configuration/"
cp -r "$JB_HOME/standalone/configuration/gatein" "$JB_HOME/domain/servers/server-two/configuration/"
mkdir -p "$JB_HOME/domain/servers/server-two/extensions"
cp -r "$JB_HOME/gatein/extensions/gatein-mobile-integration.ear" "$JB_HOME/domain/servers/server-two/extensions/"