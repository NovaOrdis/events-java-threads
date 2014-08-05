#!/bin/bash

ant clean jar

mkdir -p ./target/tda-1.0/lib
mkdir -p ./target/tda-1.0/bin

cp ./src/main/bash/tda ./target/tda-1.0/bin
chmod a+x ./target/tda-1.0/bin/tda

cp ./target/tdanalyzer.jar ./target/tda-1.0/lib

(cd ./target; zip -r ./tda-1.0.zip tda-1.0)



