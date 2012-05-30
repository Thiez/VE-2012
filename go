#!/bin/bash
for i in *.mcf 
do
lps2pbes -f $i new.lps $i.pbes
pbes2bool -v $i.pbes
done