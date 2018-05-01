package org.binas.station.ws;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.binas.station.domain.exception.UserNotFoundException;
/**
 * Class that manages the Registration and maintenance of Users
 *
 */
public class UsersManager {


	// Singleton -------------------------------------------------------------

	private UsersManager() {
	}

	/**
	 * SingletonHolder is loaded on the first execution of
	 * Singleton.getInstance() or the first access to SingletonHolder.INSTANCE,
	 * not before.
	 */
	private static class SingletonHolder {
		private static final UsersManager INSTANCE = new UsersManager();
	}

	public static synchronized UsersManager getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	// ------------------------------------------------------------------------

	public static int DEFAULT_INITIAL_BALANCE = 10;
	public AtomicInteger initialBalance = new AtomicInteger(DEFAULT_INITIAL_BALANCE);
	
	/**
	 * Map of existing users <email, TaggedUser>. Uses concurrent hash table
	 * implementation supporting full concurrency of retrievals and high
	 * expected concurrency for updates.
	 */
	private Map<String, TaggedUser> registeredUsers = new ConcurrentHashMap<>();

	public TaggedUser getUser(String email) throws UserNotFoundException{
		TaggedUser user = registeredUsers.get(email);
		if(user == null) {
			throw new UserNotFoundException();
		}
		return user;
	}

	/** Adds new User for this station. */
	public void addUser(String email, int balance, int tag) {
		TaggedUser user = new TaggedUser(email, balance, tag);
		registeredUsers.put(email, user);
	} 
	
	public synchronized void reset() {
		registeredUsers.clear();
		initialBalance.set(DEFAULT_INITIAL_BALANCE);
	}
	
	public synchronized void init(int newBalance) {
		initialBalance.set(newBalance); 
	}
	
}
