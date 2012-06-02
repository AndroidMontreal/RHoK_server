package com.androidmontreal.rhok.server.service;

import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.androidmontreal.rhok.server.hibernate.HibernateUtil;
import com.androidmontreal.rhok.server.hibernate.Transactionnal;
import com.androidmontreal.rhok.server.model.User;
import com.androidmontreal.rhok.server.model.UserSession;
import com.androidmontreal.rhok.server.service.AuthenticationWebService.AuthenticationResult.Result;
import com.androidmontreal.rhok.server.service.exceptions.DuplicateUserEmailDetected;
import com.google.inject.Inject;

/**
 * <p>This service is just a place to put our own code until we find/use a proper
 * security framework like Shiro and oAuth.
 */
@Produces(MediaType.APPLICATION_JSON)
@Path("/")
public class AuthenticationWebService {

	static private final Logger logger = LoggerFactory.getLogger(AuthenticationWebService.class);
	
	@Inject
	UserManagement userManagement ;

	@XmlRootElement
	static public class AuthenticationRequest {
		String email;
		String password;
	
		public String getEmail() {
			return email;
		}
	
		public void setEmail(String email) {
			this.email = email;
		}
	
		public String getPassword() {
			return password;
		}
	
		public void setPassword(String password) {
			this.password = password;
		}
	}

	/**
	 * <p>
	 * Used to communicate back to user their session key.
	 */
	@XmlRootElement
	static public class AuthenticationResult {
		enum Result {
			GRANTED, DENIED;
		};
	
		private Result result;
		private String sessionKey;
	
		public String getSessionKey() {
			return sessionKey;
		}
	
		public Result getState() {
			return result;
		}
	
		public void setSessionKey(String sessionKey) {
			this.sessionKey = sessionKey;
		}
	
		public void setState(Result result) {
			this.result = result;
		}
	
	}

	@POST
	@Consumes("application/json")
	@Path("authenticate")
	public AuthenticationResult authenticate( AuthenticationRequest request ) {
		// TODO: This is only a basic mock-like implementation. Will require something more solid for real projects.
		
		// Get the record for the given email.
		User foundUser = null ;
		try {
			foundUser = userManagement.txFindUser( request.email );
		} catch (DuplicateUserEmailDetected e) {
			// We'll use the error-level of logging as a flag to ops that there's interventions needed.
			logger.error("We got multiple user accounts with this email, can't continue processing this request.", e);
		}
		
		// failResult will be used if we find a problem.
		AuthenticationResult failResult = new AuthenticationResult();
		failResult.setState(Result.DENIED);
		
		// TODO: Should log something for each fail type.
		
		// Fail check: user doesn't exist.
		if( foundUser == null ) {
			logger.info("User not found.");
			return failResult;
		}
		
		// Fail check: password mismatch.
		boolean match = request.password.equals(foundUser.getPassword());
		// Passwords don't match? No good reason to continue.
		if( !match ) {
			logger.info("Failed password authentication.");
			return failResult ;
		}
		
		// TODO: Review, could we want simultaneous logins? Web/mobile client for example?
		// If there's an old existing (valid) session, we invalidate it. [i.e. logout]
		UserSession oldExistingSession = null ;
		
		try {
			oldExistingSession = txLogoutExistingSession( foundUser.getId() );
		} catch (DuplicateUserEmailDetected e) {
			logger.error("We got multiple user accounts with this email, can't continue processing this request.", e);
		}
		
		if( oldExistingSession != null ) {
			logger.info("Had an old session we invalidated.");
		}
		
		// Build and save new session.
		UserSession newSession = txLogin(foundUser.getId());
		
		AuthenticationResult result = new AuthenticationResult();
		result.setSessionKey(newSession.getSessionKey());
		
		return result ;
	}
	
	/**
	 * <p>Send a request to start a password reset procedure for specified account. The actual post data is the email we're looking for. 
	 * So here we consume "text/plain" instead of application/json.
	 * 
	 * @return Result.FAILURE if the email is not in our system, or potentially if request originates from blacklisted client.
	 */
	@POST
	@Consumes("text/plain")
	@Path("forgot")
	public Result forgottenPassword(String email) {
		// TODO: Impl.
		return null ;
	}
	
	@POST
	@Consumes("text/plain")
	@Path("logout")
	public Result logout( String key ) {
		// TODO: Impl.
		return null ;
	}
	
//	@Transactionnal
//	User txFindUser( String email ) {
//		// TODO Implement
//		return null ;
//	}
	
	@Transactionnal
	UserSession txLogin(Long userId) {
		Session session = HibernateUtil.getCurrentSession();

		// Get target user.
		User user = (User) session.load(User.class, userId);
		
		UserSession userSession = new UserSession();
		userSession.setKey(UserSession.generateKey());
		Date now = new Date();
		userSession.setStartTime(now);
		userSession.setLastActivity(now);
		// TODO: Real values needed here.
		userSession.setTimeout(1000L*60*60); // 1 hour trial 
		userSession.setUser(user);
		
		session.saveOrUpdate(userSession);

		return userSession ;
	}
	
	UserSession txLogoutExistingSession(Long userId) throws DuplicateUserEmailDetected {
		Session session = HibernateUtil.getCurrentSession();

		List<?> list = session.createQuery(
				"from UserSession as us where us.user.id = :userId " +
				"and (us.lastActivity+timeout) > :now")
			.setLong("userId", userId)
			.setDate("now", new Date())
			.list();	
		
		// This is a fail-state.
		if( list.size() > 1 ) {
			throw new DuplicateUserEmailDetected();
		}
		
		UserSession foundSession = null ;
		
		// We found a single existing user.
		if( list.size() == 1 ) {
			foundSession = (UserSession) list.get(0);
			
			// Invalidate/logout the foundSession.
			foundSession.setLoggedOut(true);
			session.saveOrUpdate(foundSession);
		}
		
		// We're done.
		return foundSession ;
	}
	
	
}

