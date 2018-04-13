package org.binas.domain;

import java.util.List;
import java.util.ArrayList;

import org.binas.exception.UserException;

public class BinasManager {

	/** Station identifier. */
	private String id;

	private static List<User> users = new ArrayList<User>();;

	// Singleton -------------------------------------------------------------

	private BinasManager() {  }

	/**
	 * SingletonHolder is loaded on the first execution of Singleton.getInstance()
	 * or the first access to SingletonHolder.INSTANCE, not before.
	 */
	private static class SingletonHolder {
		private static final BinasManager INSTANCE = new BinasManager();
	}

	public static synchronized BinasManager getInstance() {
		return SingletonHolder.INSTANCE;
	}

 	public void setId(String id) {
 		this.id = id;
 	}

 	public static void addUser(User user) {
 		users.add(user);
 	}

 	public static User getUser(String email) throws UserException {
 		for (User u : users) {
 			if (u.getEmail().equals(email)) { return u; }
 		}
 		throw new UserException("No user found in BinasManager.");
 	}

 	public boolean userExists(String email) {
 		try { 
 			this.getUser(email);
 			return true;
 		}
 		catch (UserException ue) { return false; }
 	}
}
