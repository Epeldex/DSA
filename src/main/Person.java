package main;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Person implements Comparable<Person> {

	private String idperson;
	private String name;
	private String lastname;
	private String birthdate;
	private String gender;
	private String birthplace;
	private String home;
	private List<String> studiedAt;
	private List<String> workplaces;
	private List<String> films;
	private String groupcode;
	private Set<String> friends = new HashSet<>();

	public Person(String idperson) {
		this.idperson = idperson;
	}

	public String getIdperson() {
		return idperson;
	}

	public void setIdperson(String idperson) {
		this.idperson = idperson;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getBirthdate() {
		return birthdate;
	}

	public void setBirthdate(String birthdate) {
		this.birthdate = birthdate;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getBirthplace() {
		return birthplace;
	}

	public void setBirthplace(String birthplace) {
		this.birthplace = birthplace;
	}

	public String getHome() {
		return home;
	}

	public void setHome(String home) {
		this.home = home;
	}

	public List<String> getStudiedAt() {
		return studiedAt;
	}

	public void setStudiedAt(List<String> studiedAt) {
		this.studiedAt = studiedAt;
	}

	public List<String> getWorkplaces() {
		return workplaces;
	}

	public void setWorkplaces(List<String> workplaces) {
		this.workplaces = workplaces;
	}

	public List<String> getFilms() {
		return films;
	}

	public void setFilms(List<String> films) {
		this.films = films;
	}

	public String getGroupcode() {
		return groupcode;
	}

	public void setGroupcode(String groupcode) {
		this.groupcode = groupcode;
	}

	@Override
	public String toString() {
		return "Person [idperson=" + idperson + ", name=" + name + ", lastname=" + lastname + ", birthdate=" + birthdate
				+ ", gender=" + gender + ", birthplace=" + birthplace + ", home=" + home + ", studiedAt=" + studiedAt
				+ ", workplaces=" + workplaces + ", films=" + films + ", groupcode=" + groupcode + "]";
	}

	public Set<String> getFriends() {
		return friends;
	}

	public void setFriends(Set<String> friends) {
		this.friends = friends;
	}

    @Override
    public int compareTo(Person other) {
        int c;

        c = this.birthplace.compareToIgnoreCase(other.birthplace);
        if (c != 0) return c;

        c = this.lastname.compareToIgnoreCase(other.lastname);
        if (c != 0) return c;

        return this.name.compareToIgnoreCase(other.name);
    }


}
