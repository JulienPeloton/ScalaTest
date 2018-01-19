# julien@lal
import numpy as np
import pylab as pl
import glob

def norm(p):
    """
    Split two 1D arrays of text into name (as str) and data (as float).
    """
    name, data = p
    return name, np.array(data, dtype=float)


if __name__ == "__main__":
    files = glob.glob("../profiling_*.txt")

    dic = {}
    parts = []
    for fn in files:
        parts.append(int(fn.split("/")[-1].split(".")[0].split("_")[-1]))

    fn = "../profiling_{}.txt"
    parts = np.sort(parts)
    for pos, part in enumerate(parts):
        names, data = norm(np.loadtxt(fn.format(part), dtype=str).T)
        for n, d in zip(names, data):
            if pos == 0:
                dic[n] = []
            dic[n].append(d)

    for k in dic.keys():
        pl.semilogx(parts, dic[k], label=k, marker='o')

    pl.title("Ngal = 10,000,000 / 2 cores / 4G memory")
    pl.xlabel("Number of parts")
    pl.ylabel("Time (s)")

    pl.legend()
    pl.savefig("spark_profiling.pdf")
    pl.show()
