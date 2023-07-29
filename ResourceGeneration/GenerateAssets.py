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
    lang.writeItem("rnt.minecart.steel", "Steel Minecart")
    lang.newLine()

    # Blocks
    lang.writeHeader("Blocks\n")
    lang.writeTile("rnt.rose_gold_rail", "Rose Gold Rail")

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

    # Entities
    lang.writeEntity("steel_minecart", "Steel Minecart")

    # Config
    lang.write("config.rnt.road", "Road Config")
    lang.write("config.rnt.road.move_speed_modifier", "Move Speed Modifier")


def main():
    # ROCK STUFF
    for rockType in ROCK_TYPES:
        # Road blocks, Stairs and Slabs
        roadTexture = f"rnt:blocks/road/{rockType}"
        models.createCubeAll(f"road/{rockType}", roadTexture)
        blockStates.createBlockStateSimple(f"road/{rockType}", f"rnt:models/block/road/{rockType}")
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
    recipes.createShaped("minecart/steel", ["S S", "SSS"], {"S": Ingredient(itemID="tfc:metal/sheet/steel")},
                         Result("rnt:minecart/steel"))
    # Vanilla minecart recipe
    recipes.createShaped("minecraft:minecart", ["S S", "SSS"], {"S": Ingredient(itemID="tfc:metal/sheet/wrought_iron")},
                         Result("minecraft:minecart"))
    models.createItem("rose_gold_rail", "rnt:blocks/rose_gold_rail")
    recipes.createShaped("rail/rose_gold", ["ISI", "GRG", "ISI"],
                         {"I": Ingredient(itemID="tfc:metal/rod/wrought_iron"),
                          "G": Ingredient(itemID="tfc:metal/rod/gold"), "S": Ingredient(ore="stickWood"),
                          "R": Ingredient(itemID="minecraft:redstone")}, Result("rnt:rose_gold_rail", 8))

    generateLang()


if __name__ == "__main__":
    # Put us inside our mod assets
    os.chdir("../src/main/resources/assets/rnt/")
    main()
    print("Files generated!")
