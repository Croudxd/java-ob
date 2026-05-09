package com.ben.javaob;


import java.util.Random;

public class Receipt
{
    int askid;
    int buyid;
    Long price;
    Long amount;
    Long time;

    public Receipt() {}

    @Override
    public String toString() {
        return String.format("askID: %s | bidID: %s | price: %.4f | amount: %.4f | time %d", askid, buyid, price / 10000.0, amount / 10000.0, time);
    }

    public static class Pool
    {
        final Receipt[] pool;
        int index = 0;


        public Pool(int size)
        {
            pool = new Receipt[size];
            for (int i =0; i < size; i++)
            {
                pool[i] = new Receipt();
            }
        }

        public Receipt acquire(int _askid, int _buyid, Long _price, Long _amount, Long _time)
        {
            Receipt rec = pool[index++ %pool.length];
            rec.askid = _askid;
            rec.buyid = _buyid;
            rec.price = _price;
            rec.amount = _amount;
            rec.time = _time;
            return rec;
        }

    }

}
