package mod.traister101.rnt.objects.entities;

import mcp.MethodsReturnNonnullByDefault;
import mod.traister101.rnt.objects.items.ItemsRNT;
import net.minecraft.entity.item.EntityMinecartEmpty;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EntitySteelMinecart extends EntityMinecartEmpty {

    @SuppressWarnings("unused")
    public EntitySteelMinecart(World worldIn) {
        super(worldIn);
    }

    public EntitySteelMinecart(World worldIn, double x, double y, double z) {
        super(worldIn, x, y, z);
    }

    @Override
    protected double getMaximumSpeed() {
        return super.getMaximumSpeed() * 1.5;
    }

    @Override
    public ItemStack getCartItem() {
        return new ItemStack(ItemsRNT.STEEL_MINECART);
    }

    // This needs to be overridden so our minecart item drops
    @Override
    public void killMinecart(DamageSource source) {
        setDead();

        if (world.getGameRules().getBoolean("doEntityDrops")) {
            final ItemStack itemstack = new ItemStack(ItemsRNT.STEEL_MINECART);

            if (hasCustomName()) itemstack.setStackDisplayName(getCustomNameTag());

            entityDropItem(itemstack, 0);
        }
    }
}