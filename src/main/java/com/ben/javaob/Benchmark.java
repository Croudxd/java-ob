package com.ben.javaob;

import java.util.Random;

public class Benchmark 
{
    public static void main( String[] args )
    {

        Double minPrice = 0.39;
        Double maxPrice = 0.42;
        Double minAmount = 0.0;
        Double maxAmount = 0.3;
        Orderbook book = new Orderbook();
        Order.Pool pool = new Order.Pool(1024);

        for (int i = 0; i < 100_000; i++)
        {

            Random rand = new Random();
            Double price = rand.nextDouble(minPrice, maxPrice);
            Double amount = rand.nextDouble(minAmount, maxAmount);
            boolean type = rand.nextBoolean();
            Order.type ty = Order.type.BUY;
            if (type)
            {
                ty = Order.type.SELL;
            }
            book.GOC(pool.acquire(Math.round(price * 10000f), Math.round(amount * 10000f), ty));
        }


        long goc_total = 0;
        { 
            for (int i =0; i < 1000; i++)
            {
                Random rand = new Random();
                Double price = rand.nextDouble(minPrice, maxPrice);
                Double amount = rand.nextDouble(minAmount, maxAmount);
                boolean type = rand.nextBoolean();
                Order.type ty = Order.type.BUY;
                if (type)
                {
                    ty = Order.type.SELL;
                }
                long start = System.nanoTime();
                book.GOC(pool.acquire(Math.round(price * 10000f), Math.round(amount * 10000f), ty));
                long end = System.nanoTime();
                goc_total += end - start ;
            }

        }

        long total = 0;
        for (int i =0; i < 1000; i++)
        {

            Random rand = new Random();
            Double price = rand.nextDouble(minPrice, maxPrice);
            Double amount = rand.nextDouble(minAmount, maxAmount);
            boolean type = rand.nextBoolean();
            Order.type ty = Order.type.BUY;
            if (type)
            {
                ty = Order.type.SELL;
            }
            long start = System.nanoTime();
            book.FOK(pool.acquire(Math.round(price * 10000f), Math.round(amount * 10000f), ty));
            long end = System.nanoTime();
            total += end - start ;
        }

        System.out.print("goc:");
        System.out.print(((double) goc_total / 1000 ) / 1000);
        System.out.print(" uqs");
        System.out.println();
        System.out.print("FOK:");
        System.out.print(((double) total / 1000 ) / 1000);
        System.out.print(" uqs");
        System.out.println();
    }
}
