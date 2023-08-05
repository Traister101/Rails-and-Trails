package mod.traister101.rnt.objects.blocks;

import mod.traister101.rnt.ConfigRNT;
import net.dries007.tfc.api.capability.size.IItemSize;
import net.dries007.tfc.api.capability.size.Size;
import net.dries007.tfc.api.capability.size.Weight;
import net.dries007.tfc.api.types.Rock;
import net.dries007.tfc.api.types.RockCategory;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class Road extends Block implements IItemSize {

	/// Map containing rock road block pairs. Used to better register the roads and slabs
	private static final Map<Rock, Road> ROAD_MAP = new HashMap<>();

	public Road(Rock rock) {
		super(Material.ROCK);

		ROAD_MAP.put(rock, this);

		setSoundType(SoundType.STONE);
		final RockCategory rockCategory = rock.getRockCategory();
		setHardness(rockCategory.getHardness());
		setResistance(rockCategory.getResistance());
		setHarvestLevel("pickaxe", 0);
	}

	/**
	 * Returns the road for the given rock type
	 *
	 * @param rock type of rock
	 *
	 * @return Road instance for the given rock type
	 */
	public static Road get(Rock rock) {
		return ROAD_MAP.get(rock);
	}

	@Override
	public void onEntityWalk(final World worldIn, final BlockPos pos, final Entity entityIn) {

		entityIn.motionX *= ConfigRNT.ROAD_CONFIG.moveSpeedModifier;
		entityIn.motionZ *= ConfigRNT.ROAD_CONFIG.moveSpeedModifier;

		super.onEntityWalk(worldIn, pos, entityIn);
	}

	@Nonnull
	@Override
	public Size getSize(@Nonnull final ItemStack itemStack) {
		return Size.SMALL;
	}

	@Nonnull
	@Override
	public Weight getWeight(@Nonnull final ItemStack itemStack) {
		return Weight.LIGHT;
	}
}