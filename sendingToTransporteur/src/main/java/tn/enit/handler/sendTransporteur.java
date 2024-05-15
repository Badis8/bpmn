package tn.enit.handler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

 

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProvider;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;

public class sendTransporteur  implements JobHandler {
    private static final String MESSAGE_NAME = "receptionConteneurs";

    private static final String ZEEBE_ADDRESS="9c47333b-d9fc-4f05-92c4-5d21c0b5d55e.bru-2.zeebe.camunda.io:443";
	private static final String ZEEBE_CLIENT_ID="zm7MAUM2.SdCEqyGnaBGgTg0y0~7OQVP";
	private static final String ZEEBE_CLIENT_SECRET="VwSnFrbQUsxhEs6mGP4cMs7xmjrmy~-FwLVBQUJYqar_MFng_~DUIo.3NBHfvinG";
	private static final String ZEEBE_AUTHORIZATION_SERVER_URL="https://login.cloud.camunda.io/oauth/token";
	private static final String ZEEBE_TOKEN_AUDIENCE="zeebe.camunda.io";
    @Override
    public void handle(JobClient client, ActivatedJob job) throws Exception {
        
        final String transporteur = "receptionConteneurs";
 

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

                    final Map<String, Object> inputVariables = job.getVariablesAsMap();
                    final String products = (String) inputVariables.get("demandeRecu");
            //Build the Message Variables
        final Map<String, Object> messageVariables = new HashMap<String, Object>();
        
        messageVariables.put("articlesToSend", products);
 
 
        travelAgencyClient.newPublishMessageCommand()
                .messageName(MESSAGE_NAME)
                .correlationKey(transporteur)
                .variables(messageVariables)
                .send()
                .join();

        System.out.println(transporteur + ": Les articles "+products+" ont été emballés et organisés maintenant je vais les envoyer au service de transport");

            //Complete the Job
        client.newCompleteCommand(job.getKey()).send().join();
        }
    }
    }