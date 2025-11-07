package main;

import java.util.Arrays;

public class Friend {
	private String idperson;
	private String [] friends;
	
	public Friend() {
	}
	
	public Friend(String idperson, String[] friends ) {
		this.idperson = idperson;
		this.friends = friends;
	}
	public String getIdperson() {
		return idperson;
	}
	public void setIdperson(String idperson) {
		this.idperson = idperson;
	}
	public String[] getFriends() {
		return friends;
	}
	public void setFriends(String[] friends) {
		this.friends = friends;
	}
	
	@Override
	public String toString() {
		return "Friend [idperson=" + idperson + ", friends=" + Arrays.toString(friends) + "]";
	}
	
	
}
