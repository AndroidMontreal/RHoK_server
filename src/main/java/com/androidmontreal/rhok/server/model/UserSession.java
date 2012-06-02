package com.androidmontreal.rhok.server.model;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>
 * User session has a startTime, last activity + timeout used to decide if
 * session is still valid.
 * <p>
 * A loggedOut == true overrides this and makes the session invalid.
 * <p>
 * Not built with history in mind.
 */
@Entity
@Table(name = "USER_SESSION")
@XmlRootElement
public class UserSession {

	/**
	 * <p>Keeping things neat and tidy, don't want or need a big key generation process for now.
	 */
	public final static String generateKey( ) {
		return UUID.randomUUID().toString();
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@NotNull
	@ManyToOne
	private User user;

	@NotNull
	private Date startTime;
	
	// At worst this should be set = to startTime.
	@NotNull
	private Date lastActivity;
	
	@NotNull 
	private Long timeout;

	private boolean loggedOut = false;
	
	@NotNull
	private String sessionKey;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getLastActivity() {
		return lastActivity;
	}

	public void setLastActivity(Date lastActivity) {
		this.lastActivity = lastActivity;
	}

	public Long getTimeout() {
		return timeout;
	}

	public void setTimeout(Long timeout) {
		this.timeout = timeout;
	}

	public boolean isLoggedOut() {
		return loggedOut;
	}

	public void setLoggedOut(boolean loggedOut) {
		this.loggedOut = loggedOut;
	}

	public String getSessionKey() {
		return sessionKey;
	}

	public void setKey(String key) {
		this.sessionKey = key;
	}
}
