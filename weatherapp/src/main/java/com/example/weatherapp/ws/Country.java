package com.example.weatherapp.ws;

public class Country {

  private String name;
  private String capital;
  private Currency currency;
  private int population;

  public int getPopulation() {
    return population;
  }

  public void setPopulation(int population) {
    this.population = population;
  }


  public Currency getCurrency() {
    return currency;
  }

  public void setCurrency(Currency currency) {
    this.currency = currency;
  }


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCapital() {
    return capital;
  }

  public void setCapital(String capital) {
    this.capital = capital;
  }


}
