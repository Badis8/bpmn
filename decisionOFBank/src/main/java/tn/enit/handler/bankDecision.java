package tn.enit.handler;

import java.io.FileNotFoundException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProvider;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;

public class bankDecision  implements JobHandler {
    private static final String MESSAGE_NAME = "articlesToSend";

    private static final String ZEEBE_ADDRESS="9c47333b-d9fc-4f05-92c4-5d21c0b5d55e.bru-2.zeebe.camunda.io:443";
	private static final String ZEEBE_CLIENT_ID="zm7MAUM2.SdCEqyGnaBGgTg0y0~7OQVP";
	private static final String ZEEBE_CLIENT_SECRET="VwSnFrbQUsxhEs6mGP4cMs7xmjrmy~-FwLVBQUJYqar_MFng_~DUIo.3NBHfvinG";
	private static final String ZEEBE_AUTHORIZATION_SERVER_URL="https://login.cloud.camunda.io/oauth/token";
	private static final String ZEEBE_TOKEN_AUDIENCE="zeebe.camunda.io";
    @Override
    public void handle(JobClient client, ActivatedJob job) throws Exception {
        String csvFilePath = "src\\main\\resources\\cartes.csv";
        final String articlesToSend = "verificationBanque";
 

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
                    final String code = (String) inputVariables.get("articlesToSend");
                    String card="";
                    double balance=0;
        try (Scanner scanner = new Scanner(new File(csvFilePath))) {
            // Skip the header line (assuming the first line contains column headers)
            if (scanner.hasNextLine()) {
                scanner.nextLine(); // Skip the header line
            }
            // Read each line of the CSV file
            while (scanner.hasNextLine() ) {
                String line = scanner.nextLine();
                String[] fields = line.split(",");
                System.out.println(line);
                card = fields[0].trim();
                balance = Double.parseDouble(fields[1].trim());

                if (card.equals(code))
                    break;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }                       
            final Map<String, Object> messageVariables = new HashMap<String, Object>();
            double price=(double) inputVariables.get("totalprice");
            if (card.equals(code))
            {
            if(balance>price )
            {

                messageVariables.put("payement",true);
                System.out.println(articlesToSend + " payement efféctué avec succés le  solde "+balance+" est supérieur à la somme debitée "+ price);
            }
            else    
            {
                System.out.println(articlesToSend + " echec de payement "+balance+" est inferieur à la somme debitée "+ price);                
                messageVariables.put("payement",false);
            }
        }
        else{
            throw new Exception("Carte de credit non existante");
        }


            //Complete the Job
            client.newCompleteCommand(job.getKey())
            .variables(messageVariables)
            .send()
            .join();
}
        }
    }
 