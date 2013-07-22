/**
 * Abstract class for formatting a geological coordinate.
 *
 * Copyright (C) 2012-2013 Dieter Adriaenssens
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @package org.ruleant.ariadne
 * @author  Dieter Adriaenssens <ruleant@users.sourceforge.net>
 */
package org.ruleant.ariadne;

import android.location.Location;

/**
 * Abstract class for formatting a geological coordinate.
 *
 * @author  Dieter Adriaenssens <ruleant@users.sourceforge.net>
 */
public abstract class AbstractGeoCoordinate {
    /**
     * Unformatted coordinate value.
     */
    protected double value;

    /**
     * Constructor.
     *
     * @param newValue New value for unformatted value.
     */
    public AbstractGeoCoordinate(final double newValue) {
        setValue(newValue);
    }

    /**
     * Set unformatted value.
     *
     * @param newValue New value for unformatted value.
     */
    public final void setValue(final double newValue) {
        value = newValue;
    }

    /**
     * Get unformatted value.
     *
     * @return Unformatted value.
     */
    public final double getValue() {
        return value;
    }

    /**
     * Format an unformatted angle to a GeoCoordinate.
     *
     * @return String formatted string
     */
    public String format() {
        return Location.convert(value, Location.FORMAT_SECONDS).replaceFirst(":", "° ")
                .replace(":", "' ") + "\" " + getSegmentUnit();
    }

    /**
     * Determine value segment
     *
     * @return segment code
     */
    public abstract int getSegment();

    /**
     * Get segment unit.
     *
     * @return unit
     */
    public abstract String getSegmentUnit();
}
