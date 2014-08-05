#!/bin/bash

rm -rf /c/projects/runtime/tda*

unzip ./target/tda-1.0.zip -d /c/projects/runtime

(cd /c/projects/runtime; ln -s ./tda-1.0 tda)

