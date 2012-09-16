#!/bin/sh

if [ ! -d ~/.runa-gpd ]; then
    mkdir ~/.runa-gpd
    cp /usr/lib/RunaGPD/configuration/config.ini ~/.runa-gpd
    cp -R /usr/lib/RunaGPD/workspace_demo ~/RunaGPD-processes
fi

MOZ_NAME=ReplaceableMozillaHome
VER=""

for fileName in `ls /usr/lib | grep $MOZ_NAME`; do 
newVer=`echo $fileName | awk -F - '/.*-[0-9]*(\\.[0-9]*)*/ {print $NF}' `
if [ "$newVer" \> "$VER" ]; then
    VER=$newVer
fi
done

if [ "$VER" != "" ]; then
    export MOZILLA_FIVE_HOME=/usr/lib/$MOZ_NAME-$VER
else
    export MOZILLA_FIVE_HOME=/usr/lib/$MOZ_NAME
fi
    
export LD_LIBRARY_PATH=/usr/lib/RunaGPD:$MOZILLA_FIVE_HOME:$LD_LIBRARY_PATH

/usr/lib/RunaGPD/runa-gpd -install /usr/lib/RunaGPD/ -configuration ~/.runa-gpd
