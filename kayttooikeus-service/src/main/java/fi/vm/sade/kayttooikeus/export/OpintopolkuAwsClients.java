package fi.vm.sade.kayttooikeus.export;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@Configuration
public class OpintopolkuAwsClients {
    static final Region REGION = Region.EU_WEST_1;

    @Value("${kayttooikeus.tasks.export.use-crt-client:false}")
    private boolean useCrtClient;

    @Bean
    AwsCredentialsProvider opintopolkuCredentialsProvider() {
        return DefaultCredentialsProvider.create();
    }

    @Bean
    S3AsyncClient opintopolkuS3Client(AwsCredentialsProvider opintopolkuCredentialsProvider) {
        if (!useCrtClient) {
            return S3AsyncClient.builder()
                    .credentialsProvider(opintopolkuCredentialsProvider)
                    .region(REGION)
                    .build();
        } else {
            return S3AsyncClient.crtBuilder()
                    .credentialsProvider(opintopolkuCredentialsProvider)
                    .region(REGION)
                    .build();
        }
    }
}
