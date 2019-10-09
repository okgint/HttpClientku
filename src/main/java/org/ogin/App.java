package org.ogin;

import org.ogin.body.HttpBody;
import org.ogin.client.HttpClient;
import org.ogin.client.impl.ApacheHttpClient;
import org.ogin.message.Request;
import org.ogin.message.Response;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException {
        HttpClient httpClient = new ApacheHttpClient();
        Request request = new Request("GET", "https://www.tutorialspoint.com/", null, null);
        Response response = httpClient.execute(request, null);
        HttpBody httpBody = response.getBody();
        httpBody.writeContentTo(new FileOutputStream("oke.txt"));
    }
}
