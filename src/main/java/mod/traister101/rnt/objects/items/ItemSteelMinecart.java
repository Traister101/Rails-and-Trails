package mod.traister101.rnt.objects.items;

import mcp.MethodsReturnNonnullByDefault;
import mod.traister101.rnt.objects.entities.EntitySteelMinecart;
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

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemSteelMinecart extends Item {
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

			final EntitySteelMinecart minecart = EntitySteelMinecart.create(world, blockPos.getX(), blockPos.getY(),
					blockPos.getZ(), railDirection);

			if (stack.hasDisplayName()) minecart.setCustomNameTag(stack.getDisplayName());

			world.spawnEntity(minecart);
			stack.shrink(1);
			return stack;
		}
	};

	public ItemSteelMinecart() {
		setMaxStackSize(1);
		setCreativeTab(CreativeTabs.TRANSPORTATION);
		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, MINECART_DISPENSER_BEHAVIOR);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {

		final IBlockState blockState = worldIn.getBlockState(pos);

		if (!BlockRailBase.isRailBlock(blockState)) return EnumActionResult.FAIL;

		final ItemStack itemstack = player.getHeldItem(hand);

		if (!worldIn.isRemote) {
			final EnumRailDirection railDirection = ((BlockRailBase) blockState.getBlock()).getRailDirection(
					worldIn, pos, blockState, null);

			final EntitySteelMinecart minecart = EntitySteelMinecart.create(worldIn, pos.getX(), pos.getY(), pos.getZ(),
					railDirection);

			if (itemstack.hasDisplayName()) minecart.setCustomNameTag(itemstack.getDisplayName());

			worldIn.spawnEntity(minecart);
		}

		itemstack.shrink(1);
		return EnumActionResult.SUCCESS;
	}
}