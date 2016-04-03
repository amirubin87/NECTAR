__author__ = 'Amir Rubin'
#From: 'Overlapping community detection at scale: a nonnegative matrix factorization approach'

def precision(guess,truth):
    return len(guess.intersection(truth))/len(truth)

def recall(guess,truth):
    return len(guess.intersection(truth))/len(guess)

def H(a,b):
    if a+b==0:
        return 0
    return 2*a*b/(a+b)

def F1(guess,truth):
    return H(precision(guess,truth),recall(guess,truth))

def FindBestMatchingInGroundTruth(c1, GroundTruth):
    best = 0
    for truth in GroundTruth:
        f1 = F1(c1,truth)
        if f1>best:
            best = f1
    return best

def FindBestMatchingInComms1(trueComm, Comms1):
    best = 0
    for c1 in Comms1:
        f1 = F1(c1,trueComm)
        if f1>best:
            best = f1
    return best

def ClacPartialAverageF1Part1(Comms1,GroundTruth):
    ans = 0
    for c1 in Comms1:
        ans = ans + FindBestMatchingInGroundTruth(c1, GroundTruth)
    return ans / (2*len(Comms1))

def ClacPartialAverageF1Part2(Comms1, GroundTruth):
    ans = 0
    for trueComm in GroundTruth:
        ans = ans + FindBestMatchingInComms1(trueComm, Comms1)
    return ans / (2*len(GroundTruth))

def AverageF1(Comms1,GroundTruth):
    return ClacPartialAverageF1Part1(Comms1, GroundTruth)+ ClacPartialAverageF1Part2(Comms1, GroundTruth)