package org.drugis.mtc.parameterization;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.math.random.RandomGenerator;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.drugis.common.stat.EstimateWithPrecision;
import org.drugis.mtc.model.Network;
import org.drugis.mtc.model.Study;
import org.drugis.mtc.model.Treatment;
import org.drugis.mtc.util.DerSimonianLairdPooling;

import edu.uci.ics.jung.algorithms.transformation.FoldingTransformerFixed.FoldedEdge;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.util.Pair;

abstract public class AbstractDataStartingValueGenerator implements StartingValueGenerator {
	protected final Network d_network;
	private final RandomGenerator d_rng;
	private final double d_scale;
	private final UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> d_cGraph;
	
	public static StartingValueGenerator create(Network network, UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> cGraph, RandomGenerator rng, double scale) {
		switch (network.getType()) {
		case CONTINUOUS:
			return new ContinuousDataStartingValueGenerator(network, cGraph, rng, scale);
		case RATE:
			return new DichotomousDataStartingValueGenerator(network, cGraph, rng, scale);
		default:
			throw new IllegalArgumentException("Don't know how to generate starting values for " + network.getType() + " data");					
		}
	}
	
	public AbstractDataStartingValueGenerator(Network network, UndirectedGraph<Treatment, FoldedEdge<Treatment, Study>> cGraph, RandomGenerator rng, double scale) {
		d_network = network;
		d_rng = rng;
		d_scale = scale;
		d_cGraph = cGraph;
	}

	protected abstract EstimateWithPrecision estimateRelativeEffect(Study study, BasicParameter p);

	protected abstract EstimateWithPrecision estimateTreatmentEffect(Study study, Treatment treatment);

	@Override
	public double getTreatmentEffect(Study study, Treatment treatment) {
		return generate(estimateTreatmentEffect(study, treatment));
	}

	@Override
	public double getRelativeEffect(Study study, BasicParameter p) {
		return generate(estimateRelativeEffect(study, p));
	}

	@Override
	public double getRelativeEffect(BasicParameter p) {
		return generate(getPooledEffect(p));
	}
	
	@Override
	public double getStandardDeviation() {
		double[] errors = new double[d_cGraph.getEdgeCount()];
		int i = 0;
		for (FoldedEdge<Treatment, Study> edge : d_cGraph.getEdges()) {
			final Pair<Treatment> v = edge.getVertices();
			errors[i++] = getPooledEffect(new BasicParameter(v.getFirst(), v.getSecond())).getStandardError();
		}

		if (d_rng == null) {
			return new Mean().evaluate(errors);
		} else {
			return errors[d_rng.nextInt(errors.length)];
		}
	}

	private EstimateWithPrecision getPooledEffect(BasicParameter p) {
		return (new DerSimonianLairdPooling(estimateRelativeEffects(p))).getPooled();
	}

	private double generate(EstimateWithPrecision estimate) {
		if (d_rng == null) {
			return estimate.getPointEstimate();
		}
		return estimate.getPointEstimate() + d_rng.nextGaussian() * d_scale * estimate.getStandardError();
	}

	protected List<EstimateWithPrecision> estimateRelativeEffects(final BasicParameter p) {
		final Pair<Treatment> treatments = new Pair<Treatment>(p.getBaseline(), p.getSubject());
		Set<Study> studies = new HashSet<Study>(d_network.getStudies());
		CollectionUtils.filter(studies, new Predicate<Study>() {
			public boolean evaluate(Study study) {
				return study.getTreatments().containsAll(treatments);
			}
		});
		
		List<EstimateWithPrecision> estimates = new ArrayList<EstimateWithPrecision>(studies.size());
		for (Study s : studies) {
			estimates.add(estimateRelativeEffect(s, p));
		}
		return estimates;
	}
}