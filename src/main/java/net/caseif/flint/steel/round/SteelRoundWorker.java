    /*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2016, Max Roncace <me@caseif.net>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.caseif.flint.steel.round;

import net.caseif.flint.challenger.Challenger;
import net.caseif.flint.common.challenger.CommonChallenger;
import net.caseif.flint.common.round.CommonRound;
import net.caseif.flint.common.round.CommonRoundWorker;
import net.caseif.flint.config.ConfigNode;
import net.caseif.flint.steel.SteelCore;
import net.caseif.flint.steel.util.helper.LocationHelper;
import net.caseif.flint.util.physical.Boundary;
import net.caseif.flint.util.physical.Location3D;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

class SteelRoundWorker extends CommonRoundWorker {

    SteelRoundWorker(CommonRound round) {
        super(round);
    }

    @Override
    protected void checkPlayerLocations() {
        Boundary bound = getRound().getArena().getBoundary();
        for (Challenger challenger : getRound().getChallengers()) {
            if (((CommonChallenger) challenger).isLeaving()) {
                return;
            }

            Player player = Bukkit.getPlayer(challenger.getUniqueId());
            Location3D loc = LocationHelper.convertLocation(player.getLocation());
            if (!bound.contains(loc)) {
                if (getRound().getConfigValue(ConfigNode.ALLOW_EXIT_BOUNDARY)) {
                    challenger.removeFromRound();
                } else if (loc.getY() < 0) {
                    Location3D nextSpawn = challenger.getRound().nextSpawnPoint();
                    player.setFallDistance(0);
                    player.teleport(new Location(player.getWorld(), nextSpawn.getX(), nextSpawn.getY(),
                            nextSpawn.getZ(), player.getLocation().getYaw(), player.getLocation().getPitch()));
                } else {
                    double x = loc.getX() > bound.getUpperBound().getX() ? bound.getUpperBound().getX()
                            : loc.getX() < bound.getLowerBound().getX() ? bound.getLowerBound().getX()
                                    : loc.getX();
                    double y = loc.getY() > bound.getUpperBound().getY() ? bound.getUpperBound().getY()
                            : loc.getY() < bound.getLowerBound().getY() ? bound.getLowerBound().getY()
                                    : loc.getY();
                    double z = loc.getZ() > bound.getUpperBound().getZ() ? bound.getUpperBound().getZ()
                            : loc.getZ() < bound.getLowerBound().getZ() ? bound.getLowerBound().getZ()
                                    : loc.getZ();
                    player.teleport(new Location(player.getWorld(), x, y, z,
                            player.getLocation().getYaw(), player.getLocation().getPitch()));
                }
            }
        }
    }

}
