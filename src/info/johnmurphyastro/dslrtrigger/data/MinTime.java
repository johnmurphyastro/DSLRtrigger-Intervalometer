/*
 * DSLR Trigger is designed to control a DSLR camera to providing accurate
 * start and end exposure times
 * Copyright (C) 2018 - 2019  John Murphy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package info.johnmurphyastro.dslrtrigger.data;

/**
 * @author John Murphy
 */
public class MinTime {
    /** Minimum exposure time */
    public final static int EXPOSURE_MS = 10;
    /** Minimum mirror move time */
    public final static int MIRROR_MOVE_MS = 1000;
    /** Minimum time to leave after previous shot */
    public final static int BEFORE_SHOT_MS = 250;
}
