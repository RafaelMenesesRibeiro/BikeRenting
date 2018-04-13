package org.binas.domain;

import org.binas.exception.UserException;

public class User {

	/** Station identifier. */
	private String email;
	private int credit;
	private String emailFormat = "^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+.[a-zA-Z0-9-.]+$";
	private boolean hasBike = false;


	public User(String email, int credit) throws UserException {
		checkEmail(email);
		this.email = email;
		setCredit(credit);
	}
	//verificar nos testes se email nao e null(=null), se nao e string vazia(""), se tem espaços a frente ou a tras (trim) do email e garantir que é um email (matches).

	public String getEmail(){
		return this.email;
	}

	public int getCredit(){
		return credit;
	}

	public boolean getHasBike(){
		return hasBike;
	}

	public void setCredit(int credit) throws UserException {
		if (credit < 0) {
			throw new UserException("O Saldo não pode ser negativo");
		} else { 
			this.credit = credit;
		}

	}

	public void setHasBike(boolean b){
		this.hasBike = b;
	}

	private void checkEmail(String email) throws UserException {
		if (email == null){
			throw new UserException("O e-mail não pode ser null");
		}
		email.trim();
		if (email == ""){
			throw new UserException("O e-mail não pode ser uma string vazia");
		} else if (!email.matches(emailFormat)) {
			throw new UserException("O e-mail tem de seguir o formato abc@gmail.com por exemplo");
		}
	}

}
