package com.ben.javaob;

import java.util.Random;

public class Order
{
    enum type
    {
        BUY,
        SELL,
    }

    int id;
    Double price;
    Double amount;
    type _type;


    public Order(Double price, Double amount, type _type)
    {
        // Just use random numbers for ID could be implemented better.
        Random rand = new Random();
        this.id = rand.nextInt(Integer.MAX_VALUE);
        this.price = price;
        this.amount = amount;
        this._type = _type;
    }
}

