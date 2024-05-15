package tn.enit.handler;

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

public class PriceHandler  implements JobHandler {
    private static final String MESSAGE_NAME = "articlesToSend";

    private static final String ZEEBE_ADDRESS="9c47333b-d9fc-4f05-92c4-5d21c0b5d55e.bru-2.zeebe.camunda.io:443";
	private static final String ZEEBE_CLIENT_ID="zm7MAUM2.SdCEqyGnaBGgTg0y0~7OQVP";
	private static final String ZEEBE_CLIENT_SECRET="VwSnFrbQUsxhEs6mGP4cMs7xmjrmy~-FwLVBQUJYqar_MFng_~DUIo.3NBHfvinG";
	private static final String ZEEBE_AUTHORIZATION_SERVER_URL="https://login.cloud.camunda.io/oauth/token";
	private static final String ZEEBE_TOKEN_AUDIENCE="zeebe.camunda.io";
    @Override
    public void handle(JobClient client, ActivatedJob job) throws Exception {
        String csvFilePath = "src\\main\\resources\\prices.csv";
        final String articlesToSend = "countPrice";
 

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
                    final String code = (String) inputVariables.get("article");

        System.out.println(code);   
            //Build the Message Variables
        final Map<String, Object> messageVariables = new HashMap<String, Object>();

        messageVariables.put("verification",true);
        String itemCode="";
        String itemName="";
        double price=0;
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
                itemCode = fields[0].trim();
                itemName = fields[1].trim();
                price = Double.parseDouble(fields[2].trim());

                if (itemCode.equals(code))
                    break;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }           
        double tp=0;
        if(messageVariables.get("totalprice") != null)
        {
          tp=(double)messageVariables.get("totalprice");
        }
        System.out.println(tp);   

        String pdts="";
        if(messageVariables.get("products") != null)
        {
            pdts=(String)messageVariables.get("products");
        }
        System.out.println(pdts);   

        messageVariables.put("totalprice",tp+ price);
        messageVariables.put ("products",pdts+", "+itemName);

        messageVariables.put ("finished",(Boolean)inputVariables.get("finished"));
        System.out.println(articlesToSend + " L'article: " + itemCode + " est ajouté au panier avec le nom: "+ itemName +" et le prix: "+ price );

            //Complete the Job
            client.newCompleteCommand(job.getKey())
            .variables(messageVariables)
            .send()
            .join();
}
        }
    }
 