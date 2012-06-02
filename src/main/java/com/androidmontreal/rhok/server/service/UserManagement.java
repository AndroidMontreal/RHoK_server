package com.androidmontreal.rhok.server.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.androidmontreal.rhok.server.hibernate.HibernateUtil;
import com.androidmontreal.rhok.server.hibernate.TransactionInterceptor;
import com.androidmontreal.rhok.server.hibernate.Transactionnal;
import com.androidmontreal.rhok.server.model.User;
import com.androidmontreal.rhok.server.service.exceptions.DuplicateUserEmailDetected;
import com.androidmontreal.rhok.server.service.result.Result;
import com.androidmontreal.rhok.server.service.result.ValidationResult;

/**
 * <p>UserManagement groups the user services. Current approach is to try and think of this from the use-case point of 
 * view, more specifically in a mobile environment. 
 * 
 * <li>All cross-cutting concerns can be implemented using Guice's AoP (simpler, much easier to use than aspect J)
 * <li>TODO: add authentication/authorization cross-cutting concern.
 * <li>TODO: add loc x-cuttin concern to services.
 * <li>TODO: add protocol version x-cutting concern. [go with a 'global' protocol numbering until we need finer.]
 */
@Produces(MediaType.APPLICATION_JSON)
@Path("users")
public class UserManagement {

	static private final Logger logger = LoggerFactory.getLogger(UserManagement.class);
	
	// Maximum number of days a user can access the system without confirming their email address.
	private static final int UNCONFIRMED_MAX_DAYS = 7;

	/** 
	 * <p>Persist a user to the database.
	 * <p>NOTE: 'Transactionnal' annotated methods are invoked from 'within' a hibernate transaction. We use Guice AOP to achieve this automagically.
	 * @see TransactionInterceptor 
	 * @param newUser the user to save to database.
	 * @return the newly persisted user.
	 */
	@Transactionnal 
	User txSaveUser(User newUser) {
		Session session = HibernateUtil.getCurrentSession();
		session.save(newUser);
		return newUser ; 
	}
	
	/**
	 * <p>Find a user in the database via email.
	 * @param email
	 * @return null if not found.
	 * @throws DuplicateUserEmailDetected this likely indicates a problem with our app, needs to be flagged and handled.
	 */
	@Transactionnal 
	User txFindUser(String email) throws DuplicateUserEmailDetected {
		// Using named queries. 
		// Eventually look into adding them in mapping document to avoid overloading the entity class with annotations...
		// My goal with named queries is to get compile-time checks vs potential runtimes after entity refactorings.
		List<?> list = HibernateUtil.getCurrentSession().createQuery("from User as u where u.email like :email ").setString("email", email).list();
		
		// This is a fail-state.
		if( list.size() > 1 ) {
			throw new DuplicateUserEmailDetected();
		}
		
		// We found a single existing user.
		if( list.size() == 1 ) {
			return (User) list.get(0);
		}
		
		// We're done.
		return null ;
	}
		
	/**
	 * <p>In our case this will archive the user. We're aiming for a soft-delete of sorts here. 
	 */
	@DELETE
	@Path("{userId}")
	public Result archiveUser(@PathParam("userId") String userId ) {
		// TODO: Implement.
		Result result = new Result(Result.State.FAILURE);
		return result;
	}

	/**
	 * <p>
	 * Results from the createUser call.
	 * <ul>
	 * <li>Successfully created a new user.
	 * <li>"Success", User exists, credentials are valid.
	 * <li>Failed, user exists, credentials are invalid.
	 * <li>Failed, user exists, but we require user intervention [for email
	 * confirmation, password refresh, etc]
	 * <li>Failed on request data validation. [protocol problem?]
	 * </ul>
	 * <p>
	 * This should allow us to have minimal friction on subscription, and still
	 * allow for password retrieval and more sophisticated security features.
	 * <p>
	 * It might happen that the x-cutting authentication/authorization code will
	 * take over the password-related result codes.
	 * <p>
	 * This would probably result in only getting created|exists[implied good
	 * creds/authori.]|validation fail
	 */
	@XmlRootElement // Annotation needed to enable JSON serialization when responding to web calls.
	static public class CreateUserResult {
		
		public enum Code {
			USER_CREATED, EXISTS_GOOD_CREDS, EXISTS_BAD_CREDS, EXISTS_UNCONFIRMED_EMAIL_DUE, VALIDATION_FAILED
		};
		
		private Code resultCode;
		
		private List<ValidationResult> validationResults ;
	
		public Code getResultCode() {
			return resultCode;
		}
		
		public List<ValidationResult> getValidationResults() {
			return validationResults;
		}
		
		public void setResultCode(Code resultCode) {
			this.resultCode = resultCode;
		}
	
