package pt.unl.fct.di.adc.secondwebapp.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javassist.bytecode.analysis.Type;

public class RegisterData {

	public String username;
	public String email;
	public String name;
	public String password;
	public String confirmation; 
	public String perfil;
	public String state;
	public String role;
	
	//estas variaveis nao vêem na inicializacao do user
	//se calhar nao podem tar aqui
	public long telFix;
	public long telMov;
	public String address;
	public long NIF;
	
	public RegisterData() {
		
	}

	public RegisterData(String username, String email, String name, String password, String confirmation) {
		this.username = username;
		this.email = email;
		this.name = name;
		this.password = password;
		this.confirmation = confirmation;
		
		this.state = "INATIVO";
		this.role = "USER";
		
	}
	
	public RegisterData(String username, String email, String name, String password, String confirmation,
			String perfil, String telFix, String telMov, String morada, String NIF) {
		this.username = username;
		this.email = email;
		this.name = name;
		this.password = password;
		this.confirmation = confirmation;
		
		
		
		this.state = "INATIVO";
		this.role = "USER";
		
	}
	
	
	
	
	
	
	public boolean isActive() {
		return state.equals("ATIVO");
	}
}
