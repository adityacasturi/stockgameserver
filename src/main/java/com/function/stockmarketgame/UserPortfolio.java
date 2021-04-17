package com.function.stockmarketgame;

import java.util.List;

public class UserPortfolio {
    private String id;
    private String userId;
    private String gameId;
    private int buyingPower;
    private List<Position> positions;

    public UserPortfolio() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public int getBuyingPower() {
        return buyingPower;
    }

    public void setBuyingPower(int buyingPower) {
        this.buyingPower = buyingPower;
    }

    public List<Position> getPositions() {
        return positions;
    }

    public void setPositions(List<Position> positions) {
        this.positions = positions;
    }
}
