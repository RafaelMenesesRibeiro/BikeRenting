package org.binas.domain;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.binas.domain.exception.InvalidEmailException;
import org.binas.domain.exception.UserAlreadyExistsException;
import org.binas.domain.exception.UserNotFoundException;
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
	 * Map of existing users <email, User>. Uses concurrent hash table
	 * implementation supporting full concurrency of retrievals and high
	 * expected concurrency for updates.
	 */
	private Map<String, User> registeredUsers = new ConcurrentHashMap<>();

	
	
	public User getUser(String email) throws UserNotFoundException{
		User user = registeredUsers.get(email);
		if(user == null) {
			throw new UserNotFoundException();
		}
		return user;
	}
	
	public synchronized User RegisterNewUser(String email) throws UserAlreadyExistsException, InvalidEmailException {
		if(email == null || email.trim().length() == 0 || !email.matches("\\w+(\\.?\\w)*@\\w+(\\.?\\w)*")) {
			throw new InvalidEmailException();
		}
		
		try {
			getUser(email);
			throw new UserAlreadyExistsException();
			
		} catch (UserNotFoundException e) {
			User user = new User(email,initialBalance.get());
			registeredUsers.put(email, user);
			return user;
		}
	}
	
	public synchronized void reset() {
		registeredUsers.clear();
		initialBalance.set(DEFAULT_INITIAL_BALANCE);
	}
	
	public synchronized void init(int newBalance) {
		initialBalance.set(newBalance); 
	}
	
}
