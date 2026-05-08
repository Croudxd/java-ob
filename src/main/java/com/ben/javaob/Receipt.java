package com.ben.javaob;


public class Receipt
{
    private int askid;
    private int buyid;
    private Double price;
    private Double amount;
    private Long time;

    public Receipt(int _askid, int _buyid, Double _price, Double _amount, Long _time)
    {
        this.askid = _askid;
        this.buyid = _buyid;
        this.price = _price;
        this.amount = _amount;
        this.time = _time;
    }

    @Override
    public String toString() {
        return String.format("askID: %s | bidID: %s | price: %.4f | amount: %.4f | time %d", askid, buyid, price, amount, time);
    }

}
