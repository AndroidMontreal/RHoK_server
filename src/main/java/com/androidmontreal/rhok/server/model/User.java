package com.androidmontreal.rhok.server.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>
 * The user entity.
 * <p>
 * First approach we'll mix entity definition annotations alongside Hibernate Validator JSR 303 annotations. Let's see
 * if that scales, if not we might need intermediary objects for different jobs (user creation 'orders' and such)
 */
@Entity
@Table(name = "USER")
@XmlRootElement
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	// When archived, the user is basically soft-deleted.
	private Boolean archived = false;

	private Boolean confirmed = false;

	// The id of the inviting user, if applicable.
	@ManyToOne
	private User referal;

	/**
	 * For now we won't use it in authentication scheme, only as a way to let users communicate with each other without
	 * exposing their email addresses,
	 */
	// @NotNull
	// @Pattern(regexp = "^[a-z0-9_-]{3,15}$")
	private String username;

	@NotNull
	// Original example had a CR/LF in there, I don't think that was valid...
	@Pattern(regexp = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
	// TODO: set a sensical error message for user.
	private String email;

	// TODO: This should actually be set by the email-confirmation mailing process.
	private Date lastEmailCheck = new Date();

	// @NotNull
	private String firstName;

	// @NotNull
	private String lastName;

	// FIXME: Have not decided on hash type yet.
	@NotNull
	// TODO: set a sensical error message for user.
	// "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{6,20})" this is excessive force.
	// Going with simpler number/lowercase/uppercase requirement.
	@Pattern(regexp = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{6,20})")
	private String password;

	public Boolean getArchived() {
		return archived;
	}

	public Boolean getConfirmed() {
		return confirmed;
	}

	public String getEmail() {
		return email;
	}

	public String getFirstName() {
		return firstName;
	}

	public Long getId() {
		return id;
	}

	public Date getLastEmailCheck() {
		return lastEmailCheck;
	}

	public String getLastName() {
		return lastName;
	}

	public String getPassword() {
		return password;
	}

	public User getReferal() {
		return referal;
	}

	public String getUsername() {
		return username;
	}

	public void setArchived(Boolean archived) {
		this.archived = archived;
	}

	public void setConfirmed(Boolean confirmed) {
		this.confirmed = confirmed;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setLastEmailCheck(Date lastEmailCheck) {
		this.lastEmailCheck = lastEmailCheck;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setReferal(User referer) {
		this.referal = referer;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