		public void setValidationResults(List<ValidationResult> validationResults) {
			this.validationResults = validationResults;
		}
	
	}

	
	/**
	 * <p>We'll try to always use command, to better control what fields we will be receiving and using from the web. This 
	 * is an important security issue.
	 * <p>For example, my first code draft received a "User", checked a few fields and saved it. Problem is, anybody could
	 * have decided to set some other flag in that structure, and sneaked that into our database. Bad.
	 * <p>NOTE: We rely on people properly setting bound parameters in their hibernate queries, to fight off SQL injection attacks.
	 */
	class CreateUserCommand {
		
		private String email;
		private String password;
		
		/**
		 * <p>Build a user from the selected fields found in the command. 
		 */
		public User buildUser() {
			User newUser = new User();
			
			newUser.setEmail(email);
			newUser.setPassword(password);
			
			return newUser ;
		}

		public String getEmail() {
			return email ;
		}

		public String getPassword() {
			return password ;
		}
		
		public void setEmail(String email) {
			this.email = email;
		}
		
		public void setPassword(String password) {
			this.password = password;
		}
		
	}

	/**
	 * <h3>[Use Case] User login/creation</h3>
	 * <p>The user wants to log in the system. It's possible the user never interacted with our system before.
	 * <p>We want to try to set it up in such a way as login attempts from new users will lead 
	 * to account creation. Idea is to get frictionless account creation. 
	 * <p>See CreateUserResult to get an idea of possible responses to calls.
	 */
	@POST
	@Consumes("application/json") // We expect the newUser to be passed as json info.
	public CreateUserResult createUser( CreateUserCommand createUserCommand ) {
		logger.info("Entered createUser()");
		// See if we have a record already for the given email.
		User loadedUser = null ;
		try {
			loadedUser = txFindUser(createUserCommand.getEmail());
		} catch (DuplicateUserEmailDetected e) {
			// We'll use the error-level of logging as a flag to ops that there's interventions needed.
			logger.error("We got multiple user accounts with this email, can't continue processing this request.", e);
		}
		
		// Already got someone with this email?
		if( loadedUser != null ) {
			// Credentials checks.
			boolean checksOut = loadedUser.getPassword().equals(createUserCommand.getPassword());
			
			CreateUserResult exists = new CreateUserResult();
			// If the credentials are good, return our result.
			if( checksOut ) {
				long elapsed = new Date().getTime() - loadedUser.getLastEmailCheck().getTime();
				
				double daysElapsed = (elapsed/1000.0) / 60 / 60 / 24 ;
				if( !loadedUser.getConfirmed() && daysElapsed > UNCONFIRMED_MAX_DAYS ) {
					exists.setResultCode(CreateUserResult.Code.EXISTS_UNCONFIRMED_EMAIL_DUE);
				} else {
					exists.setResultCode(CreateUserResult.Code.EXISTS_GOOD_CREDS);
				}
				
			} else {
				exists.setResultCode(CreateUserResult.Code.EXISTS_BAD_CREDS);
			}
			
			return exists ;
		} 
		
		// Do a validation check before attempting a creation.
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		
		// The command knows how to build user from received parameters.
		User newUser = createUserCommand.buildUser();
		
		// We only validate on email and password for this operation.
		HashSet<ConstraintViolation<User>> constraintViolations = new HashSet<ConstraintViolation<User>>();
		constraintViolations.addAll(validator.validateProperty(newUser, "email"));
		constraintViolations.addAll(validator.validateProperty(newUser, "password"));

		// Builds the user response if we failed to respect constraints. 
		if( constraintViolations.size() > 0 ) {
			CreateUserResult createUserResult = new CreateUserResult();
			createUserResult.setResultCode(CreateUserResult.Code.VALIDATION_FAILED);
			
			List<ValidationResult> results = new ArrayList<ValidationResult>(constraintViolations.size());
			for( ConstraintViolation<User> current : constraintViolations ) {
				ValidationResult validationResult = new ValidationResult();
				// FIXME: need to check how to extract attribute name, JSR docs fuzzy on this subject.
				validationResult.setFieldName(current.getPropertyPath().toString());
				validationResult.setMessage(current.getMessage());
				results.add(validationResult);
			}
			createUserResult.setValidationResults(results);
			
			return createUserResult ;
		}
		
		// User doesn't exist, validation was good, create the user.
		txSaveUser(newUser);

		// Prepare answer for our clients.
		CreateUserResult createUserResult = new CreateUserResult();
		createUserResult.setResultCode(CreateUserResult.Code.USER_CREATED);
		
		return createUserResult;
	}
	
	public static class InviteUserCommand {
		// TODO: Implement
	}

	/**
	 * <h3>[Use Case] User sends out invitation.</h3>
	 * <p>User invites a friend to join our system. We only want to send invites from
	 * confirmed users, ... need to elaborate this use case some more when we actually get to implement it...
	 * 
	 */
	@POST
	@Path("invite")
	@Consumes("application/json") // We expect the newUser to be passed as json info.
	public Result inviteUser( InviteUserCommand command ) {
		// TODO: Implement.
		return new Result(Result.State.FAILURE);
	}
}
