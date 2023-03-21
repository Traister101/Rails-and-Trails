package mod.traister101.rnt.objects.entities;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;

@MethodsReturnNonnullByDefault
public class EntitySteelMinecart extends EntityMinecart {

    public EntitySteelMinecart(World worldIn) {
        super(worldIn);
    }

    public EntitySteelMinecart(World worldIn, double x, double y, double z) {
        super(worldIn, x, y, z);
    }

    @Override
    protected double getMaximumSpeed() {
        return super.getMaximumSpeed() * 10;
    }

    @Override
    public Type getType() {
        return Type.RIDEABLE;
    }
}