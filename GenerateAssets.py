import os
from typing import TextIO

from MC_Asset_Generators import Item, Slab, Stairs, CubeAll

ROCK_TYPES: list[str] = [
    "granite",
    "diorite",
    "gabbro",
    "shale",
    "claystone",
    "rocksalt",
    "limestone",
    "conglomerate",
    "dolomite",
    "chert",
    "chalk",
    "rhyolite",
    "basalt",
    "andesite",
    "dacite",
    "quartzite",
    "slate",
    "phyllite",
    "schist",
    "gneiss",
    "marble"
]


def writeLang(file: TextIO, key: str, localization: str):
    file.write(f"{key}={localization}\n")


def writeLangComment(file: TextIO, comment: str):
    file.write(f"\n# {comment}\n")


def generateLang():
    os.makedirs(os.path.dirname("lang/en_us.lang"), exist_ok=True)
    with open("lang/en_us.lang", "w") as file:
        file.write("## Items\n\n")
        writeLang(file, "item.rnt.minecart.steel.name", "Steel Minecart")

        file.write("\n## Blocks\n")
        # Roads
        writeLangComment(file, "Roads")
        for rock_type in ROCK_TYPES:
            writeLang(file, f"tile.rnt.road.{rock_type}.name", f"{rock_type.capitalize()} Road")
        # Stairs & Slabs.
        # The keys for these are special and can't be included with the normal roads
        for blockType in ["stairs", "slab"]:
            writeLangComment(file, f"{blockType.capitalize()}")
            for rock_type in ROCK_TYPES:
                writeLang(file, f"tile.rnt.{blockType}.road.{rock_type}.name",
                          f"{rock_type.capitalize()} Road {blockType.capitalize()}")


def main():
    # ROCK STUFF
    for rock_type in ROCK_TYPES:
        # Road blocks, Stairs and Slabs
        roadTexture = f"rnt:blocks/road/{rock_type}"
        CubeAll(f"road/{rock_type}", roadTexture)
        Slab(f"road/{rock_type}", roadTexture)
        Stairs(f"stairs/road/{rock_type}", roadTexture)

    # Minecart metal types?
    Item("minecart/steel", "rnt:items/minecart/steel")

    generateLang()


if __name__ == "__main__":
    # Put us inside or mod assets
    os.chdir("src/main/resources/assets/rnt/")
    main()
