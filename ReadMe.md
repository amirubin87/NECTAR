# NECTAR
Node-centric ovErlapping Community deTection AlgoRithm

This repository contains implementations of the NECTAR algorithm as described in: Node-centric detection of overlapping communities in social networks (https://arxiv.org/pdf/1607.01683)

If you use this project in your work please cite: 
"Node-Centric Detection of Overlapping Communities in Social Networks",
by Cohen, Yehonatan and Hendler, Danny and Rubin, Amir,
3rd International Winter School and Conference on Network Science, 
2017.

# Requirments
You will need JRE to run the jar.

# Example
For a simple execution on a dummy input run:

java -jar .\NECTAR.jar Dummy

# What's in this repo?

## Stable
Under 'Stable' you will find the latest version. Note - it does not support threads.
Under 'Stable/ML' u can find the training set used to train the Random Forst used by NECTAR.

## Tools
Under 'Tools' u can find our implementation to the Omega-Index and Average F1 score calculating tool. 

## Older versions
Under 'Versions' you will find old various versions of NECTAR, supporting threads and weighted graphs. 
We will not continue to devolop them one.
For instance, under 'V1' you can find two java implemented version to the NECTAR algorithm (one with modularity, one with WOCC).
('V2' is the version similar to 'V1', except that the objective function is chosen by the algorithm.)

