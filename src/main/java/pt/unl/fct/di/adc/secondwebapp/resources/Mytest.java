package pt.unl.fct.di.adc.secondwebapp.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


@Path("/Test")


public class Mytest {
    @GET
	@Produces(MediaType.TEXT_PLAIN)
    @Path("sample")
	public String Test()
	{
		
	return "test";
	}
}
