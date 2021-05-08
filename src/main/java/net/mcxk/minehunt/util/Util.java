package net.mcxk.minehunt.util;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Util {
    /**
     * Convert strList to String. E.g "Foo, Bar"
     *
     * @param strList Target list
     * @return str
     */
    @NotNull
    public static String list2String(@NotNull List<String> strList) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < strList.size(); i++) {
            builder.append(strList.get(i));
            if (i + 1 != strList.size()) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    /**
     * Returns loc with modified pitch/yaw angles so it faces lookat
     *
     * @param loc    The location a players head is
     * @param lookat The location they should be looking
     * @return The location the player should be facing to have their crosshairs on the location
     * lookAt Kudos to bergerkiller for most of this function
     */
    public static @NotNull Location lookAt(@NotNull Location loc, @NotNull Location lookat) {
        // Clone the loc to prevent applied changes to the input loc
        loc = loc.clone();
        // Values of change in distance (make it relative)
        double dx = lookat.getX() - loc.getX();
        double dy = lookat.getY() - loc.getY();
        double dz = lookat.getZ() - loc.getZ();
        // Set yaw
        if (dx != 0) {
            // Set yaw start value based on dx
            if (dx < 0) {
                loc.setYaw((float) (1.5 * Math.PI));
            } else {
                loc.setYaw((float) (0.5 * Math.PI));
            }
            loc.setYaw(loc.getYaw() - (float) Math.atan(dz / dx));
        } else if (dz < 0) {
            loc.setYaw((float) Math.PI);
        }
        // Get the distance from dx/dz
        double dxz = Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));
        float pitch = (float) -Math.atan(dy / dxz);
        // Set values, convert to degrees
        // Minecraft yaw (vertical) angles are inverted (negative)
        loc.setYaw(-loc.getYaw() * 180f / (float) Math.PI + 360);
        // But pitch angles are normal
        loc.setPitch(pitch * 180f / (float) Math.PI);
        return loc;
    }
}
