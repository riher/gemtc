/*
 * This file is part of drugis.org MTC.
 * MTC is distributed from http://drugis.org/mtc.
 * Copyright (C) 2009-2011 Gert van Valkenhoef.
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.drugis.mtc;

import org.apache.commons.collections15.Transformer;

public class ContinuousNetworkBuilder<TreatmentType> extends NetworkBuilder<ContinuousMeasurement, TreatmentType> {
	public ContinuousNetworkBuilder() {
		super();
	}
	
	public ContinuousNetworkBuilder(Transformer<TreatmentType, String> idToString) {
		super(idToString);
	}
	
	public void add(String studyId, TreatmentType treatmentId, double mean, double stdDev, int sampleSize) {
		Treatment t = makeTreatment(treatmentId);
		add(studyId, t, new ContinuousMeasurement(t, mean, stdDev, sampleSize));
	}
}
