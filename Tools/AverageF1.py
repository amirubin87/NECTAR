__author__ = 'Amir Rubin'
#From: 'Overlapping community detection at scale: a nonnegative matrix factorization approach'

def precision(guess,truth):
    return (float)(len(guess.intersection(truth)))/len(truth)

def recall(guess,truth):
    return (float)(len(guess.intersection(truth)))/len(guess)

def H(a,b):
    if a+b==0:
        return 0
    return (float)(2*a*b)/(a+b)

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

def CalcPartialAverageF1Part1(Comms1,GroundTruth):
    ans = (float)(0)
    for c1 in Comms1:
        ans = ans + FindBestMatchingInGroundTruth(c1, GroundTruth)
    return (float)(ans) / (2*len(Comms1))

def CalcPartialAverageF1Part2(Comms1, GroundTruth):
    ans = (float)(0)
    for trueComm in GroundTruth:
        ans = ans + FindBestMatchingInComms1(trueComm, Comms1)

    return (float)(ans) / (2*len(GroundTruth))
	
def AverageF1(Comms1Path,GroundTruthPath, outputPath):
    Comms1File = open(Comms1Path, 'r')
    ListOfComms1 = Comms1File.readlines()
    Comms1File.close()

    Comms1 = []
    for nodes in ListOfComms1:
      Comms1.append(set(nodes.replace(' \n', '').split(' ')))

    GroundTruthFile = open(GroundTruthPath, 'r')
    ListOfGroundTruthComms = GroundTruthFile.readlines()
    GroundTruthFile.close()

    GroundTruth = []
    for nodes in ListOfGroundTruthComms:
      GroundTruth.append(set(nodes.replace(' \n', '').split(' ')))

    part1 = CalcPartialAverageF1Part1(Comms1, GroundTruth)
    part2 = CalcPartialAverageF1Part2(Comms1, GroundTruth)
    ans = part1 + part2
    output = open(outputPath, 'a')
    output.write( 'AverageF1Score:\t' + str(ans))
    output.close()
    print ans

d="N30000/"
a="5.0"
print d

AverageF1("C:/Temp/"+ d +"ST/" +  a+".txt","C:/Temp/"+ d +"MT/" +  a+".txt", "C:/Users/t-amirub/OneDrive/MA/net/b-om2-10-1/MT/F.txt")

d="N50K/"
a="5.0"
print d

AverageF1("C:/Temp/"+ d +"ST/" +  a+".txt","C:/Temp/"+ d +"MT/" +  a+".txt", "C:/Users/t-amirub/OneDrive/MA/net/b-om2-10-1/MT/F.txt")
