package src.config;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.drive.Drive;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;

@Configuration
public class GoogleDriveConfig {

    @Bean
    public Drive getDriveService() throws GeneralSecurityException, IOException {
        final HttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        GoogleCredential googleCredential = googleCredential();

        return new Drive.Builder(HTTP_TRANSPORT,
                JacksonFactory.getDefaultInstance(), googleCredential)
                .build();
    }

    @Bean
    public GoogleCredential googleCredential() throws GeneralSecurityException, IOException {
        Collection<String> scopes = new ArrayList<>();
        scopes.add("https://www.googleapis.com/auth/drive");

        HttpTransport httpTransport = new NetHttpTransport();
        JacksonFactory jsonFactory = new JacksonFactory();

        return new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .setServiceAccountId("onlinelearning@onlinelearning-402414.iam.gserviceaccount.com")
                .setServiceAccountScopes(scopes)
                .setServiceAccountPrivateKeyFromP12File(new File("D:\\hoctap\\github\\Online\\src\\onlinelearning-402414-ad146f37a49d.p12"))
                .build();
    }
}


