package mod.traister101.rnt.objects.fluids.capability;

import mod.traister101.rnt.objects.entities.EntityMinecartBarrelRNT;
import net.dries007.tfc.ConfigTFC;
import net.dries007.tfc.ConfigTFC.Devices;
import net.dries007.tfc.api.recipes.barrel.BarrelRecipe;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static net.dries007.tfc.objects.te.TEBarrel.BARREL_MAX_FLUID_TEMPERATURE;

public class BarrelMinecartFluidTank extends FluidTank {

	private final Set<Fluid> whitelist = Arrays.stream(ConfigTFC.Devices.BARREL.fluidWhitelist)
			.map(FluidRegistry::getFluid)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
	private final EntityMinecartBarrelRNT barrelCart;


	public BarrelMinecartFluidTank(final EntityMinecartBarrelRNT barrelCart) {
		super(Devices.BARREL.tank);
		this.barrelCart = barrelCart;
	}

	@Override
	public boolean canFill() {
		return barrelCart.isSealed();
	}

	@Override
	public boolean canDrain() {
		return barrelCart.isSealed();
	}

	@Override
	public boolean canFillFluidType(final @Nullable FluidStack fluidStack) {
		if (barrelCart.isSealed()) return false;

		if (fluidStack == null) return false;

		final Fluid fluidType = fluidStack.getFluid();

		// Don't go over the max temp
		if (BARREL_MAX_FLUID_TEMPERATURE < fluidType.getTemperature(fluidStack)) return false;

		return whitelist.contains(fluidType) || BarrelRecipe.isBarrelFluid(fluidStack);
	}

	@Override
	public boolean canDrainFluidType(@Nullable final FluidStack fluidStack) {
		if (fluidStack == null) return false;

		return !barrelCart.isSealed();
	}
}