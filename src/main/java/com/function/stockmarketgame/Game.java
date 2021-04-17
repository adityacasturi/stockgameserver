package com.function.stockmarketgame;

import java.sql.Date;
import java.util.List;


public class Game {
    private String id;
    private String name;
    private int initialBuyingPower;
    private Date startTime;
    private Date endTime;
    private List<UserPortfolio> portfolios;

    public Game() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getInitialBuyingPower() {
        return initialBuyingPower;
    }

    public void setInitialBuyingPower(int initialBuyingPower) {
        this.initialBuyingPower = initialBuyingPower;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public List<UserPortfolio> getPortfolios() {
        return portfolios;
    }

    public void setPortfolios(List<UserPortfolio> portfolios) {
        this.portfolios = portfolios;
    }
}
