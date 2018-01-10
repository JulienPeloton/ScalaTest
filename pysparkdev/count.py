from pyspark.sql import SparkSession
from operator import add
import numpy as np

class CountWords():
    """ Simple application to count word in a txt file """
    def __init__(self, filename, interactive=False):
        """
        Simple application to count word in a txt file.

        Parameters
        ----------
        filename : string
            Text file.
        interactive : bool
            If True, print the number of word found.
        """
        self.filename = filename
        self.interactive = interactive

        self.nword = self.countme()

    def countme(self):
        """

        Examples
        ---------
        >>> CW = CountWords("../sparkdev/data/principia.txt", True)
        129363 words detected!
        >>> print(CW.nword)
        129363

        >>> CW = CountWords("../sparkdev/data/empty.txt", True)
        File empty!
        >>> print(CW.nword)
        0
        """
        spark = SparkSession\
            .builder\
            .appName("countme")\
            .getOrCreate()

        lines = spark.read.text(self.filename).rdd.map(lambda r: r[0])

        try:
            counts = lines.flatMap(lambda x: x.split(' '))\
                .filter(lambda x: x != "")\
                .map(lambda x: (x, 1))\
                .countByKey().values()
        except ValueError:
            if self.interactive:
                print("File empty!")
            return 0

        nword = np.sum(list(counts))
        if self.interactive:
            print("{} words detected!".format(nword))

        return nword


if __name__ == "__main__":
    import doctest
    doctest.testmod()
