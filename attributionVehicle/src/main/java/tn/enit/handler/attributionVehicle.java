package tn.enit.handler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProvider;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;

public class attributionVehicle  implements JobHandler {
    private static final String MESSAGE_NAME = "articlesToSend";
    int intValue =0;
    
    private static final String ZEEBE_ADDRESS="9c47333b-d9fc-4f05-92c4-5d21c0b5d55e.bru-2.zeebe.camunda.io:443";
	private static final String ZEEBE_CLIENT_ID="zm7MAUM2.SdCEqyGnaBGgTg0y0~7OQVP";
	private static final String ZEEBE_CLIENT_SECRET="VwSnFrbQUsxhEs6mGP4cMs7xmjrmy~-FwLVBQUJYqar_MFng_~DUIo.3NBHfvinG";
	private static final String ZEEBE_AUTHORIZATION_SERVER_URL="https://login.cloud.camunda.io/oauth/token";
	private static final String ZEEBE_TOKEN_AUDIENCE="zeebe.camunda.io";
    @Override
    public void handle(JobClient client, ActivatedJob job) throws Exception {
        
        final String articlesToSend = "verificationBanque";
        final Map<String, Object> messageVariables = new HashMap<>();


        final OAuthCredentialsProvider credentialsProvider =
                new OAuthCredentialsProviderBuilder()
                        .authorizationServerUrl(ZEEBE_AUTHORIZATION_SERVER_URL)
                        .audience(ZEEBE_TOKEN_AUDIENCE)
                        .clientId(ZEEBE_CLIENT_ID)
                        .clientSecret(ZEEBE_CLIENT_SECRET)
                        .build();
        try (final ZeebeClient travelAgencyClient = ZeebeClient.newClientBuilder()
                .gatewayAddress(ZEEBE_ADDRESS)
                .credentialsProvider(credentialsProvider)
                .build()) {
                    try {
                        // Define the REST API endpoint URL
                        String apiUrl = "http://localhost:8080/disponible";
            
                        URL url = new URL(apiUrl);
            
                        // Create HttpURLConnection
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");
                        conn.setRequestProperty("Content-Type", "application/json");
            
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String response = reader.readLine(); // Assuming the response is a single line with "true" or "false"
                        reader.close();
            
                        // Parse response as a boolean
                        boolean isAvailable = Boolean.parseBoolean(response.trim());
            
                        System.out.println("Response from API:");
                        System.out.println(response);
                        
                        // Disconnect the HttpURLConnection
                        conn.disconnect();
                        
                        // Set the disponible variable based on the API response
                        messageVariables.put("disponible", isAvailable);
                       if (isAvailable)
                            System.out.println(articlesToSend + " Un transporteur est disponible et les produits sont transportés ");
                       else
                            System.out.println(articlesToSend + " Aucun transporteur n'est disponible");

            
                    } catch (IOException e) {
                        e.printStackTrace();
                        // Handle API connection or response reading errors
                        messageVariables.put("disponible", false); // Set disponible to false on error
                    }
                }




            //Complete the Job
            client.newCompleteCommand(job.getKey())
            .variables(messageVariables)
            .send()
            .join();
}
        }
    
 