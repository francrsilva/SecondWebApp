package pt.unl.fct.di.adc.secondwebapp.resources;

import java.util.logging.Logger;


import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.adc.secondwebapp.util.RegisterData;


@Path("/register")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class Register {
	/*
	 * A Logger object
	 */
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();	
	
	
			@GET
			@Path("/Test")
			@Produces(MediaType.TEXT_PLAIN)
			public String test() {
				return "test";
			}	
	
	
			@POST
			@Path("/register")
			@Consumes(MediaType.APPLICATION_JSON)
			public Response doRegistration(RegisterData data) {
				LOG.fine("Attempt to register user:" + data.username);

				//check input data
				if(!data.validEmail()) {
					return Response.status(Status.BAD_REQUEST).entity("This email is not in the correct format").build();
				}else if(!data.validPWD()) {
					return Response.status(Status.BAD_REQUEST).entity("This password is too short!").build();
				}else if(!data.passwordEqualsConfirmation()) {
					return Response.status(Status.BAD_REQUEST).entity("The password and its confirmation are not equal.").build();
				}else {
				
					Transaction txn =  datastore.newTransaction();
					try {
						Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
						Entity user = txn.get(userKey);
						if(user != null) {
							txn.rollback();
							return Response.status(Status.BAD_REQUEST).entity("User already exists.").build();
						}else {
							user = Entity.newBuilder(userKey)
									.set("user_email", data.email)
									.set("user_name", data.name)
									.set("user_pwd", DigestUtils.sha3_512Hex(data.password))
									.set("user_confirmation", data.confirmation)
									.set("user_creation_time", Timestamp.now())
									.build();
							txn.add(user);
							LOG.info("User registered!" + data.username);
							txn.commit();
							return Response.ok("{}").build();
						}
					}finally {
						if(txn.isActive()) {
							txn.rollback();
						}
					}
				}	
		}
}

