package smeo.experiments.chronicle.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Created by smeo on 02.08.16.
 */
public class TradeDetails implements Serializable {
    private final LocalDateTime tradeTime;
    private final String currency;
    private final double rate;
    private final double amount;
    private final Side side;
    private final String trader;

    public TradeDetails(LocalDateTime tradeTime, String currency, double rate, double amount, Side side, String trader) {
        this.tradeTime = tradeTime;
        this.currency = currency;
        this.rate = rate;
        this.amount = amount;
        this.side = side;
        this.trader = trader;
    }
}
