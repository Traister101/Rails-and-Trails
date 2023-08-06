package mod.traister101.rnt.objects.items;

import mod.traister101.rnt.objects.entities.EntityMinecartRNT;
import mod.traister101.rnt.objects.entities.EntityMinecartRideableRNT;
import mod.traister101.rnt.objects.types.MinecartMetal;
import net.dries007.tfc.api.capability.size.IItemSize;
import net.dries007.tfc.api.capability.size.Size;
import net.dries007.tfc.api.capability.size.Weight;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRailBase.EnumRailDirection;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class ItemMinecartRideable extends Item implements IItemSize {

	private static final IBehaviorDispenseItem MINECART_DISPENSER_BEHAVIOR = new BehaviorDefaultDispenseItem() {
		private final BehaviorDefaultDispenseItem behaviourDefaultDispenseItem = new BehaviorDefaultDispenseItem();

		/**
		 * Dispense the specified stack, play the dispense sound and spawn particles.
		 */
		public ItemStack dispenseStack(final IBlockSource source, final ItemStack stack) {
			final EnumFacing facing = source.getBlockState().getValue(BlockDispenser.FACING);
			final World world = source.getWorld();

			final BlockPos blockPos;
			final IBlockState blockState;
			{
				BlockPos deferBlockPos = source.getBlockPos().offset(facing);
				IBlockState deferBlockState = world.getBlockState(deferBlockPos);
				// Block isn't a rail
				if (!BlockRailBase.isRailBlock(deferBlockState)) {
					// If we aren't facing to one of the sides we just dispense the item
					if (facing == EnumFacing.DOWN || facing == EnumFacing.UP) {
						return behaviourDefaultDispenseItem.dispense(source, stack);
					}
					// If the material isn't air we shouldn't spawn a minecart so just dispense the item
					if (deferBlockState.getMaterial() != Material.AIR) {
						return behaviourDefaultDispenseItem.dispense(source, stack);
					}

					deferBlockPos = deferBlockPos.down();
					deferBlockState = world.getBlockState(deferBlockPos);

					// If this also isn't a rail block then we should just dispense the item at this point
					if (!BlockRailBase.isRailBlock(deferBlockState)) {
						return behaviourDefaultDispenseItem.dispense(source, stack);
					}
				}
				blockPos = deferBlockPos;
				blockState = deferBlockState;
			}

			final EnumRailDirection railDirection = ((BlockRailBase) blockState.getBlock()).getRailDirection(world,
					blockPos, blockState, null);
			final ItemMinecartRideable item = (ItemMinecartRideable) stack.getItem();
			final EntityMinecartRNT minecart = EntityMinecartRideableRNT.create(world, blockPos.getX(), blockPos.getY(),
					blockPos.getZ(), railDirection, item.metal);

			if (stack.hasDisplayName()) minecart.setCustomNameTag(stack.getDisplayName());

			world.spawnEntity(minecart);
			stack.shrink(1);
			return stack;
		}
	};
	public final MinecartMetal metal;

	public ItemMinecartRideable(final MinecartMetal metal) {
		this.metal = metal;
		setMaxStackSize(1);
		setCreativeTab(CreativeTabs.TRANSPORTATION);
		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, MINECART_DISPENSER_BEHAVIOR);
	}

	@Override
	public EnumActionResult onItemUse(final EntityPlayer player, final World world, final BlockPos pos, final EnumHand hand,
			final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {

		final IBlockState blockState = world.getBlockState(pos);

		if (!BlockRailBase.isRailBlock(blockState)) return EnumActionResult.FAIL;

		final ItemStack itemStack = player.getHeldItem(hand);

		if (!world.isRemote) {
			final EnumRailDirection railDirection = ((BlockRailBase) blockState.getBlock()).getRailDirection(
					world, pos, blockState, null);

			final EntityMinecartRNT minecart = EntityMinecartRideableRNT.create(world, pos.getX(), pos.getY(), pos.getZ(),
					railDirection, metal);

			world.spawnEntity(minecart);
		}

		itemStack.shrink(1);
		return EnumActionResult.SUCCESS;
	}

	@Nonnull
	@Override
	public Size getSize(@Nonnull ItemStack itemStack) {
		return Size.LARGE;
	}

	@Nonnull
	@Override
	public Weight getWeight(@Nonnull ItemStack itemStack) {
		return Weight.VERY_HEAVY;
	}
}