package fr.iolabs.leaf.files;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Region;

@Configuration
public class AWSClientConfig {

    @Value("${leaf.filestorage.s3.accessKey}")
    private String accessKey;

    @Value("${leaf.filestorage.s3.secretAccessKey}")
    private String secretAccessKey;

    @Value("${leaf.filestorage.s3.bucketName}")
    private String bucketName;

    private final Region region = Region.EU_Paris;

    @Bean
    public AmazonS3 amazonS3() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretAccessKey);

        return AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(region.toString())
                .build();
    }

	public String getAccessKey() {
		return accessKey;
	}

	public String getSecretAccessKey() {
		return secretAccessKey;
	}

	public String getBucketName() {
		return bucketName;
	}

	public Region getRegion() {
		return region;
	}
}