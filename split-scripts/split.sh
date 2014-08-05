#!/bin/bash

function split()
{
    local tdfile=$1
    local expressions=$2

    [ "${tdfile}" = "" ] && { echo "no thread dump file specified"; exit 1; }
    [ -f ${tdfile} ] || { echo "file ${tdfile} not found"; exit 1; }

    sd=`basename ${tdfile}`
    sd=${sd%.*}.split

    mkdir ${sd} || exit 1

    cp ${tdfile} ./current.txt

    i=0
    for e in ${expressions}; do
        target=${sd}/${i}-${e}.txt
        tda ${e} ./current.txt > ${target}
        echo -n "wrote ${target}: "
        tda -c ${target}
        tda -v ${e} ./current.txt > ./tmp.txt
        mv ./tmp.txt ./current.txt
        ((i++))
    done

    mv ./current.txt ${sd}/rest.txt
    rm -f ./tmp.txt
}