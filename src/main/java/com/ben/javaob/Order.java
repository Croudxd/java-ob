package com.ben.javaob;

import java.util.Random;

public class Order
{
    enum type
    {
        BUY,
        SELL,
    }

    protected int id;
    protected Long price;
    protected Long amount;
    protected type _type;

    public Order()
    {

    }

    public static class Pool
    {
        private final Order[] pool;
        private int index = 0;


        public Pool(int size)
        {
            pool = new Order[size];
            for (int i =0; i < size; i++)
            {
                pool[i] = new Order();
            }
        }

        public Order acquire(Long price, Long amount, Order.type _type)
        {
            Order ord = pool[index++ %pool.length];
            ord.price = price;
            ord.amount = amount;
            Random rand = new Random();
            ord.id = rand.nextInt(Integer.MAX_VALUE);
            ord._type = _type;
            return ord;
        }

    }
}

