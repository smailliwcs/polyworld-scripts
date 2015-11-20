import argparse
import os
import plotlib

def parseArgs():
    parser = argparse.ArgumentParser()
    parser.add_argument("runs", metavar = "RUNS", help = "runs directory")
    parser.add_argument("--bin-width", metavar = "BIN_WIDTH", type = int, default = 1000, help = "bin width")
    return parser.parse_args()

args = parseArgs()
figure = plotlib.getFigure()
axes = figure.gca()
for run in plotlib.getRunPaths(args.runs):
    lifeSpans, births, deaths = plotlib.getLifeSpans(run)
    zipped = plotlib.zipData(deaths, lifeSpans)
    binned = plotlib.binData(zipped[0], zipped[1], args.bin_width)
    axes.plot(binned[0], binned[1], alpha = 0.2)
axes.set_xlabel("Timestep")
axes.set_ylabel("Life span")
axes.set_ylim(bottom = 0)
figure.tight_layout()
figure.savefig("life-span.pdf")
