package cc.sighs.handheldmoon.lights;

import cc.sighs.handheldmoon.api.light.DynamicLightBuilder;
import cc.sighs.handheldmoon.api.light.impl.RayLightBehavior;
import cc.sighs.handheldmoon.dynamiclight.DynamicLightDefaults;
import cc.sighs.handheldmoon.dynamiclight.DynamicLightBehavior;
import cc.sighs.handheldmoon.util.LineLightMath;
import cc.sighs.handheldmoon.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

/**
 * Player flashlight light behavior.
 * <p>
 * Delegates to {@link RayLightBehavior} internally via
 * the {@link cc.sighs.handheldmoon.api.light} API.
 */
public class PlayerFlashlightLineLightBehavior implements DynamicLightBehavior {
    private final Player player;
    private RayLightBehavior delegate;
    private int color;
    private static final double INNER = 0.5;
    private static final double OUTER = 0.7;

    public PlayerFlashlightLineLightBehavior(Player player) {
        this.player = player;
        this.color = heldColorArgb(player);
        this.delegate = createDelegate(color);
    }

    private RayLightBehavior createDelegate(int lightColor) {
        return new RayLightBehavior(
                DynamicLightBuilder.cone()
                        .range(DynamicLightDefaults.FLASHLIGHT_RANGE)
                        .angle(INNER, OUTER)
                        .luminance(15.0)
                        .occlusion(false)
                        .buildConfig(),
                        () -> player.getEyePosition(1.0f),
                        () -> LineLightMath.computeDirection(player.getYRot(), player.getXRot(), false),
                        () -> Utils.isUsingFlashlight(player),
                        lightColor
                );
    }

    /**
     * Light colour of the held lamp's own device config, so per-item colours
     * tint the beam; players lit through a compat controller keep the global
     * config colour.
     */
    private static int heldColorArgb(Player player) {
        return cc.sighs.handheldmoon.registry.Config.lightColorArgb(Utils.heldLampConfig(player));
    }

    @Override
    public double lightAt(int blockX, int blockY, int blockZ, double falloffRatio) {
        return delegate.lightAt(blockX, blockY, blockZ, falloffRatio);
    }

    @Override
    public int color() {
        return delegate.color();
    }

    @Override
    public Bounds getBounds() {
        return delegate.getBounds();
    }

    @Override
    public boolean hasChanged() {
        // The colour lives in the held item's data component, which the ray
        // light config does not observe; rebuild the delegate when it changes.
        int current = heldColorArgb(player);
        if (current != color) {
            color = current;
            delegate = createDelegate(color);
            return true;
        }
        return delegate.hasChanged();
    }

    @Override
    public boolean isRemoved() {
        return delegate.isRemoved();
    }

    @Override
    public BatchLightSnapshot getBatchLightSnapshot() {
        return delegate.getBatchLightSnapshot();
    }

}
