#!/bin/bash
set -eo pipefail

[[ ! -f ./env_java.sh ]] || source ./env_java.sh
source ../buildScript/init/env_ndk.sh

BUILD=".build"

rm -rf $BUILD/android \
  $BUILD/java \
  $BUILD/javac-output \
  $BUILD/src

source ./.gomobile-env
: "${GOMOBILE_BIN:?Run bash libcore/init.sh first}"
export GOBIND
"$GOMOBILE_BIN" bind -v -androidapi 21 -cache "$(realpath $BUILD)" -trimpath -ldflags='-s -w -linkmode=external -extldflags=-Wl,-z,max-page-size=16384,-z,common-page-size=16384' -tags='with_conntrack,with_gvisor,with_quic,with_wireguard,with_utls,with_clash_api' . || exit 1
rm -r libcore-sources.jar

proj=../app/libs
mkdir -p $proj
cp -f libcore.aar $proj
echo ">> install $(realpath $proj)/libcore.aar"
