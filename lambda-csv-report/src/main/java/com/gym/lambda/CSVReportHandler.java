package com.gym.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.time.LocalDate;
import java.util.Map;

public class CSVReportHandler implements RequestHandler<Object, String> {

    private static final String TABLE_NAME = "SpringGym-TrainerWorkload";
    private static final String BUCKET_NAME = "springgym-reports";
    private static final Region REGION = Region.EU_NORTH_1;

    @Override
    public String handleRequest(Object input, Context context) {
        context.getLogger().log("Starting CSV Report generation...\n");

        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear());
        String month = String.valueOf(now.getMonthValue());

        // Scan DynamoDB table
        DynamoDbClient dynamoDb = DynamoDbClient.builder().region(REGION).build();
        ScanResponse scanResponse = dynamoDb.scan(
                ScanRequest.builder().tableName(TABLE_NAME).build()
        );

        // Build CSV
        StringBuilder csv = new StringBuilder();
        csv.append("Trainer First Name,Trainer Last Name,Training Duration (mins)\n");

        int trainerCount = 0;
        for (Map<String, AttributeValue> item : scanResponse.items()) {
            String firstName = item.get("FirstName").s();
            String lastName = item.get("LastName").s();

            // Navigate Years -> year -> month to get duration
            int duration = 0;
            if (item.containsKey("Years") && item.get("Years").m() != null) {
                Map<String, AttributeValue> years = item.get("Years").m();
                if (years.containsKey(year) && years.get(year).m() != null) {
                    Map<String, AttributeValue> months = years.get(year).m();
                    if (months.containsKey(month) && months.get(month).n() != null) {
                        duration = Integer.parseInt(months.get(month).n());
                    }
                }
            }

            // Skip trainers with 0 duration
            if (duration == 0) {
                context.getLogger().log("Skipping trainer " + firstName + " " + lastName + " (0 duration)\n");
                continue;
            }

            csv.append(firstName).append(",").append(lastName).append(",").append(duration).append("\n");
            trainerCount++;
        }

        // Generate report file name: Trainers_Trainings_summary_YYYY_MM
        String reportName = String.format("Trainers_Trainings_summary_%s_%02d.csv",
                year, now.getMonthValue());

        // Upload to S3
        S3Client s3 = S3Client.builder().region(REGION).build();
        s3.putObject(
                PutObjectRequest.builder()
                        .bucket(BUCKET_NAME)
                        .key(reportName)
                        .contentType("text/csv")
                        .build(),
                RequestBody.fromString(csv.toString())
        );

        String result = String.format("Report '%s' uploaded to S3 bucket '%s' with %d trainers",
                reportName, BUCKET_NAME, trainerCount);
        context.getLogger().log(result + "\n");

        dynamoDb.close();
        s3.close();

        return result;
    }
}
