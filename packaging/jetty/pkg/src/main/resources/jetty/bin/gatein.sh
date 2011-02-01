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

#Production Script to launch GateIn
#See gatein-dev.sh for development starup

# Computes the absolute path of eXo
cd `dirname "$0"`

# Sets some variables
LOG_OPTS="-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog"
SECURITY_OPTS="-Djava.security.auth.login.config=etc/login.conf"
EXO_OPTS="-Dexo.product.developing=false -Dexo.conf.dir.name=gatein/conf -Djava.awt.headless=true -Djava.util.logging.config.file=gatein/conf/logging.properties"
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

JAVA_OPTIONS="-Xms128m -Xmx384m -XX:MaxPermSize=192m $JAVA_OPTS $LOG_OPTS $SECURITY_OPTS $EXO_OPTS $GATEIN_OPTS"
export JAVA_OPTIONS

# Launches the server
exec "$PRGDIR"./jetty.sh "$@"
