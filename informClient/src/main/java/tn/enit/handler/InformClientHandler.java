package tn.enit.handler;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

 

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProvider;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;

public class InformClientHandler  implements JobHandler {
    private static final String MESSAGE_NAME = "paimentFailed";

    private static final String ZEEBE_ADDRESS="9c47333b-d9fc-4f05-92c4-5d21c0b5d55e.bru-2.zeebe.camunda.io:443";
	private static final String ZEEBE_CLIENT_ID="zm7MAUM2.SdCEqyGnaBGgTg0y0~7OQVP";
	private static final String ZEEBE_CLIENT_SECRET="VwSnFrbQUsxhEs6mGP4cMs7xmjrmy~-FwLVBQUJYqar_MFng_~DUIo.3NBHfvinG";
	private static final String ZEEBE_AUTHORIZATION_SERVER_URL="https://login.cloud.camunda.io/oauth/token";
	private static final String ZEEBE_TOKEN_AUDIENCE="zeebe.camunda.io";
    @Override
    public void handle(JobClient client, ActivatedJob job) throws Exception {
        
        final String articlesToSend = "paimentFailed";
 

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
                    final String credit = (String) inputVariables.get("credit");
        String data ="The card number: "+ credit+" not found";  
        String filePath = "src\\main\\resources\\log.txt";

        try (FileOutputStream fos = new FileOutputStream(filePath);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            byte[] bytes = data.getBytes();
            bos.write(bytes);

            System.out.println("Data has been written to " + filePath);

        } catch (IOException e) {
            e.printStackTrace();
        }            

        travelAgencyClient.newPublishMessageCommand()
                .messageName(MESSAGE_NAME)
                .correlationKey(articlesToSend)
                .send()
                .join();

        System.out.println(articlesToSend + "L'erreur a été enregistrée et le client a été informé");

            //Complete the Job
        client.newCompleteCommand(job.getKey()).send().join();
        }
    }
    }