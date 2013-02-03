#!/bin/sh

# Recursive PNG optimizer
# (c) m1kc, 2013

#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation; either version 2 of the License, or
#  (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with this program; if not, write to the Free Software
#  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
#  MA 02110-1301, USA.

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
