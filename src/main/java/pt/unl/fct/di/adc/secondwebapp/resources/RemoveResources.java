package pt.unl.fct.di.adc.secondwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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

import pt.unl.fct.di.adc.secondwebapp.util.DeleteData;


 
@Path("/remove")
@Consumes(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RemoveResources {

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteUser(DeleteData data) {
	
	LOG.fine("Attempt of user"+data.usernameA+ "to delete user:" + data.usernameB);
	
	Key userKeyA = datastore.newKeyFactory().setKind("User").newKey(data.usernameA);
	Key userKeyB = datastore.newKeyFactory().setKind("User").newKey(data.usernameB);
	
	Transaction txn =  datastore.newTransaction();
		try {
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.usernameA);
			Entity userA = txn.get(userKeyA);
			Entity userB = txn.get(userKeyB);
			if(userB == null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User does not exists.").build();
				//permissoes de remocao (ver dos roles
		//	}else if(userA. ){
				
			
			}
			else {
				
				txn.delete(userKeyB);
				LOG.info("User deleted!");
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