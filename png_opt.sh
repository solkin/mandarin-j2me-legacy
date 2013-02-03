#!/bin/sh
d=0
ARROW="==>"
CR="pngcrush -q -rem allb -brute -ow -z 3 -zmem 9"
OPTI="optipng -quiet -o7"

optimize(){
	if [ -n "`ls ${1} | grep .png`" ]; then
		echo "$ARROW" $CR ${1}/*.png
		$CR ${1}/*.png
		echo "$ARROW" $OPTI ${1}/*.png
		$OPTI ${1}/*.png
	fi
}

process(){
	let d=${d}+1
	echo "[${d}] entering dir: ${1}"
	optimize ${1}
	for i in `ls ${1}`; do if [ -d ${1}/${i} ]; then process ${1}/${i}; fi; done
	echo "[${d}] leaving dir: ${1}"
	let d=${d}-1
}

process .
