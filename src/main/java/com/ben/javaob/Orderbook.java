package com.ben.javaob;

import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Collections;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.List;

import com.ben.javaob.Order;
import com.ben.javaob.Order.type;
import com.ben.javaob.Receipt;

public class Orderbook
{
    TreeMap</*Price*/Double, /*Order object*/ArrayList<Order>> bids;
    TreeMap<Double, ArrayList<Order>> asks;
    ArrayDeque<Receipt> receipts;
    private TreeMap<Double, Double> askLiqudity;
    private TreeMap<Double, Double> bidLiqudity;

    public Orderbook()
    {
        bids = new TreeMap<>(Collections.reverseOrder());
        asks = new TreeMap<>();
        receipts = new ArrayDeque<>();

        askLiqudity = new TreeMap<>();
        bidLiqudity = new TreeMap<>();
    }

    /**
     * Function returns 0 for success, -2 if failed to match.
     **/
    public int match(Order ord)
    {

        Double remaining = ord.amount;
        List<Double> asksToRemove = new ArrayList<>();
        List<Double> bidsToRemove = new ArrayList<>();
        if (ord._type == type.BUY)
        {
            for (Double price : asks.keySet()) 
            {

                if (ord.price == 0) ord.price = price;
                if (price > ord.price ) break;
                ArrayList<Order> list = asks.get(price);
                if (list.isEmpty()) asksToRemove.add(price);
                while (remaining > 0 && !list.isEmpty())
                {
                    Order ask = list.getFirst();
                    Double am = ask.amount;
                    if (am - remaining < 0.0)
                    {
                        list.removeFirst();
                        receipts.add(new Receipt(ask.id, ord.id, ask.price, ask.amount, Instant.now().toEpochMilli()));
                        remaining -= am;
                        askLiqudity.merge(ask.price, -am, Double::sum);
                    }
                    else
                    {
                        ask.amount -= remaining;
                        receipts.add(new Receipt(ask.id, ord.id, ask.price, remaining, Instant.now().toEpochMilli()));
                        askLiqudity.merge(ask.price, -remaining, Double::sum);
                        remaining = 0.0;
                    }
                }

            }
        }
        if (ord._type == type.SELL)
        {
            for (Double price : bids.keySet()) 
            {
                if (ord.price == 0) ord.price = price;
                if (price < ord.price) break;
                ArrayList<Order> list = bids.get(price);
                if (list.isEmpty()) bidsToRemove.add(price);
                while (remaining > 0 && !list.isEmpty())
                {
                    Order bid = list.getFirst();
                    Double bm = bid.amount;
                    if (bm - remaining < 0.0)
                    {
                        list.removeFirst();
                        receipts.add(new Receipt(ord.id, bid.id, bid.price, bid.amount, Instant.now().toEpochMilli()));
                        remaining -= bm;
                        bidLiqudity.merge(bid.price, -bm, Double::sum);
                    }
                    else
                    {
                        bid.amount -= remaining;
                        receipts.add(new Receipt(ord.id, bid.id, bid.price, remaining, Instant.now().toEpochMilli()));
                        bidLiqudity.merge(bid.price, -remaining, Double::sum);
                        remaining = 0.0;
                    }
                }
            }
        }

        bidsToRemove.forEach(bids::remove);
        asksToRemove.forEach(asks::remove);
        ord.amount = remaining;
        return remaining > 0 ? -2 : 0;
    }

    public int IOC(Order ord)
    {
        return match(ord);
    }

    public int Market_order(Order ord)
    {
        ord.price = 0.0;
        return match(ord);
    }

    public int FOK(Order ord)
    {
        if(ord._type == type.BUY)
        {
            Double total = 0.0;
            for (Double price : askLiqudity.keySet()) 
            {
                if (price > ord.price) break;
                total += askLiqudity.get(price);
                if(total >= ord.amount)
                {
                    return match(ord);
                }
            }
        }
        if(ord._type == type.SELL)
        {
            Double total = 0.0;
            for (Double price : bidLiqudity.keySet()) 
            {
                if (price < ord.price) break;
                total = bidLiqudity.get(price);
                if(total >= ord.amount)
                {
                    return match(ord);
                }
            }
        }
        return -2;
    }
    
    public int GOC(Order ord)
    {
        int rs = match(ord);
        if (rs == -2)
        {
            if(ord._type == type.BUY)
            {
                bids.computeIfAbsent(ord.price, k -> new ArrayList<Order>()).add(ord);
                return -2;
            }
            if(ord._type == type.SELL)
            {
                asks.computeIfAbsent(ord.price, k -> new ArrayList<Order>()).add(ord);
                return -2;
            }
        }
        return 0;
    }

    public void Cancel(Order ord)
    {
        if(ord._type == type.BUY)
        {
            bids.get(ord.price).remove(ord);
            bidLiqudity.merge(ord.price, ord.amount, Double::sum);
        }
        if(ord._type == type.SELL)
        {
            asks.get(ord.price).remove(ord);
            askLiqudity.merge(ord.price, ord.amount, Double::sum);
        }
    }

    public void Replace(Order ord, Order replacement)
    {
        Cancel(ord);
        Market_order(replacement);
    }


}
