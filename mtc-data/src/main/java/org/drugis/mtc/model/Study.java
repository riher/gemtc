package org.drugis.mtc.model;

import java.beans.PropertyChangeListener;

import javax.xml.bind.annotation.XmlTransient;

import org.drugis.common.beans.ObserverManager;
import org.drugis.mtc.data.DataType;
import org.drugis.mtc.data.StudyData;

import com.jgoodies.binding.beans.Observable;
import com.jgoodies.binding.list.ObservableList;

public class Study extends StudyData implements Observable {
	public static final String PROPERTY_ID = "id";
	
	@XmlTransient
	ObserverManager d_obsManager = new ObserverManager(this);
	
	public Study() {
		super();
	}
	
	public Study(String id) {
		super();
		setId(id);
	}

	@Override
	public void setId(String newValue) {
		String oldValue = getId();
		super.setId(newValue);
		d_obsManager.firePropertyChange(PROPERTY_ID, oldValue, newValue);
	}
	
	@SuppressWarnings("unchecked")
	public ObservableList<Measurement> getMeasurements() {
		return (ObservableList) super.getMeasurementList();
	}
	
	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		d_obsManager.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		d_obsManager.addPropertyChangeListener(listener);
	}
	
	/**
	 * Create a clone of this Study, with the Measurement values restricted to those allowed by the DataType.
	 */
	public Study restrictMeasurements(DataType type) {
		Study s = new Study();
		s.setId(getId());
		for (Measurement m : getMeasurements()) {
			s.getMeasurements().add(m.restrict(type));
		}
		return s;
	}
}