#!/bin/sh

# this is a wrapper script used to inject jvm parameters -Xms and -Xms
# specified with the ENV variable JVM_XMX_MB if JVM_XMX_MB is not detected,
# the app start command will be executed unmodified

COMMAND="$@"
NEWCOMMAND=
JVM_HEAP_PARAM=

if [ -n "$JVM_XMX_MB" ] ; then
  JVM_HEAP_PARAM="-Xms${JVM_XMX_MB}m -Xmx${JVM_XMX_MB}m"

  for W in $COMMAND ; do
      if [ "${1##*/}" == "java" ] ; then
          NEWCOMMAND="$NEWCOMMAND $1 $JVM_HEAP_PARAM"
          shift
      fi

      if ! [ "$1" == "-Xmx" ] && ! [ "$1" == "-Xms" ] ; then
          NEWCOMMAND="$NEWCOMMAND $1"
          shift
      fi
  done

else
   NEWCOMMAND=$COMMAND
fi

echo "starting app: $NEWCOMMAND"
exec $NEWCOMMAND