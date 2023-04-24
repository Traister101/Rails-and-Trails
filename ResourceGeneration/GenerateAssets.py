import os
from typing import Final

from MinecraftDataGen import BlockState as blockStates, Recipes as recipes, Models as models
from MinecraftDataGen.Lang import Lang
from MinecraftDataGen.Recipes import Ingredient, Result

ROCK_TYPES: Final[list[str]] = [
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
    "marble",
    # Rocks+ types
    "blaimorite",
    "boninite",
    "carbonatite",
    "foidolite",
    "arkose",
    "jaspillite",
    "travertine",
    "wackestone",
    "blueschist",
    "greenschist",
    "cataclasite",
    "mylonite"
]


def generateLang():
    lang = Lang("en_us")

    lang.writeHeader("Items")
    lang.writeItem("rnt.minecart.steel.name", "Steel Minecart")
    lang.newLine()

    lang.writeHeader("Blocks\n")

    # Roads
    lang.writeComment("Roads")
    for rock_type in ROCK_TYPES:
        lang.writeTile(f"rnt.road.{rock_type}", f"{rock_type.capitalize()} Road")

    lang.newLine()

    # Stairs & Slabs.
    # The keys for these are special and can't be included with the normal roads
    for blockType in ["stairs", "slab"]:
        lang.writeComment(f"{blockType.capitalize()}")
        for rock_type in ROCK_TYPES:
            lang.writeTile(f"rnt.{blockType}.road.{rock_type}",
                           f"{rock_type.capitalize()} Road {blockType.capitalize()}")
        lang.newLine()


def main():
    # ROCK STUFF
    for rockType in ROCK_TYPES:
        # Road blocks, Stairs and Slabs
        roadTexture = f"rnt:blocks/road/{rockType}"
        blockStates.createCubeAll(f"road/{rockType}", roadTexture)
        recipes.createShaped(f"road/{rockType}", ["GSG", "SMS", "GSG"],
                             {"G": Ingredient(ore="gravel"), "S": Ingredient(itemID=f"tfc:brick/{rockType}"),
                              "M": Ingredient(itemID="tfc:mortar")},
                             Result(f"rnt:road/{rockType}", 8))

        blockStates.createSlab(f"road/{rockType}", roadTexture)
        recipes.createSlab(f"slab/road/{rockType}", f"rnt:road/{rockType}", f"rnt:slab/road/{rockType}")

        blockStates.createStairs(f"stairs/road/{rockType}", roadTexture)
        recipes.createStairs(f"stairs/road/{rockType}", f"rnt:road/{rockType}", f"rnt:stairs/road/{rockType}")

    # Minecart metal types?
    models.createItem("minecart/steel", "rnt:items/minecart/steel")

    generateLang()


if __name__ == "__main__":
    # Put us inside our mod assets
    os.chdir("../src/main/resources/assets/rnt/")
    main()
    print("Files generated!")
