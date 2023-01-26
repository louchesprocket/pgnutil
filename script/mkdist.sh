#!/bin/bash

SCRIPTDIR=$(dirname $(realpath "$0"))

export root="$SCRIPTDIR"/..
export jardir=$root/out/artifacts/pgnutil_jar
export jarname=pgnutil.jar
export jar=$jardir/$jarname
export distdir=${root}/dist
export dist=${distdir}/pgnutil

set -x

rm -rf $distdir
mkdir $distdir

cat ${root}/script/stub.sh $jar > $dist && chmod +x $dist
