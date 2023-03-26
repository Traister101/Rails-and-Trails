package mod.traister101.rnt;

import javax.annotation.Nonnull;

public final class Helper {

    /**
     * Work around Intellij warning
     *
     * @return null but not
     */
    @Nonnull
    @SuppressWarnings("ConstantConditions")
    public static <T> T getNull() {
        return null;
    }
}