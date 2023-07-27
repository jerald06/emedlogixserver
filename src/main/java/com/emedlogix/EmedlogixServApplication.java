package com.emedlogix;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Configuration
@EnableJpaRepositories(basePackages = "com.emedlogix")
@EnableElasticsearchRepositories(basePackages = "com.emedlogix")
@ComponentScan(basePackages = {"com.emedlogix"})
public class EmedlogixServApplication {

    public static void main(String[] args) {

        SpringApplication.run(EmedlogixServApplication.class, args);
    }

    @Bean(destroyMethod = "close")
    public RestHighLevelClient restClient() {

        RestClientBuilder builder = RestClient.builder(new HttpHost("localhost", 9200, "http"))
                // .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
                .setDefaultHeaders(compatibilityHeaders());
        return new RestHighLevelClient(builder);
    }

    private Header[] compatibilityHeaders() {
        return new Header[]{new BasicHeader(HttpHeaders.ACCEPT, "application/json;compatible-with=7"),
                new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json;compatible-with=7")};
    }

}
