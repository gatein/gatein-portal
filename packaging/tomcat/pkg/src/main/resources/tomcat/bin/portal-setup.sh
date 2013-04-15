#!/bin/sh

# Portal Setup Utility
#
# A simple utility for setting portal root user password
#

DIRNAME=`dirname "$0"`

# OS specific support (must be 'true' or 'false').
cygwin=false;
if  [ `uname|grep -i CYGWIN` ]; then
    cygwin = true;
fi

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
    [ -n "$JAVA_HOME" ] &&
        JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
    [ -n "$JAVAC_JAR" ] &&
        JAVAC_JAR=`cygpath --unix "$JAVAC_JAR"`
fi

# Setup the JVM
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
        JAVA="$JAVA_HOME/bin/java"
    else
        JAVA="java"
    fi
fi


# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
    JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
fi

# Sample JPDA settings for remote socket debugging
#JAVA_OPTS="$JAVA_OPTS -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=y"

eval \"$JAVA\" $JAVA_OPTS \
         -cp \"../lib/*\" \
         org.gatein.portal.installer.PortalSetupCommand "$@"