#!/bin/bash

if [ "${RUNTIME_DIR}" = "" ]; then
    echo "'RUNTIME_DIR' environment variable not set. Set it and try again" 1>&2;
    exit 1
fi

if [ ! -d ${RUNTIME_DIR} ]; then
    echo "'RUNTIME_DIR' ${RUNTIME_DIR} does not exist" 1>&2;
    exit 2
fi

rm -rf ${RUNTIME_DIR}/tda*

unzip ./target/tda-1.0.zip -d ${RUNTIME_DIR}

(cd ${RUNTIME_DIR}; ln -s ./tda-1.0 tda)

chmod a+rx ${RUNTIME_DIR}/tda/bin/*


