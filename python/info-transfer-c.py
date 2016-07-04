import argparse
import os
import plotlib

def parseArgs():
    parser = argparse.ArgumentParser()
    parser.add_argument("runs", metavar = "RUNS", help = "runs directory")
    parser.add_argument("stat", metavar = "STAT", choices = ("mean", "sum"), help = "statistic")
    parser.add_argument("--bin-width", metavar = "BIN_WIDTH", type = int, default = 1000, help = "bin width")
    return parser.parse_args()

args = parseArgs()
figure = plotlib.getFigure()
axes = figure.gca()
runs = list(plotlib.getRuns(args.runs))
for run in runs:
    lifeSpans, births, deaths = plotlib.getLifeSpans(run)
    values = {}
    path = os.path.join(run, "plots", "data", "info-transfer-c.txt")
    with open(path) as f:
        for line in f:
            if line.startswith("#"):
                continue
            agent, preNeuron, postNeuron, value = line.split()
            values.setdefault(int(agent), []).append(float(value))
    for agent in values:
        values[agent] = plotlib.getStatistic(values[agent], args.stat)
    for agent in lifeSpans:
        if agent not in values:
            values[agent] = 0
    zipped = plotlib.zipAgentData(births, values)
    binned = plotlib.binData(zipped[0], zipped[1], args.bin_width)
    axes.plot(binned[0], binned[1])
axes.set_xlabel("Timestep")
axes.set_ylabel("Complete transfer entropy")
axes.axhline(color = "0", linestyle = "dotted", linewidth = 0.5)
figure.tight_layout()
figure.savefig("info-transfer-a.pdf")