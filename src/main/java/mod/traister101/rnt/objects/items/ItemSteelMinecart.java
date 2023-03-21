package mod.traister101.rnt.objects.items;

import mcp.MethodsReturnNonnullByDefault;
import mod.traister101.rnt.objects.entities.EntitySteelMinecart;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityMinecart.Type;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemMinecart;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemSteelMinecart extends ItemMinecart {

    public ItemSteelMinecart(Type typeIn) {
        super(Type.RIDEABLE);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

        final IBlockState iblockstate = worldIn.getBlockState(pos);

        if (!BlockRailBase.isRailBlock(iblockstate))
            return EnumActionResult.FAIL;


        final ItemStack itemstack = player.getHeldItem(hand);

        if (!worldIn.isRemote) {
            BlockRailBase.EnumRailDirection enumRailDirection = iblockstate.getBlock() instanceof BlockRailBase ? ((BlockRailBase) iblockstate.getBlock()).getRailDirection(worldIn, pos, iblockstate, null) : BlockRailBase.EnumRailDirection.NORTH_SOUTH;

            final double height = enumRailDirection.isAscending() ? 0.5 : 0;

            final EntitySteelMinecart minecart = new EntitySteelMinecart(worldIn, pos.getX() + 0.5, pos.getY() + 0.0625 + height, pos.getZ() + 0.5);

            if (itemstack.hasDisplayName())
                minecart.setCustomNameTag(itemstack.getDisplayName());


            worldIn.spawnEntity(minecart);
        }

        itemstack.shrink(1);
        return EnumActionResult.SUCCESS;
    }
}