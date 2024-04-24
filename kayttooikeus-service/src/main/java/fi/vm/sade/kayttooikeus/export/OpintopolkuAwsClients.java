package fi.vm.sade.kayttooikeus.export;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@Configuration
public class OpintopolkuAwsClients {
    static final Region REGION = Region.EU_WEST_1;

    @Bean
    AwsCredentialsProvider opintopolkuCredentialsProvider() {
        return DefaultCredentialsProvider.create();
    }

    @Bean
    S3AsyncClient opintopolkuS3Client(AwsCredentialsProvider opintopolkuCredentialsProvider) {
        return S3AsyncClient.builder()
                .credentialsProvider(opintopolkuCredentialsProvider)
                .region(REGION)
                .build();
    }
}
