package org.vaadin.mvm.domain;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.vaadin.addon.leaflet.shared.Point;

public class Person {

	private static Map<String, Person> idToPerson = Collections
			.synchronizedMap(new HashMap<String, Person>());

	public static Person[] getPersons() {
		return idToPerson.values().toArray(new Person[idToPerson.size()]);
	}

	private String id = UUID.randomUUID().toString();

	private String nickName = "User " + id.substring(0, 4);

	private Point lastLocation;
	private Double accuracy;
	private Date lastSeen;
	private Integer lastLayerIndex;
	private Set<PlaceMark> placeMarks = new HashSet<PlaceMark>();

	public Person() {
		idToPerson.put(id, this);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return nickName;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Person) {
			Person p = (Person) obj;
			if (this == p) {
				return true;
			}
			if (this.id == null || p.id == null) {
				return false;
			}
			return this.id.equals(p.id);
		}

		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return id == null ? 0 : id.hashCode();
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public Point getLastLocation() {
		return lastLocation;
	}

	public void setLastLocation(Point lastLocation) {
		this.lastLocation = lastLocation;
		setLastSeen(new Date());
	}

	public static Person withId(String value) {
		Person person = idToPerson.get(value);
		if (person == null) {
			person = new Person();
			idToPerson.remove(person.getId());
			person.setId(value);
			idToPerson.put(value, person);
			person.setNickName("Returning " + person.getNickName());
		}
		return person;
	}

	public Date getLastSeen() {
		return lastSeen;
	}

	public void setLastSeen(Date lastSeen) {
		this.lastSeen = lastSeen;
	}

	public Double getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(Double accuracy) {
		this.accuracy = accuracy;
	}

	public Integer getLastLayerIndex() {
		return lastLayerIndex;
	}

	public void setLastLayerIndex(Integer lastLayerIndex) {
		this.lastLayerIndex = lastLayerIndex;
	}

	public Set<PlaceMark> getPlaceMarks() {
		return placeMarks;
	}

	public void setPlaceMarks(Set<PlaceMark> placeMarks) {
		this.placeMarks = placeMarks;
	}

}
