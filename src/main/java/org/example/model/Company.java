package org.example.model;

public class Company {
  private final String myName;
  private final String myCity;

  public Company(String name, String city) {
    myName = name;
    myCity = city;
  }

  public String getName() {
    return myName;
  }

  public String getCity() {
    return myCity;
  }
}
