package org.acme;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Random;

@Path("/disponible")
public class GreetingResource {



    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public boolean isDisponible() {
        Random random = new Random();
        int max = 50;
        int min = 10;

 
        int randomNum = random.nextInt(max - min + 1) + min;

        return (randomNum % 2 == 0);
    }
}
