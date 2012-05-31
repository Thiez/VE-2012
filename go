#!/bin/bash
mcrl22lps -D $1 $1.lps
rm -f $1.results
for i in *.mcf 
do
rm -f $i.pbes
lps2pbes -f $i $1.lps $i.pbes
echo $i : $(pbes2bool $i.pbes | tail -n 1) >> $1.results
done
