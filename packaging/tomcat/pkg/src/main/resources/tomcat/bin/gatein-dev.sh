#!/bin/sh
#
# Copyright (C) 2009 eXo Platform SAS.
# 
# This is free software; you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation; either version 2.1 of
# the License, or (at your option) any later version.
# 
# This software is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
# 
# You should have received a copy of the GNU Lesser General Public
# License along with this software; if not, write to the Free
# Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA, or see the FSF site: http://www.fsf.org.
#

# Computes the absolute path of eXo
cd `dirname "$0"`

# Sets some variables
LOG_OPTS="-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog"
SECURITY_OPTS="-Djava.security.auth.login.config=../conf/jaas.conf"
EXO_OPTS="-Dexo.product.developing=true -Dexo.conf.dir.name=gatein/conf -Djava.awt.headless=true"
EXO_CONFIG_OPTS="-Xms128m -Xmx512m -XX:MaxPermSize=256m -Dorg.exoplatform.container.configuration.debug"
JPDA_TRANSPORT=dt_socket
JPDA_ADDRESS=8000
REMOTE_DEBUG="-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n"
JMX_AGENT="-Dcom.sun.management.jmxremote"
GATEIN_OPTS=""

while getopts "D:" OPTION
do
     case $OPTION in
         D)
             GATEIN_OPTS="$GATEIN_OPTS -D$OPTARG"
             ;;
     esac
done

# skip getopt parms
shift $((OPTIND-1))

JAVA_OPTS="$JAVA_OPTS $LOG_OPTS $SECURITY_OPTS $EXO_OPTS $EXO_CONFIG_OPTS $REMOTE_DEBUG $GATEIN_OPTS"
export JAVA_OPTS

# Launches the server
exec "$PRGDIR"./catalina.sh "$@"
