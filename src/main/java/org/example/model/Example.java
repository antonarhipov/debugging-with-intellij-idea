package org.example.model;

import java.util.List;

public class Example {
    public static void main(String[] args) {
        List<Client> clients = List.of(
                buildClient("Anton", "Arhipov", "@antonarhipov", "JetBrains", "Tallinn")
                , buildClient("John", "Doe", "@john", "JetBrains", "Tallinn")
                , buildClient("Jim", "Jake", "@jim", "JetBrains", "Tallinn")
                , buildClient("Vello", "Tamm", "@vello", "JetBrains", "Tallinn")
                , buildClient("Mary", "Hummingbird", "@mary", "JetBrains", "Tallinn")
                , buildClient("Jane", "Smith", "@jane", "JetBrains", "Tallinn")
                , buildClient("Monica", "Lill", "@monica", "JetBrains", "Tallinn")
                , buildClient("Mike", "Hern", "@mike", "JetBrains", "Tallinn")
        );
        System.out.println(clients);
    }

    private static Client buildClient(String firstName, String lastName, String handle, String company, String city) {
        final ClientBuilder builder = new ClientBuilder();

        builder.setFirstName(firstName);
        builder.setLastName(lastName);

        final TwitterBuilder twitterBuilder = new TwitterBuilder();
        twitterBuilder.setHandle(handle);
        builder.setTwitter(twitterBuilder.build());

        final CompanyBuilder companyBuilder = new CompanyBuilder();
        companyBuilder.setName(company);
        companyBuilder.setCity(city);
        builder.setCompany(companyBuilder.build());


        final Client client = builder.build();
        System.out.println("Created client is: " + client);
        return client;
    }
}










