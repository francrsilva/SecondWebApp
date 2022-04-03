package pt.unl.fct.di.adc.secondwebapp.resources;


import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.StringValue;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;

import pt.unl.fct.di.adc.secondwebapp.resources.LoginResource;
import pt.unl.fct.di.adc.secondwebapp.util.AuthToken;
import pt.unl.fct.di.adc.secondwebapp.util.LoginData;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {
	
	
	/**
	 * A Logger Object
	 */
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

	
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	
	private final Gson g = new Gson();
	
	public LoginResource() {}	//Nothing to be done here(could be omitted)

	
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response loginWithToken2(LoginData data ,
			@Context HttpServletRequest request,
			@Context HttpHeaders headers) {
		LOG.fine("Login attempt by user: "+ data.username);
		
		//KEYS SHOULD BE GENERATED OUTSIDE TRANSACTIONS
		//Construct the key from the username
		Key userKey = datastore.newKeyFactory().setKind("user").newKey(data.username);
		Key ctrsKey = datastore.newKeyFactory()
				.addAncestor(PathElement.of("User", data.username))
				.setKind("UserStats").newKey("counters");
		//Generate automatically a key
		Key logKey = datastore.allocateId(datastore.newKeyFactory()
				.addAncestor(PathElement.of("User", data.username))
				.setKind("UserLog").newKey());
		
		Transaction txn = datastore.newTransaction();
		try {
			Entity user = txn.get(userKey);
			if(user==null) {
				//username does not exist
				LOG.warning("Failed attempt to login, user '"+data.username+"' does not exist.");
				return Response.status(Status.FORBIDDEN).build();
			}
			
			//We get the stats from the storage
			Entity stats = txn.get(ctrsKey);
			if(stats == null) {
				stats = Entity.newBuilder(stats)
						.set("User_stats_logins", 0L)
						.set("User_stats_fails", 0L)
						.set("user_first_login", Timestamp.now())
						.set("user_last_login", Timestamp.now())
						.build();
			}
		
			String hashedPWD = (String) user.getString("user_pwd");
			if(hashedPWD.equals(DigestUtils.sha512Hex(data.password))) {
				//Password is correct
				//Construct the logs
				Entity log = Entity.newBuilder(logKey)
						.set("user_login_ip", request.getRemoteAddr())
						.set("user_login_local", request.getRemoteHost())
						.set("user_login_latlon", 
								//Does not index this property value
								StringValue.newBuilder(headers.getHeaderString("X-AppEngine-CityLatLong"))
								.setExcludeFromIndexes(true).build()
								)
						.set("user_login_city", headers.getHeaderString("X-AppEngine-City"))
						.set("user_login_country", headers.getHeaderString("X-AppEngine-Country"))
						.set("user_login_time", Timestamp.now())
						.build();
				//Get the user statistics and updates it
				//Copying information every time a user logs in maybe is not a good solution
				Entity ustats = Entity.newBuilder(ctrsKey)
						.set("user_stats_logins", 1L + stats.getLong("user_stats_login"))
						.set("user_stats_failed", 0L )
						.set("user_first_login", stats.getTimestamp("user_first_Login"))
						.set("user_last_login", Timestamp.now())
						.build();
				
				//batch operation
				txn.put(log,ustats);
				txn.commit();
				
				//return token
				AuthToken token = new AuthToken(data.username);
				LOG.info("User '"+ data.username+"' logged in sucessfully.");
				return Response.ok(g.toJson(token)).build();
				}else {
					//Incorrect password
					//Propose a better solution
					Entity ustats = Entity.newBuilder(ctrsKey)
							.set("user_stats_logins", stats.getLong("user_stats_logins"))
							.set("user_stats_failed", stats.getLong("user_stats_failed"))
							.set("user_first_login", stats.getLong("user_first_login"))
							.set("user_last_login", stats.getLong("user_last_login"))
							.set("user_last_login_attempt", Timestamp.now())
							.build();
					txn.put(ustats);
					txn.commit();
					LOG.warning("Wrong password for username: "+data.username);
					return Response.status(Status.FORBIDDEN).build();
				}
			}catch (Exception e){
				txn.rollback();
				LOG.severe(e.getMessage());
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}finally {
			if(txn.isActive()) {
				txn.rollback();
			}
		}
	}
}


