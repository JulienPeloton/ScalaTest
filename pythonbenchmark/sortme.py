from collections import Counter

def load_and_count(fn):
    """
    Load a txt file, sort the words by their occurence.

    Parameters
    ----------
    fn : string
        Filename
    """
    data = open(fn, "r", encoding="utf-8-sig")
    wordcount = Counter(data.read().split())

    # for item in wordcount.items():
    #     print("{}\t{}".format(*item))


if __name__ == "__main__":
    load_and_count('../sparkdev/data/dico.txt')
